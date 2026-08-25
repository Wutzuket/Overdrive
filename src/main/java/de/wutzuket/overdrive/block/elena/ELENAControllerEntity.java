package de.wutzuket.overdrive.block.elena;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.recipe.AntiprotonCoolingRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ELENAControllerEntity extends BlockEntity implements IEnergyStorage, MenuProvider {
    private static final int MAX_ENERGY = 300_000;
    private static final int ENERGY_PER_TICK = 1000;
    private static final int OUTER_RADIUS = 2;
    private static final int TANK_CAPACITY = 4_000;

    private int energy = 0;
    private final FluidTank fluidTank = new FluidTank(TANK_CAPACITY);
    private boolean validStructure = false;
    private boolean hologramEnabled = false;
    private boolean active = false;
    private int progressTicks = 0;

    private @Nullable AntiprotonCoolingRecipe currentRecipe = null;
    private @Nullable BlockPos inputPortPos = null;
    private @Nullable BlockPos outputPortPos = null;

    public ELENAControllerEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.ELENA_CONTROLLER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            if (validStructure && active && level.getGameTime() % 5 == 0) {
                spawnAmbientParticles(level, pos, state);
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        boolean wasValid = validStructure;
        validStructure = checkStructure(level, pos, state);
        if (validStructure && !wasValid) {
            spawnValidationParticles(serverLevel, pos, state);
        }

        if (!validStructure) {
            if (active) {
                triggerAnnihilation(serverLevel, pos, state);
            }
            active = false;
            progressTicks = 0;
            currentRecipe = null;
            inputPortPos = null;
            outputPortPos = null;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        if (currentRecipe == null) {
            inputPortPos = findInputPort(level, pos, state);
            outputPortPos = findOutputPort(level, pos, state);
            currentRecipe = findRecipe(level);
        }

        if (currentRecipe == null) {
            active = false;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        FluidStack inputFluid = getInputFluid();
        if (inputFluid.isEmpty() || !inputFluid.is(currentRecipe.inputFluid().getFluid()) || inputFluid.getAmount() < currentRecipe.inputFluid().getAmount()) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        if (!finalOutputCanAccept()) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        int energyPerTick = Math.max(1, currentRecipe.energyCost() / Math.max(1, currentRecipe.processTime()));
        if (energy < energyPerTick) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        float vacuumLevel = getVacuumLevel(level, pos, state);
        if (vacuumLevel < 0.1f) {
            if (active && progressTicks > 0) {
                triggerAnnihilation(serverLevel, pos, state);
            }
            active = false;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        if (!active) {
            active = true;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        energy -= energyPerTick;
        progressTicks++;

        if (progressTicks >= currentRecipe.processTime()) {
            completeProcessing(serverLevel, pos, state);
        }

        pushFluidToOutput(level);
        setChanged();
        level.sendBlockUpdated(pos, state, state, 2);
    }

    private void completeProcessing(ServerLevel level, BlockPos pos, BlockState state) {
        if (currentRecipe == null) return;

        FluidStack input = currentRecipe.inputFluid();
        FluidStack output = currentRecipe.outputFluid();
        ItemStack outputCell = currentRecipe.outputCell();

        if (!drainInputFluid(input.getAmount(), IFluidHandler.FluidAction.SIMULATE)) return;

        int canFill = fluidTank.fill(output.copy(), IFluidHandler.FluidAction.SIMULATE);
        if (canFill < output.getAmount()) return;

        if (!outputCell.isEmpty() && outputPortPos != null) {
            BlockEntity be = level.getBlockEntity(outputPortPos);
            if (!(be instanceof ELENAOutputPortEntity outputPort)) {
                outputPortPos = null;
                return;
            }
            ItemStack remainder = outputPort.getItemHandler().insertItem(0, outputCell.copy(), false);
            if (!remainder.isEmpty()) return;
        }

        drainInputFluid(input.getAmount(), IFluidHandler.FluidAction.EXECUTE);
        fluidTank.fill(output.copy(), IFluidHandler.FluidAction.EXECUTE);

        progressTicks = 0;
        currentRecipe = null;
        spawnOutputParticles(level, pos, state);
        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private boolean drainInputFluid(int amount, IFluidHandler.FluidAction action) {
        if (inputPortPos == null) return false;
        BlockEntity be = level.getBlockEntity(inputPortPos);
        if (!(be instanceof ELENAInputPortEntity port)) {
            inputPortPos = null;
            return false;
        }
        FluidStack drained = port.getTank().drain(amount, action);
        return drained.getAmount() >= amount;
    }

    private FluidStack getInputFluid() {
        if (inputPortPos == null) return FluidStack.EMPTY;
        BlockEntity be = level.getBlockEntity(inputPortPos);
        if (!(be instanceof ELENAInputPortEntity port)) {
            inputPortPos = null;
            return FluidStack.EMPTY;
        }
        return port.getTank().getFluid();
    }

    private boolean finalOutputCanAccept() {
        if (outputPortPos == null) return false;
        BlockEntity be = level.getBlockEntity(outputPortPos);
        if (!(be instanceof ELENAOutputPortEntity output)) {
            outputPortPos = null;
            return false;
        }
        if (currentRecipe == null) return false;
        FluidStack outputFluid = currentRecipe.outputFluid();
        if (!outputFluid.isEmpty()) {
            int canFill = output.getFluidTank().fill(outputFluid.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (canFill < outputFluid.getAmount()) return false;
        }
        ItemStack outputCell = currentRecipe.outputCell();
        if (!outputCell.isEmpty()) {
            ItemStack remainder = output.getItemHandler().insertItem(0, outputCell.copy(), true);
            if (!remainder.isEmpty()) return false;
        }
        return true;
    }

    private void pushFluidToOutput(Level level) {
        if (outputPortPos == null) return;
        if (fluidTank.isEmpty()) return;

        BlockEntity be = level.getBlockEntity(outputPortPos);
        if (!(be instanceof ELENAOutputPortEntity output)) {
            outputPortPos = null;
            return;
        }

        FluidStack toPush = fluidTank.drain(fluidTank.getFluidAmount(), IFluidHandler.FluidAction.SIMULATE);
        if (toPush.isEmpty()) return;

        int filled = output.getFluidTank().fill(toPush.copy(), IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0) {
            fluidTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }
    }

    private @Nullable AntiprotonCoolingRecipe findRecipe(Level level) {
        FluidStack inputFluid = getInputFluid();
        if (inputFluid.isEmpty()) return null;

        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof AntiprotonCoolingRecipe recipe && recipe.getType() == ODRecipeTypes.ELENA_RECIPE.get()) {
                if (recipe.inputFluid().getFluid() == inputFluid.getFluid() && inputFluid.getAmount() >= recipe.inputFluid().getAmount()) {
                    return recipe;
                }
            }
        }
        return null;
    }

    private boolean checkStructure(Level level, BlockPos controllerPos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = controllerPos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = controllerPos.getY();
        int cz = controllerPos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        boolean foundController = false;
        int energyPortCount = 0;
        int vacuumPortCount = 0;
        int inputPortCount = 0;
        int outputPortCount = 0;

        int minX = cx - OUTER_RADIUS;
        int maxX = cx + OUTER_RADIUS;
        int minZ = cz - OUTER_RADIUS;
        int maxZ = cz + OUTER_RADIUS;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int maxDist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                boolean inRing = maxDist >= 1 && maxDist <= OUTER_RADIUS;
                if (!inRing) continue;

                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos checkPos = new BlockPos(x, cy + dy, z);
                    BlockState checkState = level.getBlockState(checkPos);

                    if (dy == -1 || dy == 1) {
                        if (!(checkState.getBlock() instanceof ELENACasingBlock)) return false;
                    } else {
                        if (checkPos.equals(controllerPos)) {
                            foundController = true;
                        } else if (checkState.getBlock() instanceof ELENACasingBlock) {
                        } else if (checkState.getBlock() instanceof ELENAEnergyInputPortBlock) {
                            energyPortCount++;
                        } else if (checkState.getBlock() instanceof ELENAVacuumPortBlock) {
                            vacuumPortCount++;
                        } else if (checkState.getBlock() instanceof ELENAInputPortBlock) {
                            inputPortCount++;
                        } else if (checkState.getBlock() instanceof ELENAOutputPortBlock) {
                            outputPortCount++;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return foundController && energyPortCount >= 1 && vacuumPortCount >= 1 && inputPortCount >= 1 && outputPortCount >= 1;
    }

    private @Nullable BlockPos findInputPort(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        for (int x = cx - OUTER_RADIUS; x <= cx + OUTER_RADIUS; x++) {
            for (int z = cz - OUTER_RADIUS; z <= cz + OUTER_RADIUS; z++) {
                int maxDist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                if (maxDist < 1 || maxDist > OUTER_RADIUS) continue;
                BlockPos checkPos = new BlockPos(x, cy, z);
                if (level.getBlockState(checkPos).getBlock() instanceof ELENAInputPortBlock) return checkPos;
            }
        }
        return null;
    }

    private @Nullable BlockPos findOutputPort(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        for (int x = cx - OUTER_RADIUS; x <= cx + OUTER_RADIUS; x++) {
            for (int z = cz - OUTER_RADIUS; z <= cz + OUTER_RADIUS; z++) {
                int maxDist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                if (maxDist < 1 || maxDist > OUTER_RADIUS) continue;
                BlockPos checkPos = new BlockPos(x, cy, z);
                if (level.getBlockState(checkPos).getBlock() instanceof ELENAOutputPortBlock) return checkPos;
            }
        }
        return null;
    }

    private float getVacuumLevel(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        float bestVacuum = 0;
        for (int x = cx - OUTER_RADIUS; x <= cx + OUTER_RADIUS; x++) {
            for (int z = cz - OUTER_RADIUS; z <= cz + OUTER_RADIUS; z++) {
                int maxDist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                if (maxDist < 1 || maxDist > OUTER_RADIUS) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos checkPos = new BlockPos(x, cy + dy, z);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be instanceof IVacuumNode node) {
                        bestVacuum = Math.max(bestVacuum, node.getVacuumLevel());
                    }
                }
            }
        }
        return bestVacuum;
    }

    private void triggerAnnihilation(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;
        float power = 6.0f;
        level.explode(null, cx + 0.5, cy + 0.5, cz + 0.5, power, Level.ExplosionInteraction.TNT);
        active = false;
        progressTicks = 0;
        validStructure = false;
        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private void spawnValidationParticles(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        DustParticleOptions options = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.5f);
        int steps = 30;
        for (int i = 0; i < steps; i++) {
            double angle = 2.0 * Math.PI * i / steps;
            double rx = OUTER_RADIUS * Math.cos(angle);
            double rz = OUTER_RADIUS * Math.sin(angle);
            double px = cx + rx + 0.5;
            double pz = cz + rz + 0.5;
            level.sendParticles(options, px, cy + 0.5, pz, 1, 0, 0, 0, 0.05);
        }
    }

    private void spawnAmbientParticles(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        DustParticleOptions options = new DustParticleOptions(new Vector3f(1.0f, 0.2f, 0.0f), 1.0f);
        for (int i = 0; i < 3; i++) {
            double angle = 2.0 * Math.PI * level.random.nextDouble();
            double rx = (OUTER_RADIUS - 0.5) * Math.cos(angle);
            double rz = (OUTER_RADIUS - 0.5) * Math.sin(angle);
            double px = cx + rx + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            double pz = cz + rz + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(options, px, cy + 0.5 + (level.random.nextDouble() - 0.5), pz, 1, 0, 0, 0, 0.05);
        }
    }

    private void spawnOutputParticles(ServerLevel level, BlockPos pos, BlockState state) {
        DustParticleOptions options = new DustParticleOptions(new Vector3f(0.5f, 0.0f, 1.0f), 1.5f);
        level.sendParticles(options, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.1);
    }

    public void toggleHologram() {
        this.hologramEnabled = !this.hologramEnabled;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void setHologramEnabled(boolean enabled) {
        this.hologramEnabled = enabled;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean isValidStructure() { return validStructure; }
    public float getVacuumLevel() { return getVacuumLevel(level, worldPosition, getBlockState()); }
    public boolean isHologramEnabled() {
        if (level != null && level.isClientSide) {
            if (Overdrive.isClientHologramEnabled(worldPosition)) return true;
        }
        return hologramEnabled;
    }
    public boolean isActive() { return active; }
    public int getProgressTicks() { return progressTicks; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return MAX_ENERGY; }
    public FluidTank getFluidTank() { return fluidTank; }

    public int getMaxProgressTicks() { return currentRecipe != null ? currentRecipe.processTime() : 200; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.elena_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new de.wutzuket.overdrive.menu.ELENAControllerMenu(id, inv, this);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!validStructure) return 0;
        int accepted = Math.min(maxReceive, MAX_ENERGY - energy);
        if (!simulate) {
            energy += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
    @Override public int getEnergyStored() { return energy; }
    @Override public int getMaxEnergyStored() { return MAX_ENERGY; }
    @Override public boolean canExtract() { return false; }
    @Override public boolean canReceive() { return validStructure; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putBoolean("validStructure", validStructure);
        tag.putBoolean("hologramEnabled", hologramEnabled);
        tag.putBoolean("active", active);
        tag.putInt("progressTicks", progressTicks);
        tag.put("fluid", fluidTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        validStructure = tag.getBoolean("validStructure");
        hologramEnabled = tag.getBoolean("hologramEnabled");
        active = tag.getBoolean("active");
        progressTicks = tag.getInt("progressTicks");
        if (tag.contains("fluid")) fluidTank.readFromNBT(registries, tag.getCompound("fluid"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

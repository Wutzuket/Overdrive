package de.wutzuket.overdrive.block.accelerator;

import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.menu.AcceleratorControllerMenu;
import de.wutzuket.overdrive.recipe.CollisionRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
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
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

public class ParticleAcceleratorControllerEntity extends BlockEntity implements IEnergyStorage, MenuProvider {
    private static final int MAX_ENERGY = 1_000_000;
    private static final int ENERGY_PER_TICK = 5000;
    private static final int OUTER_RADIUS = 25;
    private static final int TANK_CAPACITY = 16_000;

    private int energy = 0;
    private final FluidTank fluidTank = new FluidTank(TANK_CAPACITY);
    private boolean validStructure = false;
    private boolean hologramEnabled = false;
    private boolean active = false;
    private int progressTicks = 0;
    private boolean particlesSpawned = false;

    private @Nullable CollisionRecipe currentRecipe = null;
    private @Nullable BlockPos iridiumTargetPos = null;
    private @Nullable BlockPos outputPos = null;

    public ParticleAcceleratorControllerEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.PARTICLE_ACCELERATOR_CONTROLLER.get(), pos, state);
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
            spawnRingParticles(serverLevel, pos, state);
        }

        if (!validStructure) {
            if (active) {
                triggerAnnihilation(serverLevel, pos, state);
            }
            active = false;
            progressTicks = 0;
            currentRecipe = null;
            iridiumTargetPos = null;
            outputPos = null;
            return;
        }

        if (!active) {
            currentRecipe = findRecipe(level, pos, state);
            iridiumTargetPos = findTargetPos(level, pos, state);
            outputPos = findOutputPos(level, pos, state);
        }

        if (currentRecipe == null || iridiumTargetPos == null) {
            active = false;
            progressTicks = 0;
            outputPos = null;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        IridiumTargetEntity target = getTargetEntity(level);
        if (target == null || !target.hasValidBlock()) {
            active = false;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        float vacuumLevel = getVacuumLevel(level, pos, state);
        if (vacuumLevel < 0.90f) {
            if (active && progressTicks > 0) {
                triggerAnnihilation(serverLevel, pos, state);
            }
            active = false;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        if (energy < currentRecipe.energyCost()) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        if (!currentRecipe.outputFluid().isEmpty() && !canOutputAcceptFluid(level, currentRecipe.outputFluid())) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        if (!active) {
            active = true;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        energy -= ENERGY_PER_TICK;
        progressTicks++;

        if (progressTicks >= currentRecipe.processTime()) {
            completeProcessing(serverLevel, pos, state, target);
        }

        pushFluidToOutput(level);

        setChanged();
        level.sendBlockUpdated(pos, state, state, 2);
    }

    private void completeProcessing(ServerLevel level, BlockPos pos, BlockState state, IridiumTargetEntity target) {
        if (currentRecipe == null) return;

        FluidStack output = currentRecipe.outputFluid();
        if (!output.isEmpty()) {
            int canFill = fluidTank.fill(output.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (canFill < output.getAmount()) {
                return;
            }
        }

        target.consumeOneItem();

        if (!output.isEmpty()) {
            int filled = fluidTank.fill(output.copy(), IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                spawnOutputParticles(level, pos, state);
            }
        }

        progressTicks = 0;

        if (!target.hasValidBlock()) {
            active = false;
            currentRecipe = null;
        }

        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private @Nullable CollisionRecipe findRecipe(Level level, BlockPos pos, BlockState state) {
        IridiumTargetEntity target = getTargetEntity(level);
        if (target == null) return null;
        ItemStack targetStack = target.getStack();
        if (targetStack.isEmpty()) return null;

        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof CollisionRecipe recipe && recipe.getType() == ODRecipeTypes.COLLISION.get()) {
                if (recipe.inputTarget().test(targetStack)) {
                    return recipe;
                }
            }
        }
        return null;
    }

    private @Nullable BlockPos findTargetPos(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        int minX = cx - OUTER_RADIUS - 1;
        int maxX = cx + OUTER_RADIUS + 1;
        int minZ = cz - OUTER_RADIUS - 1;
        int maxZ = cz + OUTER_RADIUS + 1;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (dist >= OUTER_RADIUS - 3.5 && dist <= OUTER_RADIUS + 0.5) {
                    BlockPos checkPos = new BlockPos(x, pos.getY(), z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof IridiumTargetBlock) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private IridiumTargetEntity getTargetEntity(Level level) {
        if (iridiumTargetPos == null) return null;
        BlockEntity be = level.getBlockEntity(iridiumTargetPos);
        if (be instanceof IridiumTargetEntity target) {
            return target;
        }
        return null;
    }

    private @Nullable BlockPos findOutputPos(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        int minX = cx - OUTER_RADIUS - 1;
        int maxX = cx + OUTER_RADIUS + 1;
        int minZ = cz - OUTER_RADIUS - 1;
        int maxZ = cz + OUTER_RADIUS + 1;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (dist >= OUTER_RADIUS - 3.5 && dist <= OUTER_RADIUS + 0.5) {
                    BlockPos checkPos = new BlockPos(x, pos.getY(), z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof AcceleratorOutputBlock) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    private boolean canOutputAcceptFluid(Level level, FluidStack fluidStack) {
        if (outputPos == null) return false;
        BlockEntity be = level.getBlockEntity(outputPos);
        if (!(be instanceof AcceleratorOutputEntity output)) {
            outputPos = null;
            return false;
        }
        int canFill = output.fill(fluidStack.copy(), IFluidHandler.FluidAction.SIMULATE);
        return canFill >= fluidStack.getAmount();
    }

    private void pushFluidToOutput(Level level) {
        if (outputPos == null) return;
        if (fluidTank.isEmpty()) return;

        BlockEntity be = level.getBlockEntity(outputPos);
        if (!(be instanceof AcceleratorOutputEntity output)) {
            outputPos = null;
            return;
        }

        FluidStack toDrain = fluidTank.drain(fluidTank.getFluidAmount(), IFluidHandler.FluidAction.SIMULATE);
        if (toDrain.isEmpty()) return;

        int filled = output.fill(toDrain.copy(), IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0) {
            fluidTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }
    }

    private boolean checkStructure(Level level, BlockPos controllerPos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = controllerPos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = controllerPos.getY();
        int cz = controllerPos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        boolean foundTarget = false;
        boolean foundController = false;
        int energyPortCount = 0;
        int vacuumPortCount = 0;

        int minX = cx - OUTER_RADIUS - 2;
        int maxX = cx + OUTER_RADIUS + 2;
        int minZ = cz - OUTER_RADIUS - 2;
        int maxZ = cz + OUTER_RADIUS + 2;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = x - cx;
                double dz = z - cz;
                double dist = Math.sqrt(dx * dx + dz * dz);

                boolean inRing = dist >= OUTER_RADIUS - 3.5 && dist <= OUTER_RADIUS + 0.5;
                if (!inRing) continue;

                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos checkPos = new BlockPos(x, cy + dy, z);
                    BlockState checkState = level.getBlockState(checkPos);

                    if (dy == -1 || dy == 1) {
                        if (!(checkState.getBlock() instanceof AcceleratorCasing) &&
                                !(checkState.getBlock() instanceof AcceleratorOutputBlock)) {
                            return false;
                        }
                    } else {
                        boolean onOuterWall = dist >= OUTER_RADIUS - 0.5;
                        boolean onInnerWall = dist <= OUTER_RADIUS - 2.5;

                        if (onOuterWall || onInnerWall) {
                            if (checkPos.equals(controllerPos)) {
                                foundController = true;
                            } else if (checkState.getBlock() instanceof IridiumTargetBlock) {
                                foundTarget = true;
                            } else if (checkState.getBlock() instanceof AcceleratorCasing) {
                            } else if (checkState.getBlock() instanceof AcceleratorOutputBlock) {
                            } else if (checkState.getBlock() instanceof AcceleratorEnergyInputPortBlock) {
                                energyPortCount++;
                            } else if (checkState.getBlock() instanceof AcceleratorVacuumInputPortBlock) {
                                vacuumPortCount++;
                            } else {
                                return false;
                            }
                        } else {
                            if (!checkState.isAir()) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return foundTarget && foundController;
    }

    public float getVacuumLevel() {
        if (level == null) return 0;
        return getVacuumLevel(level, worldPosition, getBlockState());
    }

    private float getVacuumLevel(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        int minX = cx - OUTER_RADIUS - 2;
        int maxX = cx + OUTER_RADIUS + 2;
        int minZ = cz - OUTER_RADIUS - 2;
        int maxZ = cz + OUTER_RADIUS + 2;

        float bestVacuum = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = x - cx;
                double dz = z - cz;
                double dist = Math.sqrt(dx * dx + dz * dz);

                boolean inRing = dist >= OUTER_RADIUS - 3.5 && dist <= OUTER_RADIUS + 0.5;
                if (!inRing) continue;

                boolean onOuterWall = dist >= OUTER_RADIUS - 0.5;
                boolean onInnerWall = dist <= OUTER_RADIUS - 2.5;
                if (!onOuterWall && !onInnerWall) continue;

                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos checkPos = new BlockPos(x, cy + dy, z);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be instanceof de.wutzuket.overdrive.block.vacuum.IVacuumNode node) {
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
        float power = 10.0f;
        level.explode(null, cx + 0.5, cy + 0.5, cz + 0.5, power, Level.ExplosionInteraction.TNT);
        active = false;
        progressTicks = 0;
        validStructure = false;
        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private void spawnRingParticles(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        DustParticleOptions options = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.5f);
        int steps = 200;
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
            double rx = (OUTER_RADIUS - 1.5) * Math.cos(angle);
            double rz = (OUTER_RADIUS - 1.5) * Math.sin(angle);
            double px = cx + rx + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            double pz = cz + rz + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(options, px, cy + 0.5 + (level.random.nextDouble() - 0.5), pz, 1, 0, 0, 0, 0.05);
        }
    }

    private void spawnOutputParticles(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        DustParticleOptions options = new DustParticleOptions(new Vector3f(0.5f, 0.0f, 1.0f), 1.5f);
        double angle = 2.0 * Math.PI * level.random.nextDouble();
        double rx = OUTER_RADIUS * Math.cos(angle);
        double rz = OUTER_RADIUS * Math.sin(angle);
        double px = cx + rx + 0.5;
        double pz = cz + rz + 0.5;
        level.sendParticles(options, px, cy + 0.5, pz, 20, 0.5, 0.5, 0.5, 0.1);
    }

    public void toggleHologram() {
        this.hologramEnabled = !this.hologramEnabled;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setHologramEnabled(boolean enabled) {
        this.hologramEnabled = enabled;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.accelerator_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AcceleratorControllerMenu(id, inv, this);
    }

    public boolean isValidStructure() {
        return validStructure;
    }

    public boolean isHologramEnabled() {
        if (level != null && level.isClientSide) {
            if (de.wutzuket.overdrive.Overdrive.isClientHologramEnabled(worldPosition)) {
                return true;
            }
        }
        return hologramEnabled;
    }

    public boolean isActive() {
        return active;
    }

    public int getProgressTicks() {
        return progressTicks;
    }

    public int getEnergy() {
        return energy;
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

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return MAX_ENERGY;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return validStructure;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

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
        if (tag.contains("fluid")) {
            fluidTank.readFromNBT(registries, tag.getCompound("fluid"));
        }
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

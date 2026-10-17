package de.wutzuket.overdrive.block.distiller;

import de.wutzuket.overdrive.fluid.InOutFluidHandler;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.menu.CryoDistillerMenu;
import de.wutzuket.overdrive.recipe.CryoDistillerRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * Cryo Distiller: 1x1x1 Block mit Strom-Eingang und je einem Fluid-Input-/Output-Tank.
 * Das aktive Rezept kann ueber {@code "catalysator": { "block": "..." }} verlangen, dass ein
 * bestimmter Block direkt darunter liegt.
 */
public class CryoDistillerEntity extends BlockEntity implements IEnergyStorage, MenuProvider {
    public static final int MAX_ENERGY = 100_000_000;
    public static final int TANK_CAPACITY = 32_000;

    private int energy = 0;
    private final FluidTank inputTank = new FluidTank(TANK_CAPACITY) {
        @Override protected void onContentsChanged() {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };
    private final FluidTank outputTank = new FluidTank(TANK_CAPACITY) {
        @Override protected void onContentsChanged() {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };
    private final InOutFluidHandler fluidHandler = new InOutFluidHandler(inputTank, outputTank);

    private boolean active = false;
    private boolean hologramEnabled = false;
    private int progress = 0;
    private int lastProcessTime = 100;
    private int lastEnergyPerTick = 0;
    private @Nullable ResourceLocation catalystBlockId = null;
    private boolean catalystPresent = false;
    private @Nullable CryoDistillerRecipe recipe = null;

    public CryoDistillerEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.CRYO_DISTILLER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!(level.getBlockState(pos).getBlock() instanceof CryoDistillerBlock)) return;

        recipe = findRecipe(level);
        if (recipe == null) {
            stop(level, pos, state);
            setCatalyst(null, false);
            return;
        }
        lastProcessTime = Math.max(1, recipe.processTime());
        lastEnergyPerTick = recipe.energyPerTick();

        Block catalystBlock = resolveBlock(recipe.catalystBlockId());
        boolean catalystOk = catalystSatisfied(catalystBlock, level.getBlockState(pos.below()).getBlock());
        setCatalyst(recipe.catalystBlockId(), catalystOk);

        if (!catalystOk) {
            stop(level, pos, state);
            return;
        }
        if (inputTank.getFluidAmount() < recipe.inputFluid().getAmount()) {
            stop(level, pos, state);
            return;
        }
        if (energy < recipe.energyPerTick()) {
            stop(level, pos, state);
            return;
        }

        FluidStack out = recipe.outputFluid();
        if (!out.isEmpty() && outputTank.fill(out.copy(), IFluidHandler.FluidAction.SIMULATE) < out.getAmount()) {
            stop(level, pos, state);
            return;
        }

        if (!active) {
            active = true;
            progress = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        energy -= recipe.energyPerTick();
        progress++;

        if (progress >= recipe.processTime()) {
            inputTank.drain(recipe.inputFluid().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            if (!out.isEmpty()) {
                outputTank.fill(out.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
            progress = 0;
            recipe = null;
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.62, pos.getZ() + 0.5,
                        8, 0.25, 0.25, 0.25, 0.01);
            }
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        } else {
            setChanged();
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    private void stop(Level level, BlockPos pos, BlockState state) {
        if (active || progress != 0) {
            active = false;
            progress = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    /** Katalysator-Anzeige nur bei Aenderung syncen. */
    private void setCatalyst(@Nullable ResourceLocation id, boolean present) {
        boolean changed = !java.util.Objects.equals(catalystBlockId, id) || catalystPresent != present;
        catalystBlockId = id;
        catalystPresent = present;
        if (changed) {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private static @Nullable Block resolveBlock(@Nullable ResourceLocation id) {
        return id == null ? null : BuiltInRegistries.BLOCK.get(id);
    }

    /**
     * Ist der geforderte Katalysator erfüllt?
     * - kein Katalysator im Rezept  -> immer ok
     * - ungueltige Block-ID        -> nie ok (BuiltInRegistries liefert dann AIR)
     */
    private static boolean catalystSatisfied(@Nullable Block catalystBlock, Block below) {
        if (catalystBlock == null) return true;
        if (catalystBlock == Blocks.AIR) return false;
        return below == catalystBlock;
    }

    private boolean recipeMatchesTanks(CryoDistillerRecipe r) {
        FluidStack in = inputTank.getFluid();
        if (in.isEmpty() || !in.is(r.inputFluid().getFluid())) return false;
        FluidStack out = outputTank.getFluid();
        if (!out.isEmpty() && !out.is(r.outputFluid().getFluid())) return false;
        return true;
    }

    /**
     * Passt das gecachte Rezept weiterhin zu den Tankinhalten?
     * Beachtet auch, dass der Katalysator-Block von aussen entfernt/getauscht wurde.
     */
    private boolean recipeStillValid(Level level) {
        if (recipe == null || !recipeMatchesTanks(recipe)) return false;
        return catalystSatisfied(resolveBlock(recipe.catalystBlockId()),
                level.getBlockState(worldPosition.below()).getBlock());
    }

    /**
     * Sucht ein passendes Rezept fuer den Input-Tank. Bevorzugt wird eines, dessen Katalysator
     * tatsaechlich unter der Maschine liegt - sonst wird das erste passende zurueckgegeben,
     * damit die GUI anzeigen kann, welcher Block fehlt.
     */
    private @Nullable CryoDistillerRecipe findRecipe(Level level) {
        if (recipe != null && recipeStillValid(level)) {
            return recipe;
        }
        recipe = null;

        FluidStack in = inputTank.getFluid();
        if (in.isEmpty()) return null;

        Block below = level.getBlockState(worldPosition.below()).getBlock();
        CryoDistillerRecipe firstMatch = null;

        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof CryoDistillerRecipe r
                    && r.getType() == ODRecipeTypes.CRYO_DISTILLER.get()) {
                if (!in.is(r.inputFluid().getFluid())) continue;
                if (in.getAmount() < r.inputFluid().getAmount()) continue;
                FluidStack out = outputTank.getFluid();
                if (!out.isEmpty() && !out.is(r.outputFluid().getFluid())) continue;

                Block catalystBlock = resolveBlock(r.catalystBlockId());
                if (catalystSatisfied(catalystBlock, below)) {
                    recipe = r;
                    return r;
                }
                if (firstMatch == null) firstMatch = r;
            }
        }
        recipe = firstMatch;
        return firstMatch;
    }

    public FluidTank getInputTank() { return inputTank; }
    public FluidTank getOutputTank() { return outputTank; }
    public IFluidHandler getFluidHandler() { return fluidHandler; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return Math.max(1, lastProcessTime); }
    public int getEnergyPerTick() { return lastEnergyPerTick; }
    public boolean isActive() { return active; }

    /** Block-ID des aktuell geforderten Katalysators, oder null. */
    public @Nullable ResourceLocation getCatalystBlockId() { return catalystBlockId; }

    /** Liegt der geforderte Katalysator aktuell darunter? */
    public boolean isCatalystPresent() { return catalystPresent; }

    /** true, wenn der aktive Vorgang einen Katalysator-Block unter der Maschine braucht. */
    public boolean needsCatalyst() { return catalystBlockId != null; }

    /** Anzeige-Item des geforderten Katalysators fuer die GUI (leer bei ungueltiger Block-ID). */
    public ItemStack getCatalystStack() {
        Block block = resolveBlock(catalystBlockId);
        if (block == null || block == Blocks.AIR) return ItemStack.EMPTY;
        return new ItemStack(block.asItem());
    }

    public boolean isHologramEnabled() {
        if (level != null && level.isClientSide && de.wutzuket.overdrive.Overdrive.isClientHologramEnabled(worldPosition)) {
            return true;
        }
        return hologramEnabled;
    }

    public void toggleHologram() {
        this.hologramEnabled = !hologramEnabled;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.cryo_distiller");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CryoDistillerMenu(id, inv, this);
    }

    // ---- Energie (nur Eingang) ----

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Math.min(maxReceive, MAX_ENERGY - energy);
        if (accepted <= 0) return 0;
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
    @Override public boolean canReceive() { return true; }

    // ---- NBT ----

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putBoolean("active", active);
        tag.putBoolean("holo", hologramEnabled);
        tag.putInt("progress", progress);
        tag.putInt("lastTime", lastProcessTime);
        tag.putInt("lastEpt", lastEnergyPerTick);
        tag.putBoolean("catalystPresent", catalystPresent);
        if (catalystBlockId != null) tag.putString("catalyst", catalystBlockId.toString());
        tag.put("in", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("out", outputTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        active = tag.getBoolean("active");
        hologramEnabled = tag.getBoolean("holo");
        progress = tag.getInt("progress");
        if (tag.contains("lastTime")) lastProcessTime = Math.max(1, tag.getInt("lastTime"));
        lastEnergyPerTick = tag.getInt("lastEpt");
        catalystPresent = tag.getBoolean("catalystPresent");
        catalystBlockId = tag.contains("catalyst") ? ResourceLocation.tryParse(tag.getString("catalyst")) : null;
        if (tag.contains("in")) inputTank.readFromNBT(registries, tag.getCompound("in"));
        if (tag.contains("out")) outputTank.readFromNBT(registries, tag.getCompound("out"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
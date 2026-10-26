package de.wutzuket.overdrive.block.tungsten;

import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import de.wutzuket.overdrive.recipe.PositronExtractionRecipe;
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
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.IEnergyStorage;
import de.wutzuket.overdrive.antimatter.AntimatterStack;
import de.wutzuket.overdrive.antimatter.AntimatterTank;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ElectronGunEntity extends BlockEntity implements IEnergyStorage, MenuProvider, IVacuumNode {
    private static final int MAX_ENERGY = 500_000;
    /** Maximale einstellbare FE/t-Rate, begrenzt durch den internen Puffer. */
    public static final int MAX_ENERGY_RATE = MAX_ENERGY;
    /** Standardverbrauch = 1x Produktion (energy_cost des Standardrezepts: 50.000 FE/t fuer 4 B/t). */
    public static final int DEFAULT_ENERGY_RATE = 50_000;

    private int energy = 0;
    private int energyRate = DEFAULT_ENERGY_RATE;
    // Positron-Max-Rate: 14.000 mB/Op
    private final AntimatterTank fluidTank = new AntimatterTank(500_000);
    private boolean active = false;
    private boolean hologramEnabled = false;
    private int progressTicks = 0;
    private float vacuumLevel = 0;
    private long vacuumFedTick = 0;

    private @Nullable PositronExtractionRecipe currentRecipe = null;
    /** Erstes Rezept der Maschine, damit die Produktionsanzeige auch zwischen zwei Ops stimmt. */
    private @Nullable PositronExtractionRecipe baseRecipe = null;
    /** RecipeManager, zu dem {@link #baseRecipe} gehoert (wechselt bei /reload). */
    private @Nullable RecipeManager cachedRecipeManager = null;
    private @Nullable BlockPos targetPos = null;
    private @Nullable BlockPos outputPos = null;

    public ElectronGunEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.ELECTRON_GUN.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!(level.getBlockState(pos).getBlock() instanceof ElectronGunBlock)) return;

        IVacuumNode.propagate(this);

        ServerLevel serverLevel = (ServerLevel) level;

        // /reload ersetzt den RecipeManager durch ein neues Objekt. Dann muessen die
        // gecachten Rezepte verworfen werden, sonst gelten weiter die alten Werte
        // (z.B. mesh_durability_cost), bis der Block neu geladen wird.
        if (level.getRecipeManager() != cachedRecipeManager) {
            cachedRecipeManager = level.getRecipeManager();
            baseRecipe = null;
            currentRecipe = null;
        }

        if (currentRecipe == null) {
            currentRecipe = findRecipe(level);
        }
        // Rezept-Limit anwenden, falls die gespeicherte Rate darueber liegt.
        int maxRate = getMaxRate();
        if (energyRate > maxRate) {
            energyRate = maxRate;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        // Target und Ausgang jedes Mal neu suchen, wenn sie fehlen oder inzwischen durch
        // einen anderen Block ersetzt wurden. Sonst bleibt die Kanone fuer immer im
        // Leerlauf, wenn die Struktur erst nach dem ersten Tick aufgebaut wurde.
        if (targetPos == null || !(level.getBlockState(targetPos).getBlock() instanceof TungstenTargetBlock)) {
            targetPos = findTungstenTarget(level, pos, state);
        }
        if (outputPos == null || !(level.getBlockState(outputPos).getBlock() instanceof PositronVacuumOutputPortBlock)) {
            outputPos = findOutputPort(level, pos, state);
        }

        if (currentRecipe == null || targetPos == null) {
            active = false;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        TungstenTargetEntity target = getTargetEntity(level);
        if (target == null || !target.hasMesh()) {
            active = false;
            progressTicks = 0;
            currentRecipe = null;
            targetPos = null;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        if (!canOutputAccept(level)) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        if (energy < energyRate) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        float vacuumLevel = this.vacuumLevel;
        if (vacuumLevel < 0.99f) {
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

        energy -= energyRate;
        progressTicks++;

        if (progressTicks >= currentRecipe.processTime()) {
            completeProcessing(serverLevel, pos, state, target);
        }

        pushFluidToOutput(level);
        setChanged();
        level.sendBlockUpdated(pos, state, state, 2);
    }

    private void completeProcessing(ServerLevel level, BlockPos pos, BlockState state, TungstenTargetEntity target) {
        if (currentRecipe == null) return;

        AntimatterStack output = getScaledOutput(currentRecipe);
        if (!output.isEmpty()) {
            int canFill = fluidTank.insertAntimatter(0, output.type(), output.amount(), true);
            if (canFill < output.amount()) return;
            fluidTank.insertAntimatter(0, output.type(), output.amount(), false);
        }

        if (!currentRecipe.outputCell().isEmpty() && outputPos != null) {
            BlockEntity be = level.getBlockEntity(outputPos);
            if (be instanceof PositronVacuumOutputPortEntity port) {
                port.insertItem(currentRecipe.outputCell().copy());
            }
        }

        target.damageMesh(getScaledMeshDurabilityCost(currentRecipe));
        progressTicks = 0;
        currentRecipe = null;
        targetPos = null;
        spawnOutputParticles(level, pos, state);
        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private boolean canOutputAccept(Level level) {
        if (outputPos == null) return false;
        BlockEntity be = level.getBlockEntity(outputPos);
        if (!(be instanceof PositronVacuumOutputPortEntity port)) {
            outputPos = null;
            return false;
        }
        if (currentRecipe == null) return false;
        AntimatterStack output = getScaledOutput(currentRecipe);
        if (!output.isEmpty()) {
            int canFill = port.getTank().insertAntimatter(0, output.type(), output.amount(), true);
            if (canFill < output.amount()) return false;
        }
        if (!currentRecipe.outputCell().isEmpty()) {
            if (!port.canAcceptItem(currentRecipe.outputCell())) return false;
        }
        return true;
    }

    private void pushFluidToOutput(Level level) {
        if (outputPos == null) return;
        if (fluidTank.getAmount(0) <= 0) return;
        BlockEntity be = level.getBlockEntity(outputPos);
        if (!(be instanceof PositronVacuumOutputPortEntity port)) {
            outputPos = null;
            return;
        }
        int toPush = fluidTank.getAmount(0);
        if (toPush <= 0) return;
        int filled = port.getTank().insertAntimatter(0, fluidTank.getAntimatterInTank(0), toPush, false);
        if (filled > 0) {
            fluidTank.extractAntimatter(0, filled, false);
            setChanged();
        }
    }

    private @Nullable PositronExtractionRecipe findRecipe(Level level) {
        if (baseRecipe != null) return baseRecipe;
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof PositronExtractionRecipe recipe && recipe.getType() == ODRecipeTypes.POSITRON_EXTRACTION.get()) {
                baseRecipe = recipe;
                return recipe;
            }
        }
        return null;
    }

    private @Nullable BlockPos findTungstenTarget(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos checkPos = pos.relative(facing, 1);
        if (level.getBlockState(checkPos).getBlock() instanceof TungstenTargetBlock) {
            return checkPos;
        }
        return null;
    }

    private @Nullable BlockPos findOutputPort(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos checkPos = pos.relative(facing, 2);
        if (level.getBlockState(checkPos).getBlock() instanceof PositronVacuumOutputPortBlock) {
            return checkPos;
        }
        return null;
    }

    @Nullable
    private TungstenTargetEntity getTargetEntity(Level level) {
        if (targetPos == null) return null;
        BlockEntity be = level.getBlockEntity(targetPos);
        return be instanceof TungstenTargetEntity target ? target : null;
    }

    @Override
    public float getVacuumLevel() { return vacuumLevel; }

    @Override
    public void setVacuumLevel(float level) {
        this.vacuumLevel = level;
        setChanged();
    }

    @Override
    public long getVacuumFedTick() {
        return vacuumFedTick;
    }

    @Override
    public void setVacuumFedTick(long tick) {
        this.vacuumFedTick = tick;
    }

    private void triggerAnnihilation(ServerLevel level, BlockPos pos, BlockState state) {
        float power = 6.0f;
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, Level.ExplosionInteraction.TNT);
        active = false;
        progressTicks = 0;
        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private void spawnOutputParticles(ServerLevel level, BlockPos pos, BlockState state) {
        DustParticleOptions options = new DustParticleOptions(new Vector3f(0.5f, 0.0f, 1.0f), 1.5f);
        level.sendParticles(options, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.1);
    }

    public int getProgressTicks() { return progressTicks; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return MAX_ENERGY; }
    public boolean isActive() { return active; }
    public AntimatterTank getAntimatterTank() { return fluidTank; }

    public int getEnergyRate() { return energyRate; }

    public void setEnergyRate(int rate) {
        energyRate = Math.max(1, Math.min(getMaxRate(), rate));
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /**
     * Maximal einstellbare Rate. Das Rezept kann sie ueber {@code max_energy_rate} begrenzen;
     * ohne Angabe (0) gilt das Hardware-Limit des Puffers.
     */
    public int getMaxRate() {
        PositronExtractionRecipe recipe = currentRecipe != null ? currentRecipe : baseRecipe;
        if (recipe == null && level != null) {
            recipe = findRecipe(level);
        }
        if (recipe != null && recipe.maxEnergyRate() > 0) {
            return Math.max(1, Math.min(MAX_ENERGY_RATE, recipe.maxEnergyRate()));
        }
        return MAX_ENERGY_RATE;
    }

    /** Faktor aus eingestellter Rate vs. Rezeptkosten: 50.000 FE/t = 1x (4 B/t). */
    public double getRateFactor(@Nullable PositronExtractionRecipe recipe) {
        if (recipe == null || recipe.energyCost() <= 0) return 1.0;
        return energyRate / (double) recipe.energyCost();
    }

    /** Produktion pro Operation, skaliert mit der eingestellten FE/t-Rate. */
    public AntimatterStack getScaledOutput(@Nullable PositronExtractionRecipe recipe) {
        if (recipe == null || recipe.outputAntimatter().isEmpty()) return AntimatterStack.EMPTY;
        long amount = Math.round(recipe.outputAntimatter().amount() * getRateFactor(recipe));
        // Nie mehr erzeugen, als in den Tank passt (sonst ginge die Operation verloren).
        amount = Math.min(amount, fluidTank.getCapacity(0));
        if (amount < 1) amount = 1;
        if (amount > Integer.MAX_VALUE) amount = Integer.MAX_VALUE;
        return recipe.outputAntimatter().withAmount((int) amount);
    }

    /**
     * Mesh-Verschleiss pro Operation, skaliert mit der FE/t-Rate.
     * Wird immer aufgerundet, damit auch kleine Raten das Mesh mindestens 1 Punkt kosten.
     */
    public int getScaledMeshDurabilityCost(@Nullable PositronExtractionRecipe recipe) {
        if (recipe == null) return 1;
        long cost = (long) Math.ceil(recipe.meshDurabilityCost() * getRateFactor(recipe));
        if (cost < 1) cost = 1;
        if (cost > Integer.MAX_VALUE) cost = Integer.MAX_VALUE;
        return (int) cost;
    }

    public int getCurrentRecipeProcessTime() {
        PositronExtractionRecipe recipe = currentRecipe != null ? currentRecipe : baseRecipe;
        return recipe != null ? recipe.processTime() : 0;
    }

    /** Produktion pro Tick in mB (fuer die GUI-Anzeige, z.B. 4000 -> "4 B/t"). */
    public double getProductionPerTick() {
        PositronExtractionRecipe recipe = currentRecipe != null ? currentRecipe : baseRecipe;
        if (recipe == null || recipe.outputAntimatter().isEmpty()) return 0;
        return recipe.outputAntimatter().amount() * getRateFactor(recipe)
                / (double) Math.max(1, recipe.processTime());
    }

    public boolean isHologramEnabled() {
        if (level != null && level.isClientSide) {
            if (de.wutzuket.overdrive.Overdrive.isClientHologramEnabled(worldPosition)) return true;
        }
        return hologramEnabled;
    }

    public void toggleHologram() {
        this.hologramEnabled = !this.hologramEnabled;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.electron_gun");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new de.wutzuket.overdrive.menu.ElectronGunMenu(id, inv, this);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
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
    @Override public boolean canReceive() { return true; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putInt("energyRate", energyRate);
        tag.putBoolean("active", active);
        tag.putBoolean("hologramEnabled", hologramEnabled);
        tag.putInt("progressTicks", progressTicks);
        tag.putFloat("vacuumLevel", vacuumLevel);
        CompoundTag fluidTag = new CompoundTag();
        fluidTank.saveToNBT(fluidTag);
        tag.put("fluid", fluidTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        energyRate = tag.contains("energyRate") ? tag.getInt("energyRate") : DEFAULT_ENERGY_RATE;
        energyRate = Math.max(1, Math.min(MAX_ENERGY_RATE, energyRate));
        active = tag.getBoolean("active");
        hologramEnabled = tag.getBoolean("hologramEnabled");
        progressTicks = tag.getInt("progressTicks");
        vacuumLevel = tag.getFloat("vacuumLevel");
        if (tag.contains("fluid")) fluidTank.loadFromNBT(tag.getCompound("fluid"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}

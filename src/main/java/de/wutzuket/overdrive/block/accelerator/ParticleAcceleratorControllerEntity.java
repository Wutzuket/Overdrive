package de.wutzuket.overdrive.block.accelerator;

import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.menu.AcceleratorControllerMenu;
import de.wutzuket.overdrive.config.ODConfig;
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
import net.minecraft.world.level.block.Block;
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
    // Energie-Puffer als long (IEnergyStorage bleibt int-basiert): ~10 s Vollast bei Default-Rate.
    public static final long MAX_ENERGY_STORED = 50_000_000_000L;
    // Max. einstellbare Rate (Deckel, int).
    public static final int MAX_ENERGY_RATE = 800_000_000;
    public static final int OUTER_RADIUS = 25;
    // Output pro Op: 14.000 mB Antiprotonen (Rezept) -> interner Puffer ~35 Ops
    private static final int TANK_CAPACITY = 500_000;

    private long energy = 0;
    // GUI-setzbarer Soll-Verbrauch in FE/t (Default = Basis-Rezept 250 MFE/t = 20 B/t Output).
    // Skaliert Output + Iridium-Input linear: Faktor = energyRate / Rezeptkosten (bei process_time 1).
    public static final int DEFAULT_ENERGY_RATE = 250_000_000;
    private int energyRate = DEFAULT_ENERGY_RATE;
    // Output-Tank fuer Antiprotonen; fluessiges Iridium wird direkt aus dem Iridium-Target-Tank verbraucht
    private final FluidTank fluidTank = new FluidTank(TANK_CAPACITY);
    private boolean validStructure = false;
    private boolean hologramEnabled = false;
    private boolean active = false;
    private int progressTicks = 0;
    /** Angesammelte Output-Bruchteile (in mB) bei niedriger Rate: lädt auf, bis ein ganzes Quantum fertig ist. */
    private double partialOutput = 0;
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
            iridiumTargetPos = findTargetPos(level, pos, state);
            outputPos = findOutputPos(level, pos, state);
            IridiumTargetEntity t = getTargetEntity(level);
            currentRecipe = (t == null) ? null : findRecipe(level, t.getTank().getFluid());
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

        // Gate: eine Tick-Ration des eingestellten Verbrauchs muss gepuffert sein
        if (energy < energyRate) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        // Lademodus bei niedriger Rate: exakte Menge pro Tick wäre ein Bruchteil von 1 mB.
        // Statt aufzurunden wird Energie weiter verbraucht und angesammelt, bis ein ganzes
        // Quantum (Rezeptverhältnis gekürzt, z.B. 20 mB Output ↔ 1 mB Iridium) fertig ist.
        boolean fractional = isFractionalOp(currentRecipe);
        int[] quantum = fractional ? chargeQuantum(currentRecipe) : new int[]{1, 1};

        // Gate: genug fluessiges Iridium im Target-Tank (ganzes Quantum im Lademodus)
        int needIridium = fractional ? quantum[0] : getScaledInputAmount(currentRecipe);
        FluidStack targetFluid = target.getTank().getFluid();
        if (targetFluid.isEmpty() || !targetFluid.is(currentRecipe.inputFluid().getFluid()) || targetFluid.getAmount() < needIridium) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        FluidStack roomCheck = fractional
                ? currentRecipe.outputFluid().copyWithAmount(quantum[1])
                : getScaledOutput(currentRecipe);
        if (!currentRecipe.outputFluid().isEmpty() && !canOutputAcceptFluid(level, roomCheck)) {
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

        energy -= Math.min(energy, energyRate);

        if (fractional) {
            chargePartial(serverLevel, pos, state, target, quantum);
        } else {
            progressTicks++;

            if (progressTicks >= currentRecipe.processTime()) {
                completeProcessing(serverLevel, pos, state, target);
            }
        }

        pushFluidToOutput(level);

        setChanged();
        level.sendBlockUpdated(pos, state, state, 2);
    }

    /** Skalierfaktor aus GUI-Rate vs. Rezeptkosten: 250 MFE/t = 1x (20 B/t), 125 MFE/t = 0.5x (10 B/t). */
    public double getRateFactor(@Nullable CollisionRecipe recipe) {
        if (recipe == null || recipe.energyCost() <= 0) return 1.0;
        long base = (long) recipe.energyCost() * Math.max(1, recipe.processTime());
        if (base <= 0) return 1.0;
        return (double) ((long) energyRate * Math.max(1, recipe.processTime())) / base;
    }

    public FluidStack getScaledOutput(@Nullable CollisionRecipe recipe) {
        if (recipe == null || recipe.outputFluid().isEmpty()) return FluidStack.EMPTY;
        long amount = (long) (recipe.outputFluid().getAmount() * getRateFactor(recipe));
        if (amount < 1) amount = 1;
        if (amount > Integer.MAX_VALUE) amount = Integer.MAX_VALUE;
        return recipe.outputFluid().copyWithAmount((int) amount);
    }

    /** Skalierter Iridium-Input pro Op in mB (Faktor aus GUI-Rate vs. Rezeptkosten). */
    public int getScaledInputAmount(@Nullable CollisionRecipe recipe) {
        if (recipe == null || recipe.inputFluid().isEmpty()) return 1;
        long amount = (long) (recipe.inputFluid().getAmount() * getRateFactor(recipe));
        if (amount < 1) amount = 1;
        if (amount > Integer.MAX_VALUE) amount = Integer.MAX_VALUE;
        return (int) amount;
    }

    /** Exakte Output-Menge pro Tick als double (ohne Aufrunden, kann < 1 sein). */
    public double exactOutputPerTick(@Nullable CollisionRecipe recipe) {
        if (recipe == null || recipe.outputFluid().isEmpty()) return 0;
        return recipe.outputFluid().getAmount() * getRateFactor(recipe);
    }

    /** Exakte Iridium-Menge pro Tick als double (ohne Aufrunden, kann < 1 sein). */
    public double exactInputPerTick(@Nullable CollisionRecipe recipe) {
        if (recipe == null || recipe.inputFluid().isEmpty()) return 0;
        return recipe.inputFluid().getAmount() * getRateFactor(recipe);
    }

    /** True, wenn pro Tick keine ganzen mB anfallen wuerden -> Lademodus statt Aufrunden. */
    public boolean isFractionalOp(@Nullable CollisionRecipe recipe) {
        if (recipe == null || recipe.outputFluid().isEmpty()
                || recipe.inputFluid().isEmpty() || recipe.outputFluid().getAmount() <= 0
                || recipe.inputFluid().getAmount() <= 0) return false;
        return exactOutputPerTick(recipe) < 1.0 || exactInputPerTick(recipe) < 1.0;
    }

    private static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return Math.max(1, a);
    }

    /** Ganzzahliges Mindest-Quantum {iridiumMb, outputMb} aus dem gekuerzten Rezeptverhaeltnis. */
    public int[] chargeQuantum(@Nullable CollisionRecipe recipe) {
        if (recipe == null || recipe.inputFluid().isEmpty() || recipe.outputFluid().isEmpty()) {
            return new int[]{1, 1};
        }
        int g = gcd(recipe.inputFluid().getAmount(), recipe.outputFluid().getAmount());
        return new int[]{Math.max(1, recipe.inputFluid().getAmount() / g),
                Math.max(1, recipe.outputFluid().getAmount() / g)};
    }

    /** Sammelt Output-Bruchteile; vollendet ganze Quanten, sobald Iridium + Output-Platz reichen. */
    private void chargePartial(ServerLevel level, BlockPos pos, BlockState state, IridiumTargetEntity target, int[] quantum) {
        if (currentRecipe == null) return;
        int needIn = Math.max(1, quantum[0]);
        int makeOut = Math.max(1, quantum[1]);
        partialOutput += exactOutputPerTick(currentRecipe);

        while (partialOutput >= makeOut) {
            FluidStack chunk = currentRecipe.outputFluid().copyWithAmount(makeOut);
            int canFill = fluidTank.fill(chunk.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (canFill < makeOut) break;
            FluidStack drained = target.getTank().drain(needIn, IFluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() < needIn || !drained.is(currentRecipe.inputFluid().getFluid())) break;
            target.getTank().drain(needIn, IFluidHandler.FluidAction.EXECUTE);
            int filled = fluidTank.fill(chunk.copy(), IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                spawnOutputParticles(level, pos, state);
            }
            partialOutput -= makeOut;
            progressTicks = 0;
        }

        if (!target.hasValidBlock()) {
            active = false;
            currentRecipe = null;
        }

        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    /** Lade-Fortschritt 0..200 fuer den Fortschrittsbalken (0 ausserhalb des Lademodus). */
    public int getChargeScaled() {
        if (currentRecipe == null || !isFractionalOp(currentRecipe)) return 0;
        int[] quantum = chargeQuantum(currentRecipe);
        if (quantum[1] <= 0) return 0;
        double f = partialOutput / quantum[1];
        if (f < 0) f = 0;
        if (f > 1) f = 1;
        return (int) (f * 200);
    }

    /** Bereits geladene Ticks im Lademodus (fuer Tooltip; sonst progressTicks). */
    public int getChargeTicksElapsed() {
        if (currentRecipe == null || !isFractionalOp(currentRecipe)) return progressTicks;
        double perTick = exactOutputPerTick(currentRecipe);
        if (perTick <= 0) return 0;
        long t = (long) (partialOutput / perTick);
        if (t < 0) t = 0;
        if (t > Integer.MAX_VALUE) t = Integer.MAX_VALUE;
        return (int) t;
    }

    /** Gesamt-Ticks bis zum fertigen Quantum im Lademodus (fuer Tooltip; sonst processTime). */
    public int getChargeTicksTotal() {
        if (currentRecipe == null || !isFractionalOp(currentRecipe)) {
            return currentRecipe == null ? 1 : Math.max(1, currentRecipe.processTime());
        }
        double perTick = exactOutputPerTick(currentRecipe);
        if (perTick <= 0) return 1;
        int[] quantum = chargeQuantum(currentRecipe);
        long t = (long) Math.ceil(quantum[1] / perTick);
        if (t < 1) t = 1;
        if (t > Integer.MAX_VALUE) t = Integer.MAX_VALUE;
        return (int) t;
    }

    public int getEnergyRate() { return energyRate; }

    public void setEnergyRate(int rate) {
        energyRate = Math.max(1, Math.min(MAX_ENERGY_RATE, rate));
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private void completeProcessing(ServerLevel level, BlockPos pos, BlockState state, IridiumTargetEntity target) {
        if (currentRecipe == null) return;

        FluidStack output = getScaledOutput(currentRecipe);
        if (!output.isEmpty()) {
            int canFill = fluidTank.fill(output.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (canFill < output.getAmount()) {
                return;
            }
        }

        // Iridium aus dem Target-Tank verbrauchen (SIMULATE pruefen, dann EXECUTE)
        int needIridium = getScaledInputAmount(currentRecipe);
        FluidStack drained = target.getTank().drain(needIridium, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() < needIridium || !drained.is(currentRecipe.inputFluid().getFluid())) return;
        target.getTank().drain(needIridium, IFluidHandler.FluidAction.EXECUTE);

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

    private @Nullable CollisionRecipe findRecipe(Level level, FluidStack available) {
        if (available.isEmpty()) return null;

        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof CollisionRecipe recipe && recipe.getType() == ODRecipeTypes.COLLISION.get()) {
                if (!recipe.inputFluid().isEmpty() && available.is(recipe.inputFluid().getFluid())) {
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
        if (!de.wutzuket.overdrive.config.ODConfig.ACCELERATOR_EXPLODE_ON_FAILURE.get()) {
            active = false;
            progressTicks = 0;
            validStructure = false;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        // Multiple small explosions along the ring
        int explosionCount = 12;
        for (int i = 0; i < explosionCount; i++) {
            double angle = 2.0 * Math.PI * i / explosionCount;
            double rx = OUTER_RADIUS * Math.cos(angle);
            double rz = OUTER_RADIUS * Math.sin(angle);
            double ex = cx + rx + 0.5;
            double ez = cz + rz + 0.5;
            level.explode(null, ex, cy + 0.5, ez, 4.0f, Level.ExplosionInteraction.TNT);
        }

        // Destroy ring blocks - bottom layer (y-1) mostly survives, middle (y) partially, top (y+1) mostly destroyed
        int minX = cx - OUTER_RADIUS - 2;
        int maxX = cx + OUTER_RADIUS + 2;
        int minZ = cz - OUTER_RADIUS - 2;
        int maxZ = cz + OUTER_RADIUS + 2;

        double bottomChance = de.wutzuket.overdrive.config.ODConfig.ACCELERATOR_BOTTOM_DESTROY_CHANCE.get();
        double middleChance = de.wutzuket.overdrive.config.ODConfig.ACCELERATOR_MIDDLE_DESTROY_CHANCE.get();
        double topChance = de.wutzuket.overdrive.config.ODConfig.ACCELERATOR_TOP_DESTROY_CHANCE.get();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = x - cx;
                double dz = z - cz;
                double dist = Math.sqrt(dx * dx + dz * dz);

                boolean inRing = dist >= OUTER_RADIUS - 3.5 && dist <= OUTER_RADIUS + 0.5;
                if (!inRing) continue;

                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos blockPos = new BlockPos(x, cy + dy, z);
                    BlockState blockState = level.getBlockState(blockPos);
                    Block block = blockState.getBlock();
                    if (block instanceof AcceleratorCasing
                            || block instanceof IridiumTargetBlock
                            || block instanceof AcceleratorOutputBlock
                            || block instanceof AcceleratorEnergyInputPortBlock
                            || block instanceof AcceleratorVacuumInputPortBlock) {
                        double destroyChance;
                        if (dy == -1) {
                            destroyChance = bottomChance;
                        } else if (dy == 0) {
                            destroyChance = middleChance;
                        } else {
                            destroyChance = topChance;
                        }
                        if (level.random.nextDouble() < destroyChance) {
                            level.destroyBlock(blockPos, true);
                        }
                    }
                }
            }
        }

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

        DustParticleOptions options = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 2.5f);
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
                    if (checkState.getBlock() instanceof AcceleratorCasing
                            || checkState.getBlock() instanceof IridiumTargetBlock
                            || checkState.getBlock() instanceof AcceleratorOutputBlock
                            || checkState.getBlock() instanceof AcceleratorEnergyInputPortBlock
                            || checkState.getBlock() instanceof AcceleratorVacuumInputPortBlock) {
                        level.sendParticles(options, x + 0.5, cy + dy + 0.5, z + 0.5, 3, 0.2, 0.2, 0.2, 0.05);
                    }
                }
            }
        }
        // Controller block itself
        level.sendParticles(options, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.05);
    }

    private void spawnAmbientParticles(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        DustParticleOptions options = new DustParticleOptions(new Vector3f(1.0f, 0.2f, 0.0f), 2f);
        for (int i = 0; i < 6; i++) {
            double angle = 2.0 * Math.PI * level.random.nextDouble();
            double rx = (OUTER_RADIUS - 1.5) * Math.cos(angle);
            double rz = (OUTER_RADIUS - 1.5) * Math.sin(angle);
            double px = cx + rx + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            double pz = cz + rz + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(options, px, cy + 0.5 + (level.random.nextDouble() - 0.5), pz, 2, 0, 0, 0, 0.1);
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

    public int getMaxRate() {
        return MAX_ENERGY_RATE;
    }

    public long getEnergyStoredLong() {
        return energy;
    }

    public long getMaxEnergyStoredLong() {
        return MAX_ENERGY_STORED;
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

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!validStructure) return 0;
        long accepted = Math.min((long) maxReceive, MAX_ENERGY_STORED - energy);
        if (!simulate) {
            energy += accepted;
            setChanged();
        }
        return (int) Math.min(accepted, maxReceive);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, energy);
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
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

    public FluidStack getOutputFluidForDisplay() {
        if (level != null) {
            BlockPos displayOutputPos = outputPos;
            if (displayOutputPos == null) {
                displayOutputPos = findOutputPos(level, worldPosition, getBlockState());
            }
            if (displayOutputPos != null
                    && level.getBlockEntity(displayOutputPos) instanceof AcceleratorOutputEntity output) {
                return output.getFluidTank().getFluid().copy();
            }
        }
        return fluidTank.getFluid().copy();
    }

    public int getOutputFluidCapacityForDisplay() {
        if (level != null) {
            BlockPos displayOutputPos = outputPos;
            if (displayOutputPos == null) {
                displayOutputPos = findOutputPos(level, worldPosition, getBlockState());
            }
            if (displayOutputPos != null
                    && level.getBlockEntity(displayOutputPos) instanceof AcceleratorOutputEntity output) {
                return output.getFluidTank().getCapacity();
            }
        }
        return fluidTank.getCapacity();
    }

    /** Target-Entity am Ring (null, wenn gerade keines gefunden). */
    public @Nullable IridiumTargetEntity getTarget() {
        if (level == null) return null;
        return getTargetEntity(level);
    }

    /** Sync-Helper fuer GUI/Tooltip: Iridium-Menge im Target-Tank (0 ohne Target). */
    public int getTargetIridiumAmount() {
        IridiumTargetEntity t = getTarget();
        return t == null ? 0 : t.getTank().getFluidAmount();
    }

    /** Sync-Helper fuer GUI/Tooltip: Kapazitaet des Target-Tanks. */
    public int getTargetIridiumCapacity() {
        IridiumTargetEntity t = getTarget();
        return t == null ? IridiumTargetEntity.TANK_CAPACITY : t.getTank().getCapacity();
    }

    /** Sync-Helper fuer GUI/Tooltip: Fluid im Target-Tank (EMPTY ohne Target). */
    public FluidStack getTargetIridiumFluid() {
        IridiumTargetEntity t = getTarget();
        return t == null ? FluidStack.EMPTY : t.getTank().getFluid().copy();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("energy", energy);
        tag.putInt("energyRate", energyRate);
        tag.putBoolean("validStructure", validStructure);
        tag.putBoolean("hologramEnabled", hologramEnabled);
        tag.putBoolean("active", active);
        tag.putInt("progressTicks", progressTicks);
        tag.put("fluid", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putDouble("partialOutput", partialOutput);
        // "inputFluid" aus aelteren Versionen wird ignoriert (Iridium liegt jetzt im Target-Tank).
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.get("energy") instanceof net.minecraft.nbt.NumericTag n ? n.getAsLong() : 0;
        energyRate = tag.contains("energyRate") ? tag.getInt("energyRate") : DEFAULT_ENERGY_RATE;
        if (energyRate < 1) energyRate = DEFAULT_ENERGY_RATE;
        validStructure = tag.getBoolean("validStructure");
        hologramEnabled = tag.getBoolean("hologramEnabled");
        active = tag.getBoolean("active");
        progressTicks = tag.getInt("progressTicks");
        if (tag.contains("fluid")) {
            fluidTank.readFromNBT(registries, tag.getCompound("fluid"));
        }
        partialOutput = tag.contains("partialOutput") ? Math.max(0, tag.getDouble("partialOutput")) : 0;
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

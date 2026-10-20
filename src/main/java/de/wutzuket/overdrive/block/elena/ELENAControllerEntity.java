package de.wutzuket.overdrive.block.elena;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.antimatter.IAntimatterHandler;
import de.wutzuket.overdrive.antimatter.InOutAntimatterHandler;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.IEnergyStorage;
import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.AntimatterStack;
import de.wutzuket.overdrive.antimatter.AntimatterTank;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ELENAControllerEntity extends BlockEntity implements IEnergyStorage, MenuProvider {
    // Energie-Puffer als long (IEnergyStorage bleibt int-basiert): ~10 s Vollast bei Default-Rate.
    public static final long MAX_ENERGY_STORED = 10_000_000_000L;
    // Max. einstellbare Rate (Deckel, int).
    public static final int MAX_ENERGY_RATE = 100_000_000;
    public static final int OUTER_RADIUS = 2;
    // Kettendurchsatz 14.000 mB/t (Max-Rate)
    private static final int TANK_CAPACITY = 500_000;

    private long energy = 0;
    // GUI-setzbarer Soll-Verbrauch in FE/t (Default = Basis-Rezept 50 MFE/t = 20 B/t Output).
    public static final int DEFAULT_ENERGY_RATE = 50_000_000;
    private int energyRate = DEFAULT_ENERGY_RATE;
    // Input-Tank (frueher ELENA-Input-Port) + Output-Tank (frueher ELENA-Output-Port): beide im Controller.
    private final AntimatterTank inputTank = new AntimatterTank(TANK_CAPACITY);
    private final AntimatterTank fluidTank = new AntimatterTank(TANK_CAPACITY);
    private IAntimatterHandler antimatterHandler;
    private boolean validStructure = false;
    private boolean hologramEnabled = false;
    private boolean active = false;
    private int progressTicks = 0;
    /** Angesammelte Output-Bruchteile (in mB) bei niedriger Rate: lädt auf, bis ein ganzes Quantum fertig ist. */
    private double partialOutput = 0;

    private @Nullable AntiprotonCoolingRecipe currentRecipe = null;
    /** Gecachtes Basisrezept der Maschine (fuer die Produktionsanzeige, unabhaengig vom Input-Tank). */
    private @Nullable AntiprotonCoolingRecipe baseRecipe = null;

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
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        if (currentRecipe == null) {
            currentRecipe = findRecipe(level);
        }

        if (currentRecipe == null) {
            active = false;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        Antimatter inputType = getInputAntimatter();
        // Lademodus bei niedriger Rate: exakte Menge pro Tick wäre ein Bruchteil von 1 mB.
        // Statt aufzurunden wird Energie weiter verbraucht und angesammelt (wie beim Accelerator).
        boolean fractional = isFractionalOp(currentRecipe);
        int[] quantum = fractional ? chargeQuantum(currentRecipe) : new int[]{1, 1};
        int needInput = fractional ? quantum[0] : getScaledInputAmount(currentRecipe);
        if (inputType == null || !inputType.equals(currentRecipe.inputAntimatter().type())
                || inputTank.getAmount(0) < needInput) {
            if (active) {
                active = false;
                progressTicks = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        AntimatterStack roomCheck = fractional
                ? currentRecipe.outputAntimatter().withAmount(quantum[1])
                : getScaledOutput(currentRecipe);
        if (!finalOutputCanAccept(roomCheck)) {
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

        if (!active) {
            active = true;
            progressTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        energy -= Math.min(energy, energyRate);

        if (fractional) {
            chargePartial(serverLevel, pos, state, quantum);
        } else {
            progressTicks++;

            if (progressTicks >= currentRecipe.processTime()) {
                completeProcessing(serverLevel, pos, state);
            }
        }

        setChanged();
        level.sendBlockUpdated(pos, state, state, 2);
    }

    /** Skalierfaktor aus GUI-Rate vs. Rezeptkosten: 50 MFE/t = 1x (20 B/t), 25 MFE/t = 0.5x (10 B/t). */
    public double getRateFactor(@Nullable AntiprotonCoolingRecipe recipe) {
        if (recipe == null || recipe.energyCost() <= 0) return 1.0;
        long base = (long) recipe.energyCost() * Math.max(1, recipe.processTime());
        if (base <= 0) return 1.0;
        return (double) ((long) energyRate * Math.max(1, recipe.processTime())) / base;
    }

    /**
     * Produktion pro Tick in mB (fuer die GUI-Anzeige, z.B. 20000 -&gt; "20 B/t").
     * Nutzt das aktive Rezept, sonst das Basisrezept der Maschine, damit die Rate auch
     * ohne Input im Tank angezeigt wird.
     */
    public double getProductionPerTick() {
        AntiprotonCoolingRecipe recipe = currentRecipe != null ? currentRecipe : getBaseRecipe();
        if (recipe == null || recipe.outputAntimatter().isEmpty()) return 0;
        return recipe.outputAntimatter().amount() * getRateFactor(recipe) / Math.max(1, recipe.processTime());
    }

    /** Das (erste) ELENA-Rezept der Maschine, unabhaengig vom Inhalt des Input-Tanks. */
    private @Nullable AntiprotonCoolingRecipe getBaseRecipe() {
        if (baseRecipe != null) return baseRecipe;
        if (level == null) return null;
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof AntiprotonCoolingRecipe recipe
                    && recipe.getType() == ODRecipeTypes.ELENA_RECIPE.get()) {
                baseRecipe = recipe;
                return recipe;
            }
        }
        return null;
    }


    public AntimatterStack getScaledOutput(@Nullable AntiprotonCoolingRecipe recipe) {
        if (recipe == null || recipe.outputAntimatter().isEmpty()) return new AntimatterStack(null, 0);
        long amount = (long) (recipe.outputAntimatter().amount() * getRateFactor(recipe));
        if (amount < 1) amount = 1;
        if (amount > Integer.MAX_VALUE) amount = Integer.MAX_VALUE;
        return recipe.outputAntimatter().withAmount((int) amount);
    }

    /** Skalierter Fluid-Input pro Op in mB (Faktor aus GUI-Rate vs. Rezeptkosten). */
    public int getScaledInputAmount(@Nullable AntiprotonCoolingRecipe recipe) {
        if (recipe == null || recipe.inputAntimatter().isEmpty()) return 1;
        long amount = (long) (recipe.inputAntimatter().amount() * getRateFactor(recipe));
        if (amount < 1) amount = 1;
        if (amount > Integer.MAX_VALUE) amount = Integer.MAX_VALUE;
        return (int) amount;
    }

    /** Exakte Output-Menge pro Tick als double (ohne Aufrunden, kann < 1 sein). */
    public double exactOutputPerTick(@Nullable AntiprotonCoolingRecipe recipe) {
        if (recipe == null || recipe.outputAntimatter().isEmpty()) return 0;
        return recipe.outputAntimatter().amount() * getRateFactor(recipe);
    }

    /** Exakte Input-Menge pro Tick als double (ohne Aufrunden, kann < 1 sein). */
    public double exactInputPerTick(@Nullable AntiprotonCoolingRecipe recipe) {
        if (recipe == null || recipe.inputAntimatter().isEmpty()) return 0;
        return recipe.inputAntimatter().amount() * getRateFactor(recipe);
    }

    /** True, wenn pro Tick keine ganzen mB anfallen wuerden -> Lademodus statt Aufrunden. Items (Cells) sind nicht teilbar. */
    public boolean isFractionalOp(@Nullable AntiprotonCoolingRecipe recipe) {
        if (recipe == null || recipe.outputAntimatter().isEmpty()
                || recipe.inputAntimatter().isEmpty() || recipe.outputAntimatter().amount() <= 0
                || recipe.inputAntimatter().amount() <= 0 || !recipe.outputCell().isEmpty()) return false;
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

    /** Ganzzahliges Mindest-Quantum {inputMb, outputMb} aus dem gekuerzten Rezeptverhaeltnis. */
    public int[] chargeQuantum(@Nullable AntiprotonCoolingRecipe recipe) {
        if (recipe == null || recipe.inputAntimatter().isEmpty() || recipe.outputAntimatter().isEmpty()) {
            return new int[]{1, 1};
        }
        int g = gcd(recipe.inputAntimatter().amount(), recipe.outputAntimatter().amount());
        return new int[]{Math.max(1, recipe.inputAntimatter().amount() / g),
                Math.max(1, recipe.outputAntimatter().amount() / g)};
    }

    /** Sammelt Output-Bruchteile aus dem Input-Tank; vollendet ganze Quanten, sobald Tank + Output-Platz reichen. */
    private void chargePartial(ServerLevel level, BlockPos pos, BlockState state, int[] quantum) {
        if (currentRecipe == null) return;
        int needIn = Math.max(1, quantum[0]);
        int makeOut = Math.max(1, quantum[1]);
        partialOutput += exactOutputPerTick(currentRecipe);

        while (partialOutput >= makeOut) {
            AntimatterStack chunk = currentRecipe.outputAntimatter().withAmount(makeOut);
            int canFill = fluidTank.insertAntimatter(0, chunk.type(), makeOut, true);
            if (canFill < makeOut) break;
            int drained = inputTank.extractAntimatter(0, needIn, true);
            if (drained < needIn) break;
            inputTank.extractAntimatter(0, needIn, false);
            int filled = fluidTank.insertAntimatter(0, chunk.type(), makeOut, false);
            if (filled > 0) {
                spawnOutputParticles(level, pos, state);
            }
            partialOutput -= makeOut;
            progressTicks = 0;
        }

        if (inputTank.getAmount(0) <= 0) {
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

    private void completeProcessing(ServerLevel level, BlockPos pos, BlockState state) {
        if (currentRecipe == null) return;

        AntimatterStack output = getScaledOutput(currentRecipe);
        int needIn = getScaledInputAmount(currentRecipe);

        int simDrained = inputTank.extractAntimatter(0, needIn, true);
        if (simDrained < needIn) return;

        if (!output.isEmpty()) {
            int canFill = fluidTank.insertAntimatter(0, output.type(), output.amount(), true);
            if (canFill < output.amount()) return;
        }

        inputTank.extractAntimatter(0, needIn, false);
        if (!output.isEmpty()) {
            fluidTank.insertAntimatter(0, output.type(), output.amount(), false);
        }

        progressTicks = 0;
        currentRecipe = null;
        spawnOutputParticles(level, pos, state);
        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private Antimatter getInputAntimatter() {
        return inputTank.getAntimatterInTank(0);
    }

    private boolean finalOutputCanAccept(AntimatterStack output) {
        if (currentRecipe == null) return false;
        if (output.isEmpty()) return true;
        int canFill = fluidTank.insertAntimatter(0, output.type(), output.amount(), true);
        return canFill >= output.amount();
    }

    private @Nullable AntiprotonCoolingRecipe findRecipe(Level level) {
        Antimatter inputType = getInputAntimatter();
        if (inputType == null) return null;

        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof AntiprotonCoolingRecipe recipe && recipe.getType() == ODRecipeTypes.ELENA_RECIPE.get()) {
                if (recipe.inputAntimatter().type().equals(inputType)) {
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
        if (!de.wutzuket.overdrive.config.ODConfig.ELENA_EXPLODE_ON_FAILURE.get()) {
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
            level.explode(null, ex, cy + 0.5, ez, 3.0f, Level.ExplosionInteraction.TNT);
        }

        // Destroy ring blocks - tiered destruction
        int minX = cx - OUTER_RADIUS;
        int maxX = cx + OUTER_RADIUS;
        int minZ = cz - OUTER_RADIUS;
        int maxZ = cz + OUTER_RADIUS;

        double bottomChance = de.wutzuket.overdrive.config.ODConfig.ELENA_BOTTOM_DESTROY_CHANCE.get();
        double middleChance = de.wutzuket.overdrive.config.ODConfig.ELENA_MIDDLE_DESTROY_CHANCE.get();
        double topChance = de.wutzuket.overdrive.config.ODConfig.ELENA_TOP_DESTROY_CHANCE.get();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int maxDist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                boolean inRing = maxDist >= 1 && maxDist <= OUTER_RADIUS;
                if (!inRing) continue;

                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos blockPos = new BlockPos(x, cy + dy, z);
                    BlockState blockState = level.getBlockState(blockPos);
                    Block block = blockState.getBlock();
                    if (block instanceof ELENACasingBlock
                            || block instanceof ELENAEnergyInputPortBlock
                            || block instanceof ELENAVacuumPortBlock
                            || block instanceof ELENAInputPortBlock
                            || block instanceof ELENAOutputPortBlock) {
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

    private void spawnValidationParticles(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        DustParticleOptions options = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 2.5f);
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
                    if (checkState.getBlock() instanceof ELENACasingBlock
                            || checkState.getBlock() instanceof ELENAEnergyInputPortBlock
                            || checkState.getBlock() instanceof ELENAVacuumPortBlock
                            || checkState.getBlock() instanceof ELENAInputPortBlock
                            || checkState.getBlock() instanceof ELENAOutputPortBlock) {
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
            double rx = (OUTER_RADIUS - 0.5) * Math.cos(angle);
            double rz = (OUTER_RADIUS - 0.5) * Math.sin(angle);
            double px = cx + rx + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            double pz = cz + rz + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(options, px, cy + 0.5 + (level.random.nextDouble() - 0.5), pz, 2, 0, 0, 0, 0.1);
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
    public long getEnergyStoredLong() { return energy; }
    public long getMaxEnergyStoredLong() { return MAX_ENERGY_STORED; }
    public int getMaxRate() { return MAX_ENERGY_RATE; }
    public AntimatterTank getFluidTank() { return fluidTank; }

    public AntimatterTank getInputTank() { return inputTank; }

    /** @return the antimatter handler of the controller, the input and the output tank. */
    public IAntimatterHandler getAntimatterHandler() {
        if (antimatterHandler == null) {
            antimatterHandler = new InOutAntimatterHandler(inputTank, fluidTank);
        }
        return antimatterHandler;
    }

    /** Sucht den Controller in der Nähe (fuer Port-Weiterleitungen ohne eigene BE). */
    public static @Nullable ELENAControllerEntity findNear(Level level, BlockPos pos) {
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockEntity be = level.getBlockEntity(pos.offset(dx, dy, dz));
                    if (be instanceof ELENAControllerEntity ctrl) return ctrl;
                }
            }
        }
        return null;
    }

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
        long accepted = Math.min((long) maxReceive, MAX_ENERGY_STORED - energy);
        if (!simulate) {
            energy += accepted;
            setChanged();
        }
        return (int) Math.min(accepted, maxReceive);
    }

    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
    @Override public int getEnergyStored() { return (int) Math.min(Integer.MAX_VALUE, energy); }
    @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
    @Override public boolean canExtract() { return false; }
    @Override public boolean canReceive() { return validStructure; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("energy", energy);
        tag.putInt("energyRate", energyRate);
        tag.putBoolean("validStructure", validStructure);
        tag.putBoolean("hologramEnabled", hologramEnabled);
        tag.putBoolean("active", active);
        tag.putInt("progressTicks", progressTicks);
        tag.putDouble("partialOutput", partialOutput);
        CompoundTag fluidTag = new CompoundTag();
        fluidTank.saveToNBT(fluidTag);
        tag.put("fluid", fluidTag);
        CompoundTag inputTag = new CompoundTag();
        inputTank.saveToNBT(inputTag);
        tag.put("inputFluid", inputTag);
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
        partialOutput = tag.contains("partialOutput") ? Math.max(0, tag.getDouble("partialOutput")) : 0;
        if (tag.contains("fluid")) fluidTank.loadFromNBT(tag.getCompound("fluid"));
        if (tag.contains("inputFluid")) inputTank.loadFromNBT(tag.getCompound("inputFluid"));
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

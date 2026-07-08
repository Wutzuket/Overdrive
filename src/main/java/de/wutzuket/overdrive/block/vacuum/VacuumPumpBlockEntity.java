package de.wutzuket.overdrive.block.vacuum;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.menu.VacuumPumpMenu;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class VacuumPumpBlockEntity extends BlockEntity implements IEnergyStorage, MenuProvider, IVacuumNode {
    private static final int MAX_ENERGY = 200_000;
    private static final int ENERGY_PER_TICK = 100;

    public enum VacuumStatus {
        OFFLINE,
        EVACUATING,
        STABLE,
        LEAK
    }

    private int energy = 0;
    private boolean validStructure = false;
    private boolean hologramEnabled = false;
    private VacuumStatus status = VacuumStatus.OFFLINE;
    private int vacuumProgress = 0;
    private int evacTicks = 0;
    private int pipeCount = 0;
    private BlockPos outputPortPos = null;
    private boolean particlesSpawned = false;

    private int getEvacTime() {
        return getVolume() * 200 / 27;
    }

    public VacuumPumpBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.VACUUM_PUMP_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean wasValid = validStructure;
        int prevPipeCount = pipeCount;
        validStructure = checkStructure(level, pos, state);
        boolean structureChanged = wasValid != validStructure;

        if (structureChanged && validStructure) {
            status = VacuumStatus.EVACUATING;
            vacuumProgress = 0;
            evacTicks = 0;
            particlesSpawned = false;
            spawnSuccessParticles(level, pos);
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        } else if (structureChanged) {
            status = VacuumStatus.OFFLINE;
            vacuumProgress = 0;
            evacTicks = 0;
            pipeCount = 0;
            outputPortPos = null;
            particlesSpawned = false;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        } else if (pipeCount != prevPipeCount) {
            if (status == VacuumStatus.STABLE) {
                int newEvacTime = getEvacTime();
                if (pipeCount > prevPipeCount) {
                    int prevVolume = 27 + prevPipeCount;
                    int oldEvacTime = prevVolume * 200 / 27;
                    status = VacuumStatus.EVACUATING;
                    evacTicks = oldEvacTime;
                    vacuumProgress = Math.min(100, (evacTicks * 100) / newEvacTime);
                } else {
                    evacTicks = newEvacTime;
                }
            }
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (!validStructure) {
            if (status == VacuumStatus.OFFLINE && energy > 0) {
                energy = Math.max(0, energy - ENERGY_PER_TICK);
                setChanged();
            }
            pushVacuumToPort(level, 0);
            return;
        }

        boolean leaking = detectLeak(level, pos, state);

        if (leaking && status != VacuumStatus.LEAK) {
            status = VacuumStatus.LEAK;
            vacuumProgress = 0;
            evacTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (!leaking && status == VacuumStatus.LEAK) {
            status = VacuumStatus.EVACUATING;
            vacuumProgress = 0;
            evacTicks = 0;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (status == VacuumStatus.EVACUATING || status == VacuumStatus.LEAK) {
            if (energy >= ENERGY_PER_TICK) {
                energy -= ENERGY_PER_TICK;
                if (status == VacuumStatus.EVACUATING) {
                    evacTicks++;
                    int evacTime = getEvacTime();
                    if (evacTicks >= evacTime) {
                        status = VacuumStatus.STABLE;
                        vacuumProgress = 100;
                        evacTicks = evacTime;
                    } else {
                        vacuumProgress = Math.min(100, (evacTicks * 100) / evacTime);
                    }
                }
                pushVacuumToPort(level, getVacuumLevel());
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            } else {
                pushVacuumToPort(level, 0);
            }
        } else if (status == VacuumStatus.STABLE) {
            if (energy < ENERGY_PER_TICK) {
                status = VacuumStatus.OFFLINE;
                vacuumProgress = 0;
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            } else {
                energy -= ENERGY_PER_TICK / 10;
                pushVacuumToPort(level, getVacuumLevel());
                setChanged();
            }
        }
    }

    private void pushVacuumToPort(Level level, float vacuum) {
        if (outputPortPos == null) return;
        BlockEntity be = level.getBlockEntity(outputPortPos);
        if (be instanceof IVacuumNode node) {
            node.setVacuumLevel(vacuum);
        }
    }

    private void spawnSuccessParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            DustParticleOptions options = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f);
            for (int i = 0; i < 30; i++) {
                double dx = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 3;
                double dy = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 3;
                double dz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 3;
                serverLevel.sendParticles(options, dx, dy, dz, 1, 0, 0, 0, 0.1);
            }
        }
    }

    private boolean checkStructure(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction back = facing.getOpposite();

        BlockPos foundOutputPort = null;
        int portCount = 0;
        int pipes = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    BlockPos checkPos;
                    if (back == Direction.NORTH) checkPos = pos.offset(dx, dy, -dz);
                    else if (back == Direction.SOUTH) checkPos = pos.offset(dx, dy, dz);
                    else if (back == Direction.WEST) checkPos = pos.offset(-dz, dy, dx);
                    else checkPos = pos.offset(dz, dy, dx);

                    if (checkPos.equals(pos)) continue;

                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.isAir()) return false;

                    if (checkState.getBlock() instanceof EnergyInputPortBlock) {
                        portCount++;
                    } else if (checkState.getBlock() instanceof VacuumOutputPortBlock) {
                        portCount++;
                        foundOutputPort = checkPos;
                    } else if (checkState.getBlock() instanceof VacuumPipeBlock) {
                        pipes++;
                    } else if (!(checkState.getBlock() instanceof PumpCasingBlock)) {
                        return false;
                    }
                }
            }
        }
        outputPortPos = foundOutputPort;
        pipeCount = pipes + countExternalPipes(level);
        return portCount == 2;
    }

    private int countExternalPipes(Level level) {
        if (outputPortPos == null) return 0;
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction back = facing.getOpposite();

        Set<BlockPos> structurePositions = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    BlockPos checkPos;
                    if (back == Direction.NORTH) checkPos = worldPosition.offset(dx, dy, -dz);
                    else if (back == Direction.SOUTH) checkPos = worldPosition.offset(dx, dy, dz);
                    else if (back == Direction.WEST) checkPos = worldPosition.offset(-dz, dy, dx);
                    else checkPos = worldPosition.offset(dz, dy, dx);
                    structurePositions.add(checkPos);
                }
            }
        }

        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = outputPortPos.relative(dir);
            if (!structurePositions.contains(neighbor)) {
                BlockState neighborState = level.getBlockState(neighbor);
                if (neighborState.getBlock() instanceof VacuumPipeBlock && visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        int count = 0;
        int maxSteps = 1000;
        while (!queue.isEmpty() && count < maxSteps) {
            BlockPos current = queue.poll();
            count++;
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!structurePositions.contains(neighbor) && visited.add(neighbor)) {
                    if (level.getBlockState(neighbor).getBlock() instanceof VacuumPipeBlock) {
                        queue.add(neighbor);
                    }
                }
            }
        }
        return count;
    }

    private boolean detectLeak(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction back = facing.getOpposite();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    BlockPos checkPos;
                    if (back == Direction.NORTH) checkPos = pos.offset(dx, dy, -dz);
                    else if (back == Direction.SOUTH) checkPos = pos.offset(dx, dy, dz);
                    else if (back == Direction.WEST) checkPos = pos.offset(-dz, dy, dx);
                    else checkPos = pos.offset(dz, dy, dx);

                    if (checkPos.equals(pos)) continue;

                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof VacuumOutputPortBlock) {
                        boolean hasExternalConnection = false;
                        for (Direction dir : Direction.values()) {
                            BlockPos neighbor = checkPos.relative(dir);
                            if (neighbor.equals(pos)) continue;
                            BlockState neighborState = level.getBlockState(neighbor);
                            Block neighborBlock = neighborState.getBlock();
                            if (neighborBlock instanceof PumpCasingBlock) continue;
                            if (neighborBlock instanceof EnergyInputPortBlock) continue;
                            if (neighborBlock instanceof VacuumOutputPortBlock) continue;
                            if (!neighborState.isAir()) {
                                hasExternalConnection = true;
                                break;
                            }
                        }
                        if (!hasExternalConnection) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isValidStructure() {
        return validStructure;
    }

    public boolean isHologramEnabled() {
        return hologramEnabled;
    }

    public void setHologramEnabled(boolean enabled) {
        this.hologramEnabled = enabled;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public VacuumStatus getStatus() {
        return status;
    }

    public int getVacuumProgress() {
        return vacuumProgress;
    }

    public int getVolume() {
        return 27 + pipeCount;
    }

    public int getEnergyUsage() {
        if (status == VacuumStatus.EVACUATING || status == VacuumStatus.LEAK) return ENERGY_PER_TICK;
        if (status == VacuumStatus.STABLE) return ENERGY_PER_TICK / 10;
        return 0;
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
        return true;
    }

    @Override
    public float getVacuumLevel() {
        return Math.min(1.0f, vacuumProgress / 100.0f);
    }

    @Override
    public void setVacuumLevel(float level) {
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.vacuum_pump");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new VacuumPumpMenu(containerId, inventory, this);
    }

    public int getEnergyScaled(int max) {
        return (int) ((long) energy * max / MAX_ENERGY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putBoolean("validStructure", validStructure);
        tag.putBoolean("hologramEnabled", hologramEnabled);
        tag.putInt("status", status.ordinal());
        tag.putInt("vacuumProgress", vacuumProgress);
        tag.putInt("evacTicks", evacTicks);
        tag.putInt("pipeCount", pipeCount);
        if (outputPortPos != null) {
            tag.putLong("outputPortPos", outputPortPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        validStructure = tag.getBoolean("validStructure");
        hologramEnabled = tag.getBoolean("hologramEnabled");
        int statusOrdinal = tag.getInt("status");
        status = statusOrdinal >= 0 && statusOrdinal < VacuumStatus.values().length ? VacuumStatus.values()[statusOrdinal] : VacuumStatus.OFFLINE;
        vacuumProgress = tag.getInt("vacuumProgress");
        evacTicks = tag.getInt("evacTicks");
        pipeCount = tag.getInt("pipeCount");
        if (tag.contains("outputPortPos")) {
            outputPortPos = BlockPos.of(tag.getLong("outputPortPos"));
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

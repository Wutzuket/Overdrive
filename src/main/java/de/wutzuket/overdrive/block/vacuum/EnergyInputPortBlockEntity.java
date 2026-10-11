package de.wutzuket.overdrive.block.vacuum;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyInputPortBlockEntity extends BlockEntity implements IEnergyStorage, IVacuumNode {
    private VacuumPumpBlockEntity cachedPump = null;
    private float vacuum = 0;
    private long vacuumFedTick = 0;

    public EnergyInputPortBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.ENERGY_INPUT_PORT_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyInputPortBlockEntity be) {
        IVacuumNode.propagate(be);
    }

    @Override
    public float getVacuumLevel() {
        return vacuum;
    }

    @Override
    public void setVacuumLevel(float level) {
        this.vacuum = level;
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("vacuum", vacuum);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        vacuum = tag.getFloat("vacuum");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private VacuumPumpBlockEntity getPump(Level level, BlockPos pos) {
        if (cachedPump == null || cachedPump.isRemoved()) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos searchPos = pos.offset(dx, dy, dz);
                        BlockEntity be = level.getBlockEntity(searchPos);
                        if (be instanceof VacuumPumpBlockEntity pump) {
                            cachedPump = pump;
                            return pump;
                        }
                    }
                }
            }
            return null;
        }
        return cachedPump;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (level == null) return 0;
        VacuumPumpBlockEntity pump = getPump(level, worldPosition);
        if (pump == null) return 0;
        return pump.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}

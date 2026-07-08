package de.wutzuket.overdrive.block.accelerator;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class AcceleratorEnergyInputPortEntity extends BlockEntity implements IEnergyStorage {
    private ParticleAcceleratorControllerEntity cachedController = null;

    public AcceleratorEnergyInputPortEntity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.ACCELERATOR_ENERGY_INPUT_PORT.get(), blockPos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AcceleratorEnergyInputPortEntity be) {
    }

    private ParticleAcceleratorControllerEntity getController(Level level, BlockPos pos) {
        if (cachedController == null || cachedController.isRemoved()) {
            for (int dx = -30; dx <= 30; dx++) {
                for (int dz = -30; dz <= 30; dz++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos searchPos = pos.offset(dx, dy, dz);
                        BlockEntity be = level.getBlockEntity(searchPos);
                        if (be instanceof ParticleAcceleratorControllerEntity ctrl) {
                            cachedController = ctrl;
                            return ctrl;
                        }
                    }
                }
            }
            return null;
        }
        return cachedController;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (level == null) return 0;
        ParticleAcceleratorControllerEntity ctrl = getController(level, worldPosition);
        if (ctrl == null) return 0;
        return ctrl.receiveEnergy(maxReceive, simulate);
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }
}

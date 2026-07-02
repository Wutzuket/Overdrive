package de.wutzuket.overdrive.block.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import de.wutzuket.overdrive.index.ODBlockEntities;

public class CreativeEnergyCubeBlockEntity extends BlockEntity implements IEnergyStorage {
    private static final int MAX_PUSH = Integer.MAX_VALUE;
    private static final String TAG_ENERGY = "energy";

    private int energy = Integer.MAX_VALUE;

    public CreativeEnergyCubeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.CREATIVE_ENERGY_CUBE_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeEnergyCubeBlockEntity blockEntity) {
        if (level.getGameTime() % 5 != 0) return;

        for (Direction direction : Direction.values()) {
            IEnergyStorage neighborStorage = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos.relative(direction),
                    direction.getOpposite()
            );
            if (neighborStorage != null && neighborStorage.canReceive()) {
                neighborStorage.receiveEnergy(MAX_PUSH, false);
            }
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return maxExtract;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_ENERGY, energy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TAG_ENERGY, CompoundTag.TAG_INT)) {
            energy = tag.getInt(TAG_ENERGY);
        } else {
            energy = Integer.MAX_VALUE;
        }
    }
}

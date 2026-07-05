package de.wutzuket.overdrive.block.vacuum;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VacuumPipeBlockEntity extends BlockEntity implements IVacuumNode {
    private float vacuum = 0;

    public VacuumPipeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.VACUUM_PIPE_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VacuumPipeBlockEntity be) {
        IVacuumNode.propagate(be);
    }

    @Override
    public float getVacuumLevel() {
        return vacuum;
    }

    @Override
    public void setVacuumLevel(float level) {
        this.vacuum = level;
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
}

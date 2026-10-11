package de.wutzuket.overdrive.block.vacuum;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VacuumOutputPortBlockEntity extends BlockEntity implements IVacuumNode {
    private float vacuum = 0;
    private long vacuumFedTick = 0;

    public VacuumOutputPortBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.VACUUM_OUTPUT_PORT_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VacuumOutputPortBlockEntity be) {
        IVacuumNode.propagate(be);
        be.syncToClient();
    }

    @Override
    public float getVacuumLevel() {
        return vacuum;
    }

    @Override
    public void setVacuumLevel(float v) {
        this.vacuum = v;
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

    public void syncToClient() {
        if (getLevel() != null && !getLevel().isClientSide) {
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
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
}

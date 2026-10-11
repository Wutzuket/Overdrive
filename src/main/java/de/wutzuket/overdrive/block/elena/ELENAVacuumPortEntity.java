package de.wutzuket.overdrive.block.elena;

import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ELENAVacuumPortEntity extends BlockEntity implements IVacuumNode {
    private float vacuum = 0;
    private long vacuumFedTick = 0;

    public ELENAVacuumPortEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.ELENA_VACUUM_PORT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ELENAVacuumPortEntity be) {
        IVacuumNode.propagate(be);
        if (!level.isClientSide) {
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    @Override public float getVacuumLevel() { return vacuum; }
    @Override public void setVacuumLevel(float v) { this.vacuum = v; setChanged(); }
    @Override public long getVacuumFedTick() { return vacuumFedTick; }
    @Override public void setVacuumFedTick(long tick) { this.vacuumFedTick = tick; }

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

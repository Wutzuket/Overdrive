package de.wutzuket.overdrive.block.ad;

import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.block.vacuum.VacuumStructures;
import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ADVacuumPortEntity extends BlockEntity implements IVacuumNode {
    private float vacuum = 0;
    private long vacuumFedTick = 0;
    /** Volumen des AD-Rings (einmal berechnet, dann gecacht). */
    private int cachedVolume = -1;

    public ADVacuumPortEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.AD_VACUUM_PORT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ADVacuumPortEntity be) {
        IVacuumNode.propagate(be);
        if (!level.isClientSide) {
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    @Override public float getVacuumLevel() { return vacuum; }
    @Override public void setVacuumLevel(float v) { this.vacuum = v; setChanged(); }
    @Override public long getVacuumFedTick() { return vacuumFedTick; }
    @Override public void setVacuumFedTick(long tick) { this.vacuumFedTick = tick; }

    /** Volumen des AD-Rings in Bloecken (fuer die Vakuum-Pumpe). */
    @Override public int getStructureVolume() {
        if (cachedVolume < 0) {
            Level lvl = getLevel();
            int vol = lvl == null ? 0 : VacuumStructures.adVolume(lvl, getBlockPos());
            // 0 nicht cachen: der Ring kann spaeter erst fertiggestellt werden.
            cachedVolume = vol > 0 ? vol : -1;
        }
        return Math.max(0, cachedVolume);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("vacuum", vacuum);
        tag.putInt("structVolume", cachedVolume);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        vacuum = tag.getFloat("vacuum");
        cachedVolume = tag.contains("structVolume") ? tag.getInt("structVolume") : -1;
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

package de.wutzuket.overdrive.block.annihilation;

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

public class AnnihilationVacuumPortEntity extends BlockEntity implements IVacuumNode {
    private float vacuum=0;
    private long vacuumFedTick=0;
    /** Volumen des Reaktor-Torus (einmal berechnet, dann gecacht). */
    private int cachedVolume=-1;
    public AnnihilationVacuumPortEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.ANNIHILATION_VACUUM_PORT.get(), pos, state); }
    public static void tick(Level level, BlockPos pos, BlockState state, AnnihilationVacuumPortEntity be){
        IVacuumNode.propagate(be);
        if(!level.isClientSide) level.sendBlockUpdated(pos,state,state,2);
    }
    public void tick(Level l, BlockPos p, BlockState s){ IVacuumNode.propagate(this); }
    @Override public float getVacuumLevel(){ return vacuum; }
    @Override public void setVacuumLevel(float v){ vacuum=v; setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    @Override public long getVacuumFedTick(){ return vacuumFedTick; }
    @Override public void setVacuumFedTick(long tick){ vacuumFedTick=tick; }
    /** Volumen des Reaktor-Torus in Bloecken (fuer die Vakuum-Pumpe). */
    @Override public int getStructureVolume(){
        if(cachedVolume<0){
            Level lvl=getLevel();
            int vol = lvl==null?0:VacuumStructures.annihilationVolume(lvl, getBlockPos());
            // 0 nicht cachen: der Reaktor kann spaeter erst fertiggestellt werden.
            cachedVolume = vol>0?vol:-1;
        }
        return Math.max(0, cachedVolume);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){ super.saveAdditional(tag,r); tag.putFloat("vac",vacuum); tag.putInt("structVolume",cachedVolume); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){ super.loadAdditional(tag,r); vacuum=tag.getFloat("vac"); cachedVolume=tag.contains("structVolume")?tag.getInt("structVolume"):-1; }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}

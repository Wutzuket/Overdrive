package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SteamOutputPortEntity extends BlockEntity {
    // Gegenstueck zum Coolant-Port: muss den gleichen Full-Rate-Rueckfluss aufnehmen
    private final FluidTank tank = new FluidTank(300_000_000){
        @Override protected void onContentsChanged(){ setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    };
    public SteamOutputPortEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.ANNIHILATION_STEAM_OUTPUT.get(), pos, state); }
    public FluidTank getTank(){ return tank; }
    private DumpMode dumpMode=DumpMode.IDLE;
    public DumpMode getDumpMode(){ return dumpMode; }
    public void setDumpMode(DumpMode m){ dumpMode=m==null?DumpMode.IDLE:m; setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    public void cycleDumpMode(){ setDumpMode(dumpMode.next()); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){ super.saveAdditional(tag,r); tag.put("fluid", tank.writeToNBT(r,new CompoundTag())); tag.putString("dumpMode", dumpMode.key()); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){ super.loadAdditional(tag,r); if(tag.contains("fluid")) tank.readFromNBT(r, tag.getCompound("fluid")); dumpMode=tag.contains("dumpMode")?DumpMode.fromKey(tag.getString("dumpMode")):DumpMode.IDLE; }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}

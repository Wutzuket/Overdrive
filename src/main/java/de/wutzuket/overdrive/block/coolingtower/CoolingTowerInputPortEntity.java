package de.wutzuket.overdrive.block.coolingtower;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CoolingTowerInputPortEntity extends BlockEntity {
    private final FluidTank tank = new FluidTank(8000){
        @Override protected void onContentsChanged(){ setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    };
    public CoolingTowerInputPortEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.COOLING_TOWER_INPUT_PORT.get(), pos, state); }
    public FluidTank getTank(){ return tank; }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){ super.saveAdditional(tag,r); tag.put("tank", tank.writeToNBT(r,new CompoundTag())); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){ super.loadAdditional(tag,r); if(tag.contains("tank")) tank.readFromNBT(r, tag.getCompound("tank")); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}

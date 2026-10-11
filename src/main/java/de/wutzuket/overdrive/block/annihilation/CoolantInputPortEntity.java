package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CoolantInputPortEntity extends BlockEntity {
    // Max-Rate-Puffer: 280M mB/t Cryofluid-Umsatz (Full-Rate-Spec), int-sicher (< 2.147Mrd)
    private final FluidTank tank = new FluidTank(300_000_000){
        @Override protected void onContentsChanged(){ setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    };
    public CoolantInputPortEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.ANNIHILATION_COOLANT_INPUT.get(), pos, state); }
    public FluidTank getTank(){ return tank; }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){ super.saveAdditional(tag,r); tag.put("fluid", tank.writeToNBT(r,new CompoundTag())); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){ super.loadAdditional(tag,r); if(tag.contains("fluid")) tank.readFromNBT(r, tag.getCompound("fluid")); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}

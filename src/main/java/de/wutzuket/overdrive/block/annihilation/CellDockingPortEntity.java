package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CellDockingPortEntity extends BlockEntity {
    // 2 KB (2.000.000 mB) pro Seite
    private final FluidTank tank = new FluidTank(2_000_000){
        @Override protected void onContentsChanged(){ setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
        @Override public boolean isFluidValid(FluidStack stack){
            Fluid f=stack.getFluid();
            return f==ODFluids.COOLED_ANTIPROTONS_SOURCE.get() || f==ODFluids.ANTIPROTONS_SOURCE.get() || f==ODFluids.DECELERATED_ANTIPROTONS_SOURCE.get()
                    || f==ODFluids.POSITRONS_SOURCE.get();
        }
    };
    public CellDockingPortEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.ANNIHILATION_CELL_PORT.get(), pos, state); }
    public FluidTank getTank(){ return tank; }
    public boolean hasAntiprotonFluid(){
        Fluid f=tank.getFluid().getFluid();
        return (f==ODFluids.COOLED_ANTIPROTONS_SOURCE.get() || f==ODFluids.ANTIPROTONS_SOURCE.get() || f==ODFluids.DECELERATED_ANTIPROTONS_SOURCE.get()) && tank.getFluidAmount()>0;
    }
    public boolean hasPositronFluid(){
        Fluid f=tank.getFluid().getFluid();
        return f==ODFluids.POSITRONS_SOURCE.get() && tank.getFluidAmount()>0;
    }
    public boolean consumeFluid(int amount){
        if(tank.getFluidAmount()>=amount){
            tank.drain(amount, FluidTank.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){ super.saveAdditional(tag,r); tag.put("tank", tank.writeToNBT(r,new CompoundTag())); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){ super.loadAdditional(tag,r); if(tag.contains("tank")) tank.readFromNBT(r, tag.getCompound("tank")); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}

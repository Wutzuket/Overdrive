package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

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
    private final ItemStackHandler itemHandler = new ItemStackHandler(1){
        @Override protected void onContentsChanged(int slot){ setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    };
    public CellDockingPortEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.ANNIHILATION_CELL_PORT.get(), pos, state); }
    public FluidTank getTank(){ return tank; }
    public ItemStackHandler getItemHandler(){ return itemHandler; }
    public boolean hasChargedFluid(){
        return !tank.getFluid().isEmpty() && tank.getFluidAmount()>0;
    }
    public boolean hasAntiprotonFluid(){
        Fluid f=tank.getFluid().getFluid();
        return (f==ODFluids.COOLED_ANTIPROTONS_SOURCE.get() || f==ODFluids.ANTIPROTONS_SOURCE.get() || f==ODFluids.DECELERATED_ANTIPROTONS_SOURCE.get()) && tank.getFluidAmount()>0;
    }
    public boolean hasPositronFluid(){
        Fluid f=tank.getFluid().getFluid();
        return f==ODFluids.POSITRONS_SOURCE.get() && tank.getFluidAmount()>0;
    }
    public boolean hasAntiproton(){
        if(hasAntiprotonFluid()) return true;
        ItemStack s=itemHandler.getStackInSlot(0);
        if(s.isEmpty()) return false;
        Integer a=s.get(ODDataComponents.ANTIPROTON_AMOUNT.get());
        return a!=null && a>0;
    }
    public boolean hasPositron(){
        if(hasPositronFluid()) return true;
        ItemStack s=itemHandler.getStackInSlot(0);
        if(s.isEmpty()) return false;
        Integer p=s.get(ODDataComponents.POSITRON_AMOUNT.get());
        return p!=null && p>0;
    }
    public boolean hasChargedCell(){
        ItemStack s=itemHandler.getStackInSlot(0);
        if(s.isEmpty()) return false;
        Integer a=s.get(ODDataComponents.ANTIPROTON_AMOUNT.get());
        Integer p=s.get(ODDataComponents.POSITRON_AMOUNT.get());
        return (a!=null && a>0) || (p!=null && p>0);
    }
    public boolean consumeFluid(int amount){
        if(tank.getFluidAmount()>=amount){
            tank.drain(amount, FluidTank.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }
    public boolean consumeDose(int amount){
        ItemStack s=itemHandler.getStackInSlot(0);
        if(s.isEmpty()) return false;
        Integer a=s.get(ODDataComponents.ANTIPROTON_AMOUNT.get());
        if(a!=null && a>=amount){
            s.set(ODDataComponents.ANTIPROTON_AMOUNT.get(), a-amount);
            if(a-amount<=0) itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            setChanged();
            return true;
        }
        Integer p=s.get(ODDataComponents.POSITRON_AMOUNT.get());
        if(p!=null && p>=amount){
            s.set(ODDataComponents.POSITRON_AMOUNT.get(), p-amount);
            if(p-amount<=0) itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            setChanged();
            return true;
        }
        return false;
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){ super.saveAdditional(tag,r); tag.put("tank", tank.writeToNBT(r,new CompoundTag())); tag.put("Inv", itemHandler.serializeNBT(r)); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){ super.loadAdditional(tag,r); if(tag.contains("tank")) tank.readFromNBT(r, tag.getCompound("tank")); if(tag.contains("Inv")) itemHandler.deserializeNBT(r, tag.getCompound("Inv")); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}

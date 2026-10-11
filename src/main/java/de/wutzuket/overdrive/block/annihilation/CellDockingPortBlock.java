package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellDockingPortBlock extends Block implements EntityBlock {
    public CellDockingPortBlock(Properties p){ super(p); }
    @Override protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit){
        return InteractionResult.SUCCESS;
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
        BlockEntity be=level.getBlockEntity(pos);
        if(!(be instanceof CellDockingPortEntity port)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        FluidTank tank=port.getTank();
        if(stack.is(Items.BUCKET)){
            if(!level.isClientSide && tank.getFluidAmount()>=1000){
                FluidStack drained=tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                if(!drained.isEmpty()){
                    ItemStack filled=new ItemStack(drained.getFluid().getBucket());
                    if(!player.isCreative()) stack.shrink(1);
                    if(!player.getInventory().add(filled)) player.drop(filled,false);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        var cap=stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if(cap!=null && !stack.is(Items.BUCKET)){
            FluidStack sim=cap.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if(!sim.isEmpty() && tank.isFluidValid(sim) && tank.fill(sim, IFluidHandler.FluidAction.SIMULATE)>=1000){
                if(!level.isClientSide){
                    cap.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    tank.fill(sim, IFluidHandler.FluidAction.EXECUTE);
                    level.sendBlockUpdated(pos, state, state, 3);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state){ return new CellDockingPortEntity(pos,state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type){
        if(type!=ODBlockEntities.ANNIHILATION_CELL_PORT.get()) return null;
        return (l,po,st,be)-> {};
    }
}

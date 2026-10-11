package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class CoolantInputPortBlock extends Block implements EntityBlock {
    public CoolantInputPortBlock(Properties p){ super(p); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state){ return new CoolantInputPortEntity(pos,state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type){
        if(type!=ODBlockEntities.ANNIHILATION_COOLANT_INPUT.get()) return null;
        return (l,po,st,be)-> {};
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit){
        BlockEntity be=level.getBlockEntity(pos);
        if(!(be instanceof CoolantInputPortEntity port)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        FluidTank tank=port.getTank();
        if(stack.is(Items.BUCKET)){
            if(!level.isClientSide){
                if(tank.getFluidAmount()>=1000){
                    FluidStack drained=tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    if(!drained.isEmpty()){
                        ItemStack filled=new ItemStack(drained.getFluid().getBucket());
                        if(!player.isCreative()) stack.shrink(1);
                        if(!player.getInventory().add(filled)) player.drop(filled,false);
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        var bucketHandler=stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if(bucketHandler!=null && !stack.is(Items.BUCKET)){
            FluidStack bucketFluid=bucketHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if(!bucketFluid.isEmpty() && tank.isFluidValid(bucketFluid) && tank.fill(bucketFluid, IFluidHandler.FluidAction.SIMULATE)>=1000){
                if(!level.isClientSide){
                    bucketHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    tank.fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);
                    if(!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    level.sendBlockUpdated(pos, state, state, 3);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        // Auch direktes Rechtsklick mit Wasser-Eimer ohne Capability Fallback
        if(!level.isClientSide && stack.is(Items.WATER_BUCKET)){
            FluidStack water=new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
            if(tank.fill(water, IFluidHandler.FluidAction.SIMULATE)>=1000){
                tank.fill(water, IFluidHandler.FluidAction.EXECUTE);
                if(!player.isCreative()){
                    stack.shrink(1);
                    ItemStack empty=new ItemStack(Items.BUCKET);
                    if(!player.getInventory().add(empty)) player.drop(empty,false);
                }
                level.sendBlockUpdated(pos, state, state, 3);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}

package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.antimatter.IAntimatterPort;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellDockingPortBlock extends Block implements EntityBlock, IAntimatterPort {
    public CellDockingPortBlock(Properties p){ super(p); }
    @Override protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit){
        return InteractionResult.SUCCESS;
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
        BlockEntity be=level.getBlockEntity(pos);
        if(!(be instanceof CellDockingPortEntity port)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        // Die Antimatter werden ausschliesslich ueber das Antimatter-System bewegt, also ueber
        // eine Zelle statt ueber einen Eimer.
        if (stack.getItem() instanceof de.wutzuket.overdrive.item.AntimatterCellItem) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (port.getItemHandler().getStackInSlot(0).isEmpty()
                    && port.getItemHandler().getStackInSlot(1).isEmpty()) {
                port.getItemHandler().setStackInSlot(0, stack.copy());
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return ItemInteractionResult.CONSUME;
            }
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state){ return new CellDockingPortEntity(pos,state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type){
        if(type!=ODBlockEntities.ANNIHILATION_CELL_PORT.get()) return null;
        return (l,po,st,be)-> {
            if (be instanceof CellDockingPortEntity port) {
                CellDockingPortEntity.tick(l, po, st, port);
            }
        };
    }
}

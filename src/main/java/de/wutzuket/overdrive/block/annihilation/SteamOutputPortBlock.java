package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SteamOutputPortBlock extends Block implements EntityBlock {
    public SteamOutputPortBlock(Properties p){ super(p); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state){ return new SteamOutputPortEntity(pos,state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type){
        if(type!=ODBlockEntities.ANNIHILATION_STEAM_OUTPUT.get()) return null;
        return (l,po,st,be)-> {};
    }
    // Sneak + Rechtsklick mit leerer Hand: Dump-Modus umschalten (wie Mekanism: Idle -> Dump -> Dump Excess)
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
        if(player.isShiftKeyDown() && stack.isEmpty() && level.getBlockEntity(pos) instanceof SteamOutputPortEntity port){
            if(!level.isClientSide){
                port.cycleDumpMode();
                player.displayClientMessage(Component.translatable("message.overdrive.steam_dump_mode", port.getDumpMode().displayName()), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}

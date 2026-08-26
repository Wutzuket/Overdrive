package de.wutzuket.overdrive.item;

import de.wutzuket.overdrive.block.vacuum.VacuumPipeBlock;
import de.wutzuket.overdrive.index.ODTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        boolean secondaryUse = context.isSecondaryUseActive();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (state.getBlock() instanceof VacuumPipeBlock pipe) {
            if (!secondaryUse) {
                if (level.isClientSide) {
                    return InteractionResult.SUCCESS;
                }

                Direction face = context.getClickedFace();
                if (pipe.toggleConnection(level, pos, face)) {
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }
        } else if (!secondaryUse) {
            return InteractionResult.PASS;
        }

        if (!canBreak(state)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel serverLevel) {
            dropBlockToInventoryOrWorld(serverLevel, pos, player, context.getItemInHand());
            level.removeBlock(pos, false);
        }
        return InteractionResult.CONSUME;
    }

    private static boolean canBreak(BlockState state) {
        return state.is(ODTags.WRENCHABLE_BLOCKS)
                || state.getBlock().asItem().builtInRegistryHolder().is(ODTags.WRENCHABLE_ITEMS);
    }

    private static void dropBlockToInventoryOrWorld(ServerLevel level, BlockPos pos, Player player, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        var blockEntity = level.getBlockEntity(pos);
        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);

        for (ItemStack drop : drops) {
            ItemHandlerHelper.giveItemToPlayer(player, drop.copy());
        }
    }
}

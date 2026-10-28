package de.wutzuket.overdrive.block.tungsten;

import de.wutzuket.overdrive.antimatter.IAntimatterPort;
import de.wutzuket.overdrive.item.AntimatterCellItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public class PositronVacuumOutputPortBlock extends Block implements EntityBlock, IAntimatterPort {
    public PositronVacuumOutputPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PositronVacuumOutputPortEntity port) {
                player.openMenu(port, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof AntimatterCellItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        // Zellen werden direkt am Ausgang befuellt bzw. geleert, damit das GUI ohne
        // zusaetzlichen Item-Slot auskommt (wie bei den anderen Antimatter-Ports).
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PositronVacuumOutputPortEntity port) {
            port.handleCellUse(player, hand);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public boolean canConnectAntimatter() {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PositronVacuumOutputPortEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof PositronVacuumOutputPortEntity port) {
                PositronVacuumOutputPortEntity.tick(lvl, pos, st, port);
            }
        };
    }
}

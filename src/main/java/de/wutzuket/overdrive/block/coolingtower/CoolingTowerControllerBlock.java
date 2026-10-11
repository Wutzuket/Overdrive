package de.wutzuket.overdrive.block.coolingtower;

import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoolingTowerControllerBlock extends Block implements EntityBlock {
    public CoolingTowerControllerBlock(Properties p) {
        super(p);
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CoolingTowerControllerEntity c) {
                player.openMenu(c, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Creative Auto-Build
        if (!player.isCreative()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (!(stack.getItem() instanceof BlockItem bi)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        Block held = bi.getBlock();
        Block target = null;
        boolean isInputPort = false, isOutputPort = false, isCasing = false;
        if (held == ODBlocks.COOLING_TOWER_INPUT_PORT.get()) { isInputPort = true; target = held; }
        else if (held == ODBlocks.COOLING_TOWER_OUTPUT_PORT.get()) { isOutputPort = true; target = held; }
        else if (held == ODBlocks.COOLING_TOWER_CASING.get()) { isCasing = true; target = held; }
        else return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int placed = 0;

        if (isInputPort) {
            BlockPos inSlot = pos.offset(CoolingTowerControllerEntity.INPUT_DX, CoolingTowerControllerEntity.INPUT_DY, CoolingTowerControllerEntity.INPUT_DZ);
            if (level.getBlockState(inSlot).isAir()) {
                level.setBlock(inSlot, target.defaultBlockState(), 3);
                placed = 1;
            }
        } else if (isOutputPort) {
            BlockPos outSlot = pos.offset(CoolingTowerControllerEntity.OUTPUT_DX, CoolingTowerControllerEntity.OUTPUT_DY, CoolingTowerControllerEntity.OUTPUT_DZ);
            if (level.getBlockState(outSlot).isAir()) {
                level.setBlock(outSlot, target.defaultBlockState(), 3);
                placed = 1;
            }
        } else if (isCasing) {
            // Build entire tower shell
            final int H = CoolingTowerControllerEntity.H;
            for (int y = 0; y < H; y++) {
                int r = (y == 0 || y == H - 1) ? 2 : (y >= 3 && y <= 5 ? 1 : 2);
                for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
                    boolean isWall = Math.abs(x) == r || Math.abs(z) == r;
                    if (!isWall) continue;
                    BlockPos pp = pos.offset(x, y, z);
                    if (pp.equals(pos)) continue;
                    if (y == 1 && x == CoolingTowerControllerEntity.INPUT_DX && z == CoolingTowerControllerEntity.INPUT_DZ) continue;
                    if (y == 7 && x == CoolingTowerControllerEntity.OUTPUT_DX && z == CoolingTowerControllerEntity.OUTPUT_DZ) continue;
                    if (!level.getBlockState(pp).isAir()) continue;
                    level.setBlock(pp, target.defaultBlockState(), 3);
                    placed++;
                }
            }
        }
        if (placed > 0) {
            player.displayClientMessage(Component.literal("Auto-Build: " + placed + "x " + target.getName().getString() + " platziert (Creative)"), true);
        } else {
            player.displayClientMessage(Component.literal("Nichts zu platzieren für " + target.getName().getString()), true);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoolingTowerControllerEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ODBlockEntities.COOLING_TOWER_CONTROLLER.get()) return null;
        return (l, po, st, be) -> { if (be instanceof CoolingTowerControllerEntity c) c.tick(l, po, st); };
    }
}

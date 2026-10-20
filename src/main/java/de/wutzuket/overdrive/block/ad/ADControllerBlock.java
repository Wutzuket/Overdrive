package de.wutzuket.overdrive.block.ad;

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
import net.minecraft.world.item.Items;
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

public class ADControllerBlock extends Block implements EntityBlock {
    public ADControllerBlock(Properties properties) {
        super(properties);
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
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ADControllerEntity ctrl) {
                player.openMenu(ctrl, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ADControllerEntity ctrl)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Creative Auto-Build
        if (!player.isCreative()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (!(stack.getItem() instanceof BlockItem bi)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        Block held = bi.getBlock();
        Block target = null;
        int need = 0;
        boolean isInputPort = false, isOutputPort = false, isEnergyPort = false, isVacuumPort = false, isCasing = false;
        if (held == ODBlocks.AD_INPUT_PORT.get()) { isInputPort = true; target = held; need = 1; }
        else if (held == ODBlocks.AD_OUTPUT_PORT.get()) { isOutputPort = true; target = held; need = 1; }
        else if (held == ODBlocks.AD_ENERGY_INPUT_PORT.get()) { isEnergyPort = true; target = held; need = 1; }
        else if (held == ODBlocks.AD_VACUUM_PORT.get()) { isVacuumPort = true; target = held; need = 1; }
        else if (held == ODBlocks.AD_CASING.get()) { isCasing = true; target = held; }
        else return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx = pos.getX() + facing.getStepX() * ADControllerEntity.OUTER_RADIUS;
        int cy = pos.getY();
        int cz = pos.getZ() + facing.getStepZ() * ADControllerEntity.OUTER_RADIUS;
        int OUTER_RADIUS = ADControllerEntity.OUTER_RADIUS;
        int placed = 0;

        if (need > 0 || isInputPort || isOutputPort || isEnergyPort || isVacuumPort) {
            // Zähle bereits vorhandene
            int existing = 0;
            int minX = cx - OUTER_RADIUS;
            int maxX = cx + OUTER_RADIUS;
            int minZ = cz - OUTER_RADIUS;
            int maxZ = cz + OUTER_RADIUS;
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                int maxDist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                boolean inRing = maxDist >= 3 && maxDist <= OUTER_RADIUS;
                if (!inRing) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos p = new BlockPos(x, cy + dy, z);
                    Block b = level.getBlockState(p).getBlock();
                    if (b == target) existing++;
                }
            }
            int missing = need - existing;
            if (missing <= 0) {
                player.displayClientMessage(Component.literal("Alle " + target.getName().getString() + " bereits vorhanden"), true);
                return ItemInteractionResult.SUCCESS;
            }
            // Platziere an erster freier Ring-Position
            outer: for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                int maxDist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                boolean inRing = maxDist >= 3 && maxDist <= OUTER_RADIUS;
                if (!inRing) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos p = new BlockPos(x, cy + dy, z);
                    if (p.equals(pos)) continue;
                    if (!level.getBlockState(p).isAir()) continue;
                    level.setBlock(p, target.defaultBlockState(), 3);
                    placed++;
                    if (placed >= missing) break outer;
                }
            }
        } else if (isCasing) {
            // Casing: alle Ring-Positionen füllen (Wände und Top/Bottom)
            int minX = cx - OUTER_RADIUS;
            int maxX = cx + OUTER_RADIUS;
            int minZ = cz - OUTER_RADIUS;
            int maxZ = cz + OUTER_RADIUS;
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                int maxDist = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                boolean inRing = maxDist >= 3 && maxDist <= OUTER_RADIUS;
                if (!inRing) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos p = new BlockPos(x, cy + dy, z);
                    if (p.equals(pos)) continue;
                    if (!level.getBlockState(p).isAir()) continue;
                    level.setBlock(p, target.defaultBlockState(), 3);
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
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ADControllerEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != ODBlockEntities.AD_CONTROLLER.get()) {
            return null;
        }
        return (tickerLevel, tickerPos, tickerState, tickerBlockEntity) -> {
            if (tickerBlockEntity instanceof ADControllerEntity ctrl) {
                ctrl.tick(tickerLevel, tickerPos, tickerState);
            }
        };
    }
}
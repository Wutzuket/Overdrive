package de.wutzuket.overdrive.block.scanner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.wutzuket.overdrive.index.ODBlockEntities;

public class scanner_block extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 14.0 / 16.0, 1);

    public scanner_block(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull net.minecraft.world.level.BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new scanner_blockentity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != ODBlockEntities.SCANNER_BLOCK_ENTITY.get()) {
            return null;
        }

        return (tickerLevel, tickerPos, tickerState, tickerBlockEntity) -> {
            if (tickerBlockEntity instanceof scanner_blockentity scannerBlockEntity) {
                scanner_blockentity.serverTick(tickerLevel, tickerPos, tickerState, scannerBlockEntity);
            }
        };
    }
}

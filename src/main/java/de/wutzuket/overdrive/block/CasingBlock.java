package de.wutzuket.overdrive.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public abstract class CasingBlock extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final Map<Direction, BooleanProperty> PROPERTY_MAP = Map.of(
            Direction.NORTH, NORTH, Direction.SOUTH, SOUTH,
            Direction.EAST, EAST, Direction.WEST, WEST,
            Direction.UP, UP, Direction.DOWN, DOWN
    );

    protected CasingBlock(Properties props) {
        super(props);
        BlockState state = defaultBlockState();
        for (BooleanProperty prop : PROPERTY_MAP.values()) {
            state = state.setValue(prop, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROPERTY_MAP.values().toArray(new BooleanProperty[0]));
    }

    protected abstract boolean isMatchingCasing(BlockState state);

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getConnectionState(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return getConnectionState(level, pos, state);
    }

    private BlockState getConnectionState(BlockGetter level, BlockPos pos, BlockState state) {
        for (Map.Entry<Direction, BooleanProperty> entry : PROPERTY_MAP.entrySet()) {
            BlockPos nPos = pos.relative(entry.getKey());
            state = state.setValue(entry.getValue(), isMatchingCasing(level.getBlockState(nPos)));
        }
        return state;
    }

    public static BooleanProperty getPropertyFor(Direction direction) {
        return PROPERTY_MAP.get(direction);
    }
}

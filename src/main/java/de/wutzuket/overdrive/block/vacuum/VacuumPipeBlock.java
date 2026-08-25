package de.wutzuket.overdrive.block.vacuum;

import de.wutzuket.overdrive.block.accelerator.AcceleratorVacuumInputPortBlock;
import de.wutzuket.overdrive.block.ad.ADVacuumPortBlock;
import de.wutzuket.overdrive.block.elena.ELENAVacuumPortBlock;
import de.wutzuket.overdrive.block.wolfram.PositronVacuumOutputPortBlock;
import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.EnumMap;

public class VacuumPipeBlock extends Block implements EntityBlock {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final Map<Direction, BooleanProperty> PROPERTY_MAP = Map.of(
            Direction.NORTH, NORTH,
            Direction.EAST, EAST,
            Direction.SOUTH, SOUTH,
            Direction.WEST, WEST,
            Direction.UP, UP,
            Direction.DOWN, DOWN
    );

    private static final double MIN = 5.0 / 16.0;
    private static final double MAX = 11.0 / 16.0;
    private static final double MID = 8.0 / 16.0;

    private static final VoxelShape CENTER_SHAPE = Shapes.box(MIN, MIN, MIN, MAX, MAX, MAX);
    private static final Map<Direction, VoxelShape> CONNECTION_SHAPES = new EnumMap<>(Direction.class);

    static {
        CONNECTION_SHAPES.put(Direction.NORTH, Shapes.box(MIN, MIN, 0.0, MAX, MAX, MID));
        CONNECTION_SHAPES.put(Direction.SOUTH, Shapes.box(MIN, MIN, MID, MAX, MAX, 1.0));
        CONNECTION_SHAPES.put(Direction.WEST, Shapes.box(0.0, MIN, MIN, MID, MAX, MAX));
        CONNECTION_SHAPES.put(Direction.EAST, Shapes.box(MID, MIN, MIN, 1.0, MAX, MAX));
        CONNECTION_SHAPES.put(Direction.DOWN, Shapes.box(MIN, 0.0, MIN, MAX, MID, MAX));
        CONNECTION_SHAPES.put(Direction.UP, Shapes.box(MIN, MID, MIN, MAX, 1.0, MAX));
    }

    public VacuumPipeBlock(Properties properties) {
        super(properties);
        BlockState defaultState = defaultBlockState();
        for (BooleanProperty prop : PROPERTY_MAP.values()) {
            defaultState = defaultState.setValue(prop, false);
        }
        registerDefaultState(defaultState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return getConnectionState(level, pos, defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return getConnectionState(level, pos, state);
    }

    private BlockState getConnectionState(BlockGetter level, BlockPos pos, BlockState state) {
        for (Map.Entry<Direction, BooleanProperty> entry : PROPERTY_MAP.entrySet()) {
            Direction dir = entry.getKey();
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            state = state.setValue(entry.getValue(), isConnectable(neighborState));
        }
        return state;
    }

    private boolean isConnectable(BlockState state) {
        return state.getBlock() instanceof VacuumPipeBlock
                || state.getBlock() instanceof VacuumOutputPortBlock
                || state.getBlock() instanceof AcceleratorVacuumInputPortBlock
            || state.getBlock() instanceof ADVacuumPortBlock
            || state.getBlock() instanceof ELENAVacuumPortBlock
                || state.getBlock() instanceof PositronVacuumOutputPortBlock
                || state.getBlock() instanceof de.wutzuket.overdrive.block.wolfram.ElectronGunBlock;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTER_SHAPE;
        for (Map.Entry<Direction, BooleanProperty> entry : PROPERTY_MAP.entrySet()) {
            if (state.getValue(entry.getValue())) {
                shape = Shapes.joinUnoptimized(shape, CONNECTION_SHAPES.get(entry.getKey()), BooleanOp.OR);
            }
        }
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new VacuumPipeBlockEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof VacuumPipeBlockEntity pipe) {
                VacuumPipeBlockEntity.tick(lvl, pos, st, pipe);
            }
        };
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof VacuumPipeBlockEntity pipe && pipe.getVacuumLevel() > 0) {
            pipe.setVacuumLevel(Math.max(0, pipe.getVacuumLevel() - 0.02f));
        }
    }
}

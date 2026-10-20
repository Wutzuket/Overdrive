package de.wutzuket.overdrive.block.antimatter;

import de.wutzuket.overdrive.antimatter.AntimatterNetwork;
import de.wutzuket.overdrive.antimatter.IAntimatterPort;
import de.wutzuket.overdrive.antimatter.PipeTier;
import de.wutzuket.overdrive.antimatter.PipeSideMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * The antimatter pipe. Every side can be configured independently (normal, push, pull, disabled)
 * and the pipe renders as a continuous glass tube. Straight pipes are rendered without the center
 * cube so the tube looks completely seamless.
 */
public class AntimatterPipeBlock extends Block implements EntityBlock {

    public static final Map<Direction, EnumProperty<PipeConnection>> CONNECTIONS = new EnumMap<>(Direction.class);
    public static final EnumProperty<PipeShape> SHAPE = EnumProperty.create("shape", PipeShape.class);

    static {
        CONNECTIONS.put(Direction.NORTH, EnumProperty.create("north", PipeConnection.class));
        CONNECTIONS.put(Direction.EAST, EnumProperty.create("east", PipeConnection.class));
        CONNECTIONS.put(Direction.SOUTH, EnumProperty.create("south", PipeConnection.class));
        CONNECTIONS.put(Direction.WEST, EnumProperty.create("west", PipeConnection.class));
        CONNECTIONS.put(Direction.UP, EnumProperty.create("up", PipeConnection.class));
        CONNECTIONS.put(Direction.DOWN, EnumProperty.create("down", PipeConnection.class));
    }

    /** Edge of the tube in blocks. */
    public static final double MIN = 5.0 / 16.0;
    /** Edge of the tube in blocks. */
    public static final double MAX = 11.0 / 16.0;

    private static final VoxelShape CENTER_SHAPE = Shapes.box(MIN, MIN, MIN, MAX, MAX, MAX);
    private static final Map<Direction, VoxelShape> ARM_SHAPES = new EnumMap<>(Direction.class);

    static {
        ARM_SHAPES.put(Direction.NORTH, Shapes.box(MIN, MIN, 0.0, MAX, MAX, MIN));
        ARM_SHAPES.put(Direction.SOUTH, Shapes.box(MIN, MIN, MAX, MAX, MAX, 1.0));
        ARM_SHAPES.put(Direction.WEST, Shapes.box(0.0, MIN, MIN, MIN, MAX, MAX));
        ARM_SHAPES.put(Direction.EAST, Shapes.box(MAX, MIN, MIN, 1.0, MAX, MAX));
        ARM_SHAPES.put(Direction.DOWN, Shapes.box(MIN, 0.0, MIN, MAX, MIN, MAX));
        ARM_SHAPES.put(Direction.UP, Shapes.box(MIN, MAX, MIN, MAX, 1.0, MAX));
    }

    private final PipeTier tier;

    public AntimatterPipeBlock(Properties properties, PipeTier tier) {
        super(properties);
        this.tier = tier;

        BlockState state = defaultBlockState();
        for (EnumProperty<PipeConnection> property : CONNECTIONS.values()) {
            state = state.setValue(property, PipeConnection.NONE);
        }
        registerDefaultState(state.setValue(SHAPE, PipeShape.CENTER));
    }

    /** @return the tier of this pipe, it decides the color and the transfer rate. */
    public PipeTier getTier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (EnumProperty<PipeConnection> property : CONNECTIONS.values()) {
            builder.add(property);
        }
        builder.add(SHAPE);
    }

    // --- connection logic --------------------------------------------------

    /** @return true when pipes may connect to a block with the given state. */
    public static boolean isConnectable(BlockState state) {
        if (state.getBlock() instanceof AntimatterPipeBlock) {
            return true;
        }
        return state.getBlock() instanceof IAntimatterPort port && port.canConnectAntimatter();
    }

    /**
     * Recalculates the block state from the configuration of the pipe and the blocks around it.
     */
    public BlockState getConnectionState(BlockGetter level, BlockPos pos, BlockState state) {
        AntimatterPipeBlockEntity pipe = null;
        if (level.getBlockEntity(pos) instanceof AntimatterPipeBlockEntity existing) {
            pipe = existing;
        }

        Map<Direction, PipeConnection> connections = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            PipeSideMode mode = pipe == null ? PipeSideMode.NORMAL : pipe.getSideMode(direction);
            if (!isConnectable(neighbour)) {
                connections.put(direction, PipeConnection.NONE);
            } else {
                connections.put(direction, switch (mode) {
                    case DISABLED -> PipeConnection.DISABLED;
                    case PULL -> PipeConnection.PULL;
                    case PUSH -> PipeConnection.PUSH;
                    default -> PipeConnection.OPEN;
                });
            }
        }

        for (Map.Entry<Direction, EnumProperty<PipeConnection>> entry : CONNECTIONS.entrySet()) {
            state = state.setValue(entry.getValue(), connections.get(entry.getKey()));
        }
        return state.setValue(SHAPE, computeShape(connections));
    }

    /** Determines whether the pipe can be rendered as one continuous tube. */
    public static PipeShape computeShape(Map<Direction, PipeConnection> connections) {
        boolean north = connections.get(Direction.NORTH) == PipeConnection.OPEN;
        boolean south = connections.get(Direction.SOUTH) == PipeConnection.OPEN;
        boolean east = connections.get(Direction.EAST) == PipeConnection.OPEN;
        boolean west = connections.get(Direction.WEST) == PipeConnection.OPEN;
        boolean up = connections.get(Direction.UP) == PipeConnection.OPEN;
        boolean down = connections.get(Direction.DOWN) == PipeConnection.OPEN;

        if (north && south && !east && !west && !up && !down) {
            return PipeShape.STRAIGHT_Z;
        }
        if (east && west && !north && !south && !up && !down) {
            return PipeShape.STRAIGHT_X;
        }
        if (up && down && !north && !south && !east && !west) {
            return PipeShape.STRAIGHT_Y;
        }
        return PipeShape.CENTER;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return getConnectionState(level, pos, defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return getConnectionState(level, pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos neighbourPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighbourPos, movedByPiston);
        BlockState updated = getConnectionState(level, pos, state);
        if (!updated.equals(state)) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AntimatterPipeBlockEntity pipe) {
            AntimatterNetwork.rebuildNetwork(pipe);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    // --- interaction -------------------------------------------------------

    /**
     * Determines which arm of the pipe was clicked. The local coordinates of the hit position are
     * used to find out which of the six arms is the closest one.
     *
     * @return the clicked arm or {@code null} when the center of the pipe was clicked
     */
    public static Direction getClickedArm(double localX, double localY, double localZ) {
        if (localX < MIN) {
            return Direction.WEST;
        }
        if (localX > MAX) {
            return Direction.EAST;
        }
        if (localY < MIN) {
            return Direction.DOWN;
        }
        if (localY > MAX) {
            return Direction.UP;
        }
        if (localZ < MIN) {
            return Direction.NORTH;
        }
        if (localZ > MAX) {
            return Direction.SOUTH;
        }
        return null;
    }

    /**
     * Determines which arm of the pipe was clicked by using the local coordinates of the hit.
     *
     * @return the clicked arm or {@code null} when the center of the pipe was clicked
     */
    public static Direction getClickedArm(net.minecraft.world.item.context.UseOnContext context) {
        Vec3 local = context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()));
        return getClickedArm(local.x, local.y, local.z);
    }

    /** Recalculates the block state of the pipe so the models and the hitbox are up to date. */
    public static void refreshState(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof AntimatterPipeBlock pipe)) {
            return;
        }
        BlockState updated = pipe.getConnectionState(level, pos, state);
        if (!updated.equals(state)) {
            level.setBlockAndUpdate(pos, updated);
        } else {
            level.sendBlockUpdated(pos, updated, state, Block.UPDATE_ALL);
        }
    }

    // --- shapes ------------------------------------------------------------

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTER_SHAPE;
        for (Map.Entry<Direction, EnumProperty<PipeConnection>> entry : CONNECTIONS.entrySet()) {
            if (state.getValue(entry.getValue()) != PipeConnection.NONE) {
                shape = Shapes.joinUnoptimized(shape, ARM_SHAPES.get(entry.getKey()), BooleanOp.OR);
            }
        }
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    // --- block entity ------------------------------------------------------

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AntimatterPipeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                           BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof AntimatterPipeBlockEntity pipe) {
                AntimatterPipeBlockEntity.tick(lvl, pos, st, pipe);
            }
        };
    }

    /** @return the property that stores the connection state of the given direction. */
    public static Property<PipeConnection> connectionProperty(Direction direction) {
        return CONNECTIONS.get(direction);
    }
}

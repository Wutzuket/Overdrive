package de.wutzuket.overdrive.block.antimatter;

import com.mojang.serialization.MapCodec;
import de.wutzuket.overdrive.antimatter.IAntimatterPort;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * The antimatter tank. It stores antimatter and can be filled or emptied with antimatter cells
 * through its own gui.
 */
public class AntimatterTankBlock extends BaseEntityBlock implements IAntimatterPort {

    private static final MapCodec<AntimatterTankBlock> CODEC = simpleCodec(AntimatterTankBlock::new);

    /** The tank fills the whole block so that the pipes connect without a gap. */
    private static final VoxelShape SHAPE = Shapes.block();

    public AntimatterTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable AntimatterTankEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AntimatterTankEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                          BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof AntimatterTankEntity tank) {
                AntimatterTankEntity.tick(lvl, pos, st, tank);
            }
        };
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Opens the tank gui. The menu has to be opened by the block itself, the vanilla
     * {@code BaseEntityBlock} only provides the menu provider.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider menuProvider = this.getMenuProvider(state, level, pos);
        if (menuProvider == null) {
            return InteractionResult.PASS;
        }
        player.openMenu(menuProvider, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean canConnectAntimatter() {
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock())) {
            level.invalidateCapabilities(pos);
        }
    }
}

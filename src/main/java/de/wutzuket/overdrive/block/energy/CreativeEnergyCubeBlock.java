package de.wutzuket.overdrive.block.energy;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.wutzuket.overdrive.index.ODBlockEntities;

public class CreativeEnergyCubeBlock extends Block implements EntityBlock {
    public static final MapCodec<CreativeEnergyCubeBlock> CODEC = simpleCodec(CreativeEnergyCubeBlock::new);

    public CreativeEnergyCubeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CreativeEnergyCubeBlockEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != ODBlockEntities.CREATIVE_ENERGY_CUBE_BLOCK_ENTITY.get()) {
            return null;
        }
        return (tickerLevel, tickerPos, tickerState, tickerBlockEntity) -> {
            if (tickerBlockEntity instanceof CreativeEnergyCubeBlockEntity energyCube) {
                CreativeEnergyCubeBlockEntity.serverTick(tickerLevel, tickerPos, tickerState, energyCube);
            }
        };
    }
}

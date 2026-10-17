package de.wutzuket.overdrive.block.annihilation;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CoreContainmentGlassBlock extends Block {
    public CoreContainmentGlassBlock(Properties properties) {
        super(properties.sound(SoundType.GLASS).noOcclusion());
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getBlock() == this || super.skipRendering(state, adjacentBlockState, side);
    }
}

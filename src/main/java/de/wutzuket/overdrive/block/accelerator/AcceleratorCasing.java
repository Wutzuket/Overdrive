package de.wutzuket.overdrive.block.accelerator;

import de.wutzuket.overdrive.block.CasingBlock;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class AcceleratorCasing extends CasingBlock {
    public AcceleratorCasing(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isMatchingCasing(BlockState state) {
        return state.is(ODBlocks.ACCELERATOR_CASING.get());
    }
}

package de.wutzuket.overdrive.block.ad;

import de.wutzuket.overdrive.block.CasingBlock;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class ADCasingBlock extends CasingBlock {
    public ADCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isMatchingCasing(BlockState state) {
        return state.is(ODBlocks.AD_CASING.get());
    }
}

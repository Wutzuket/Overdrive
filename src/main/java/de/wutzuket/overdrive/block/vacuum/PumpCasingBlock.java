package de.wutzuket.overdrive.block.vacuum;

import de.wutzuket.overdrive.block.CasingBlock;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class PumpCasingBlock extends CasingBlock {
    public PumpCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isMatchingCasing(BlockState state) {
        return state.is(ODBlocks.PUMP_CASING.get());
    }
}

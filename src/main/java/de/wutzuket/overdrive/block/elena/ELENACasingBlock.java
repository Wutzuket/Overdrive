package de.wutzuket.overdrive.block.elena;

import de.wutzuket.overdrive.block.CasingBlock;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class ELENACasingBlock extends CasingBlock {
    public ELENACasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isMatchingCasing(BlockState state) {
        return state.is(ODBlocks.ELENA_CASING.get());
    }
}

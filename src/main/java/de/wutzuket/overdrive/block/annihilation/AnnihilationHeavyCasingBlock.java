package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.block.CasingBlock;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class AnnihilationHeavyCasingBlock extends CasingBlock {
    public AnnihilationHeavyCasingBlock(Properties properties) { super(properties); }
    @Override protected boolean isMatchingCasing(BlockState state) { return state.is(ODBlocks.ANNIHILATION_HEAVY_CASING.get()); }
}

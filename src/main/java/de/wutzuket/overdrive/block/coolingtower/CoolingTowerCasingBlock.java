package de.wutzuket.overdrive.block.coolingtower;

import de.wutzuket.overdrive.block.CasingBlock;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class CoolingTowerCasingBlock extends CasingBlock {
    public CoolingTowerCasingBlock(Properties p){ super(p); }
    @Override protected boolean isMatchingCasing(BlockState state){ return state.is(ODBlocks.COOLING_TOWER_CASING.get()); }
}

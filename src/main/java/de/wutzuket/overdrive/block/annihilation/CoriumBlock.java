package de.wutzuket.overdrive.block.annihilation;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class CoriumBlock extends Block {
    public CoriumBlock(Properties properties) {
        super(properties.sound(SoundType.STONE).lightLevel(s->8));
    }
}

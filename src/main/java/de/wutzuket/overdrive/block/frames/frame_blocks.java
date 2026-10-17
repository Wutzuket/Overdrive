package de.wutzuket.overdrive.block.frames;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class frame_blocks extends Block {
    public frame_blocks(Properties properties) {
        super(properties.sound(SoundType.STONE));
    }
}

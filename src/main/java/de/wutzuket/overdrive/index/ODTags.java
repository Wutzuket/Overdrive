package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ODTags {
    private ODTags() {}

    public static final TagKey<Block> WRENCHABLE_BLOCKS = TagKey.create(Registries.BLOCK, Overdrive.id("wrenchable"));
    public static final TagKey<Item> WRENCHABLE_ITEMS = TagKey.create(Registries.ITEM, Overdrive.id("wrenchable"));
}

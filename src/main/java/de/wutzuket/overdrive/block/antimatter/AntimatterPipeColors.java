package de.wutzuket.overdrive.block.antimatter;

import de.wutzuket.overdrive.antimatter.PipeTier;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Gives every pipe tier its own color. The tint is applied to the glass of the pipe, so all the
 * tiers share the same models and only differ in their color.
 */
@EventBusSubscriber(modid = de.wutzuket.overdrive.Overdrive.MODID, bus = EventBusSubscriber.Bus.MOD)
public class AntimatterPipeColors {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((net.minecraft.client.color.block.BlockColor) (state, level, pos, tintIndex) -> PipeTier.BASIC.getColor(),
                ODBlocks.ANTIMATTER_PIPE_BASIC.get());
        event.register((net.minecraft.client.color.block.BlockColor) (state, level, pos, tintIndex) -> PipeTier.ADVANCED.getColor(),
                ODBlocks.ANTIMATTER_PIPE_ADVANCED.get());
        event.register((net.minecraft.client.color.block.BlockColor) (state, level, pos, tintIndex) -> PipeTier.ELITE.getColor(),
                ODBlocks.ANTIMATTER_PIPE_ELITE.get());
        event.register((net.minecraft.client.color.block.BlockColor) (state, level, pos, tintIndex) -> PipeTier.ULTIMATE.getColor(),
                ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> {
            if (layer != 0) {
                return 0xFFFFFFFF;
            }
            if (stack.getItem() == ODBlocks.ANTIMATTER_PIPE_BASIC.get().asItem()) {
                return PipeTier.BASIC.getColor();
            }
            if (stack.getItem() == ODBlocks.ANTIMATTER_PIPE_ADVANCED.get().asItem()) {
                return PipeTier.ADVANCED.getColor();
            }
            if (stack.getItem() == ODBlocks.ANTIMATTER_PIPE_ELITE.get().asItem()) {
                return PipeTier.ELITE.getColor();
            }
            if (stack.getItem() == ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get().asItem()) {
                return PipeTier.ULTIMATE.getColor();
            }
            return 0xFFFFFFFF;
        }, ODBlocks.ANTIMATTER_PIPE_BASIC.get().asItem(),
                ODBlocks.ANTIMATTER_PIPE_ADVANCED.get().asItem(),
                ODBlocks.ANTIMATTER_PIPE_ELITE.get().asItem(),
                ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get().asItem());
    }
}

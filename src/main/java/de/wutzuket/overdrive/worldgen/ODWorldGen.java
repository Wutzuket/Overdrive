package de.wutzuket.overdrive.worldgen;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;

public class ODWorldGen {

    public static final ResourceKey<PlacedFeature> PLACED_ALUMINUM_ORE = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "ore_aluminum"));

    public static void register(IEventBus modEventBus) {
    }
}

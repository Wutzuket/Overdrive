package de.wutzuket.overdrive.worldgen;

import com.mojang.serialization.MapCodec;
import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODBiomeModifiers {

    private static final ResourceKey<Registry<MapCodec<? extends BiomeModifier>>> BIOME_MODIFIER_SERIALIZERS_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath("neoforge", "biome_modifier_serializers"));

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(BIOME_MODIFIER_SERIALIZERS_KEY, Overdrive.MODID);

    public static final Supplier<MapCodec<AluminumOreBiomeModifier>> ALUMINUM_ORE =
            BIOME_MODIFIER_SERIALIZERS.register("add_aluminum_ore", () -> AluminumOreBiomeModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}

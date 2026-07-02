package de.wutzuket.overdrive.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.wutzuket.overdrive.config.ODConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record AluminumOreBiomeModifier(HolderSet<PlacedFeature> features) implements BiomeModifier {

    public static final MapCodec<AluminumOreBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AluminumOreBiomeModifier::features)
            ).apply(instance, AluminumOreBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && ODConfig.ENABLE_ALUMINUM_GENERATION.get()) {
            for (Holder<PlacedFeature> feature : this.features) {
                builder.getGenerationSettings().addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}

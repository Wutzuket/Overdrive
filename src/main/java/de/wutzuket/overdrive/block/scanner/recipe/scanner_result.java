package de.wutzuket.overdrive.block.scanner.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Ergebnis eines Scanner-Rezepts: die Item-ID und die Anzahl.
 */
public record scanner_result(@NotNull ResourceLocation id, int count) {
    /**
     * {@code "result": { "id": "overdrive:accelerator_casing", "count": 12 }}
     */
    public static final Codec<scanner_result> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(scanner_result::id),
            Codec.INT.optionalFieldOf("count", 1).forGetter(scanner_result::count)
    ).apply(inst, scanner_result::new));
}

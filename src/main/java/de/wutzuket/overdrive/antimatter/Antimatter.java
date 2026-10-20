package de.wutzuket.overdrive.antimatter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A registered type of antimatter. Antimatter is a completely custom substance that has
 * nothing to do with Minecraft's fluid system - it is transported and stored through the
 * dedicated {@link IAntimatterHandler} capability.
 *
 * @param id           unique registry name of this antimatter type
 * @param translationKey translation key used for the display name
 * @param color        packed ARGB color used for rendering and GUI elements
 */
public record Antimatter(ResourceLocation id, String translationKey, int color) {

    /** Lazily resolved codec so the registry does not have to exist while this class is loaded. */
    public static final Codec<Antimatter> CODEC = Codec.lazyInitialized(() -> ResourceLocation.CODEC.comapFlatMap(
            rl -> {
                Antimatter found = ODAntimatters.get(rl);
                if (found == null) {
                    return DataResult.<Antimatter>error(() -> "Unknown antimatter type: " + rl);
                }
                return DataResult.success(found);
            },
            antimatter -> antimatter.id()));

    /** Network codec that sends the registry name of the type. */
    public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, Antimatter> STREAM_CODEC =
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.map(
                    id -> {
                        ResourceLocation parsed = ResourceLocation.tryParse(id);
                        Antimatter found = parsed == null ? null : ODAntimatters.get(parsed);
                        return found != null ? found : ODAntimatters.ANTIPROTONS;
                    },
                    antimatter -> antimatter.id().toString());

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    /** Red channel in the range 0..1. */
    public float red() {
        return ((color >> 16) & 0xFF) / 255.0F;
    }

    /** Green channel in the range 0..1. */
    public float green() {
        return ((color >> 8) & 0xFF) / 255.0F;
    }

    /** Blue channel in the range 0..1. */
    public float blue() {
        return (color & 0xFF) / 255.0F;
    }

    /** Alpha channel in the range 0..1. */
    public float alpha() {
        return ((color >>> 24) & 0xFF) / 255.0F;
    }
}

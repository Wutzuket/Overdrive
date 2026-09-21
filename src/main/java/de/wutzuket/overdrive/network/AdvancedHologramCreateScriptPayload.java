package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdvancedHologramCreateScriptPayload(BlockPos pos, String fileName, String content)
        implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "advanced_hologram_create_script");
    public static final CustomPacketPayload.Type<AdvancedHologramCreateScriptPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedHologramCreateScriptPayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
                buf.writeBlockPos(value.pos());
                buf.writeUtf(value.fileName() == null ? "" : value.fileName());
                buf.writeUtf(value.content() == null ? "" : value.content());
            }, buf -> new AdvancedHologramCreateScriptPayload(
                    buf.readBlockPos(),
                    buf.readUtf(256),
                    buf.readUtf(32767)));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
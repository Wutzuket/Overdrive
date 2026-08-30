package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HologramCreateFilePayload(BlockPos pos, String fileName, String content)
        implements CustomPacketPayload {
    private static final int MAX_CONTENT_LENGTH = 65536;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "hologram_create_file");
    public static final CustomPacketPayload.Type<HologramCreateFilePayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, HologramCreateFilePayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
                buf.writeBlockPos(value.pos());
                buf.writeUtf(value.fileName());
                buf.writeUtf(value.content(), MAX_CONTENT_LENGTH);
            }, buf -> {
                BlockPos pos = buf.readBlockPos();
                String fileName = buf.readUtf(32767);
                String content = buf.readUtf(MAX_CONTENT_LENGTH);
                return new HologramCreateFilePayload(pos, fileName, content);
            });

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
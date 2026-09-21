package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record AdvancedHologramSaveScriptPayload(BlockPos pos, @Nullable String fileName, String content)
        implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "advanced_hologram_save_script");
    public static final CustomPacketPayload.Type<AdvancedHologramSaveScriptPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedHologramSaveScriptPayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
                buf.writeBlockPos(value.pos());
                buf.writeNullable(value.fileName(), FriendlyByteBuf::writeUtf);
                buf.writeUtf(value.content() == null ? "" : value.content());
            }, buf -> {
                BlockPos pos = buf.readBlockPos();
                String fileName = buf.readNullable(byteBuf -> byteBuf.readUtf(32767));
                return new AdvancedHologramSaveScriptPayload(pos, fileName, buf.readUtf(32767));
            });

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
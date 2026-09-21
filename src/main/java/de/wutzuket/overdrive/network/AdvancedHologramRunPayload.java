package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdvancedHologramRunPayload(BlockPos pos, boolean running)
        implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "advanced_hologram_run");
    public static final CustomPacketPayload.Type<AdvancedHologramRunPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedHologramRunPayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
                buf.writeBlockPos(value.pos());
                buf.writeBoolean(value.running());
            }, buf -> new AdvancedHologramRunPayload(buf.readBlockPos(), buf.readBoolean()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
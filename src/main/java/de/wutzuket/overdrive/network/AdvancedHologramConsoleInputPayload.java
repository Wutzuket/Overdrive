package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdvancedHologramConsoleInputPayload(BlockPos pos, String input)
        implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "advanced_hologram_console_input");
    public static final CustomPacketPayload.Type<AdvancedHologramConsoleInputPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedHologramConsoleInputPayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
                buf.writeBlockPos(value.pos());
                buf.writeUtf(value.input() == null ? "" : value.input());
            }, buf -> new AdvancedHologramConsoleInputPayload(
                    buf.readBlockPos(),
                    buf.readUtf(256)));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AnnihilationTogglePayload(BlockPos pos, boolean enabled) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "annihilation_toggle");
    public static final Type<AnnihilationTogglePayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AnnihilationTogglePayload> STREAM_CODEC =
            StreamCodec.of((buf, v) -> { buf.writeBlockPos(v.pos()); buf.writeBoolean(v.enabled()); },
                    buf -> new AnnihilationTogglePayload(buf.readBlockPos(), buf.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

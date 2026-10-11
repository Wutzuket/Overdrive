package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AnnihilationBurnRatePayload(BlockPos pos, double burnRate) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "annihilation_burn_rate");
    public static final Type<AnnihilationBurnRatePayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AnnihilationBurnRatePayload> STREAM_CODEC =
            StreamCodec.of((buf, v) -> { buf.writeBlockPos(v.pos()); buf.writeDouble(v.burnRate()); },
                    buf -> new AnnihilationBurnRatePayload(buf.readBlockPos(), buf.readDouble()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

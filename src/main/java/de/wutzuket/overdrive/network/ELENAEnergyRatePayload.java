package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ELENAEnergyRatePayload(BlockPos pos, int energyRate) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "elena_energy_rate");
    public static final Type<ELENAEnergyRatePayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ELENAEnergyRatePayload> STREAM_CODEC =
            StreamCodec.of((buf, v) -> { buf.writeBlockPos(v.pos()); buf.writeInt(v.energyRate()); },
                    buf -> new ELENAEnergyRatePayload(buf.readBlockPos(), buf.readInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

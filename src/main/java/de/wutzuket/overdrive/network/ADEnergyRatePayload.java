package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ADEnergyRatePayload(BlockPos pos, int energyRate) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "ad_energy_rate");
    public static final Type<ADEnergyRatePayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ADEnergyRatePayload> STREAM_CODEC =
            StreamCodec.of((buf, v) -> { buf.writeBlockPos(v.pos()); buf.writeInt(v.energyRate()); },
                    buf -> new ADEnergyRatePayload(buf.readBlockPos(), buf.readInt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

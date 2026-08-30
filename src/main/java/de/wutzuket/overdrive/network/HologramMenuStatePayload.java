package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.block.hologram.HologramBlockEntity;
import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record HologramMenuStatePayload(BlockPos pos, List<String> availableFiles, String selectedFile,
                                       double offsetX, double offsetY, double offsetZ,
                                       double scale, double rotationX, double rotationY, double rotationZ,
                                       boolean spin)
        implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "hologram_menu_state");
    public static final CustomPacketPayload.Type<HologramMenuStatePayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, HologramMenuStatePayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
                buf.writeBlockPos(value.pos());
                buf.writeVarInt(value.availableFiles().size());
                for (String file : value.availableFiles()) {
                    buf.writeUtf(file);
                }
                buf.writeBoolean(value.selectedFile() != null);
                if (value.selectedFile() != null) {
                    buf.writeUtf(value.selectedFile());
                }
                buf.writeDouble(value.offsetX());
                buf.writeDouble(value.offsetY());
                buf.writeDouble(value.offsetZ());
                buf.writeDouble(value.scale());
                buf.writeDouble(value.rotationX());
                buf.writeDouble(value.rotationY());
                buf.writeDouble(value.rotationZ());
                buf.writeBoolean(value.spin());
            }, buf -> {
                BlockPos pos = buf.readBlockPos();
                int size = buf.readVarInt();
                List<String> files = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    files.add(buf.readUtf(32767));
                }
                String selected = buf.readBoolean() ? buf.readUtf(32767) : null;
                double offsetX = buf.readDouble();
                double offsetY = buf.readDouble();
                double offsetZ = buf.readDouble();
                double scale = buf.readDouble();
                double rotationX = buf.readDouble();
                double rotationY = buf.readDouble();
                double rotationZ = buf.readDouble();
                boolean spin = buf.readBoolean();
                return new HologramMenuStatePayload(pos, List.copyOf(files), selected, offsetX, offsetY, offsetZ, scale, rotationX, rotationY, rotationZ, spin);
            });

    public static HologramMenuStatePayload fromBlockEntity(HologramBlockEntity blockEntity) {
        return new HologramMenuStatePayload(
                blockEntity.getBlockPos(),
                List.copyOf(blockEntity.getAvailableJsonFiles()),
                blockEntity.getSelectedHologramFile(),
                blockEntity.getOffsetX(),
                blockEntity.getOffsetY(),
                blockEntity.getOffsetZ(),
                blockEntity.getScale(),
                blockEntity.getRotationX(),
                blockEntity.getRotationY(),
                blockEntity.getRotationZ(),
                blockEntity.getSpin()
        );
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

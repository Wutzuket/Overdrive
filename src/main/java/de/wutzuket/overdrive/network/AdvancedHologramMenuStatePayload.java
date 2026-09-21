package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.advanced_hologram.AdvancedHologramBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record AdvancedHologramMenuStatePayload(BlockPos pos, String scriptText, boolean running, boolean powered,
                                               List<String> scriptFiles, @Nullable String selectedScriptFile)
        implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "advanced_hologram_menu_state");
    public static final CustomPacketPayload.Type<AdvancedHologramMenuStatePayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedHologramMenuStatePayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
                buf.writeBlockPos(value.pos());
                buf.writeUtf(value.scriptText() == null ? "" : value.scriptText());
                buf.writeBoolean(value.running());
                buf.writeBoolean(value.powered());
                buf.writeNullable(value.selectedScriptFile(), FriendlyByteBuf::writeUtf);
                buf.writeCollection(value.scriptFiles(), FriendlyByteBuf::writeUtf);
            }, buf -> {
                BlockPos pos = buf.readBlockPos();
                String scriptText = buf.readUtf(32767);
                boolean running = buf.readBoolean();
                boolean powered = buf.readBoolean();
                String selectedScriptFile = buf.readNullable(byteBuf -> byteBuf.readUtf(32767));
                List<String> scriptFiles = buf.readList(byteBuf -> byteBuf.readUtf(32767));
                return new AdvancedHologramMenuStatePayload(pos, scriptText, running, powered, scriptFiles, selectedScriptFile);
            });

    public static AdvancedHologramMenuStatePayload fromBlockEntity(AdvancedHologramBlockEntity blockEntity) {
        return fromBlockEntity(blockEntity, null);
    }

    public static AdvancedHologramMenuStatePayload fromBlockEntity(AdvancedHologramBlockEntity blockEntity, @Nullable UUID playerUuid) {
        return new AdvancedHologramMenuStatePayload(
                blockEntity.getBlockPos(),
                blockEntity.getScriptText(),
                blockEntity.isRunning(),
                blockEntity.isPowered(),
                playerUuid != null ? blockEntity.getAvailableScriptFilesForPlayer(playerUuid) : blockEntity.getAvailableScriptFiles(),
                blockEntity.getSelectedScriptFile()
        );
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
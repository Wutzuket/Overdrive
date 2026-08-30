package de.wutzuket.overdrive.network;

import com.google.gson.JsonParser;
import de.wutzuket.overdrive.block.hologram.HologramBlockEntity;
import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class ServerPayloadHandler {
    public static void handleToggleHologram(final ToggleHologramPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Level level = serverPlayer.level();
                BlockPos pos = payload.pos();
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ParticleAcceleratorControllerEntity ctrl) {
                    ctrl.toggleHologram();
                } else if (be instanceof de.wutzuket.overdrive.block.ad.ADControllerEntity ad) {
                    ad.toggleHologram();
                } else if (be instanceof de.wutzuket.overdrive.block.elena.ELENAControllerEntity elena) {
                    elena.toggleHologram();
                } else if (be instanceof de.wutzuket.overdrive.block.wolfram.ElectronGunEntity gun) {
                    gun.toggleHologram();
                }
            }
        });
    }

    public static void handleHologramMenuUpdate(final HologramMenuUpdatePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof HologramBlockEntity hologram)) {
                return;
            }

            if (payload.selectedFile() != null) {
                if (hologram.getAvailableJsonFiles().contains(payload.selectedFile())) {
                    hologram.setSelectedHologramFile(payload.selectedFile());
                }
            } else {
                hologram.setSelectedHologramFile(null);
            }

            hologram.setOffsetX(payload.offsetX());
            hologram.setOffsetY(payload.offsetY());
            hologram.setOffsetZ(payload.offsetZ());
            hologram.setScale(payload.scale());
            hologram.setRotationX(payload.rotationX());
            hologram.setRotationY(payload.rotationY());
            hologram.setRotationZ(payload.rotationZ());
            hologram.setSpin(payload.spin());
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, HologramMenuStatePayload.fromBlockEntity(hologram));
        });
    }

    public static void handleHologramCreateFile(final HologramCreateFilePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof HologramBlockEntity hologram)) {
                return;
            }

            String fileName = sanitizeFileName(payload.fileName());
            if (fileName == null || !isValidJson(payload.content())) {
                return;
            }

            ServerLevel serverLevel = (ServerLevel) level;
            Path directory = hologram.ensureHologramsDirectory(serverLevel);
            Path target = directory.resolve(fileName);
            if (Files.exists(target)) {
                return;
            }

            try {
                Files.writeString(target, payload.content());
            } catch (IOException ignored) {
                return;
            }

            hologram.scanJsonFiles(serverLevel);
            hologram.setSelectedHologramFile(fileName);
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, HologramMenuStatePayload.fromBlockEntity(hologram));
        });
    }

    private static @org.jetbrains.annotations.Nullable String sanitizeFileName(String raw) {
        if (raw == null) {
            return null;
        }
        String name = raw.trim();
        if (name.isEmpty() || name.equals(".") || name.equals("..")) {
            return null;
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!name.toLowerCase(Locale.ROOT).endsWith(".json")) {
            name = name + ".json";
        }
        return name.isEmpty() || name.equals(".") || name.equals("..") ? null : name;
    }

    private static boolean isValidJson(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }
        try {
            JsonParser.parseString(content);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}

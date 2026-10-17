package de.wutzuket.overdrive.network;

import com.google.gson.JsonParser;
import de.wutzuket.overdrive.block.advanced_hologram.AdvancedHologramBlockEntity;
import de.wutzuket.overdrive.block.hologram.HologramBlockEntity;
import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
                } else if (be instanceof de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity ann) {
                    ann.toggleHologram();
                } else if (be instanceof de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity cooling) {
                    cooling.toggleHologram();
                } else if (be instanceof de.wutzuket.overdrive.block.distiller.CryoDistillerEntity distiller) {
                    distiller.toggleHologram();
                }
            }
        });
    }
    public static void handleAnnihilationToggle(final AnnihilationTogglePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer sp){
                Level lvl=sp.level();
                BlockEntity be=lvl.getBlockEntity(payload.pos());
                if(be instanceof de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity ann){
                    ann.setEnabled(payload.enabled());
                }
            }
        });
    }
    public static void handleAcceleratorEnergyRate(final AcceleratorEnergyRatePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer sp){
                Level lvl=sp.level();
                BlockEntity be=lvl.getBlockEntity(payload.pos());
                if(be instanceof ParticleAcceleratorControllerEntity acc){
                    acc.setEnergyRate(payload.energyRate());
                }
            }
        });
    }
    public static void handleADEnergyRate(final ADEnergyRatePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer sp){
                Level lvl=sp.level();
                BlockEntity be=lvl.getBlockEntity(payload.pos());
                if(be instanceof de.wutzuket.overdrive.block.ad.ADControllerEntity ad){
                    ad.setEnergyRate(payload.energyRate());
                }
            }
        });
    }
    public static void handleELENAEnergyRate(final ELENAEnergyRatePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer sp){
                Level lvl=sp.level();
                BlockEntity be=lvl.getBlockEntity(payload.pos());
                if(be instanceof de.wutzuket.overdrive.block.elena.ELENAControllerEntity elena){
                    elena.setEnergyRate(payload.energyRate());
                }
            }
        });
    }
    public static void handleAnnihilationBurnRate(final AnnihilationBurnRatePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer sp){
                Level lvl=sp.level();
                BlockEntity be=lvl.getBlockEntity(payload.pos());
                if(be instanceof de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity ann){
                    ann.setBurnRate(payload.burnRate());
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

    public static void handleAdvancedHologramScript(final AdvancedHologramScriptPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof AdvancedHologramBlockEntity hologram)) {
                return;
            }

            String script = payload.scriptText() == null ? "" : payload.scriptText();
            hologram.setScriptText(script);
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, AdvancedHologramMenuStatePayload.fromBlockEntity(hologram, serverPlayer.getUUID()));
        });
    }

    public static void handleAdvancedHologramRun(final AdvancedHologramRunPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof AdvancedHologramBlockEntity hologram)) {
                return;
            }

            hologram.setRunning(payload.running());
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, AdvancedHologramMenuStatePayload.fromBlockEntity(hologram, serverPlayer.getUUID()));
        });
    }

    public static void handleAdvancedHologramConsoleInput(final AdvancedHologramConsoleInputPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof AdvancedHologramBlockEntity hologram)) {
                return;
            }

            hologram.submitScriptInput(payload.input());
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
        });
    }

    public static void handleAdvancedHologramSelectScript(final AdvancedHologramSelectScriptPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof AdvancedHologramBlockEntity hologram)) {
                return;
            }
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            hologram.scanScriptFiles(serverLevel);
            String fileName = payload.fileName();
            if (fileName == null || !hologram.getAvailableScriptFiles().contains(fileName)) {
                hologram.setSelectedScriptFile(null);
                hologram.setRunning(false);
            } else if (!hologram.isOwnedByPlayer(fileName, serverPlayer.getUUID())) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.select_error_not_owner"), true);
            } else {
                hologram.setSelectedScriptFile(fileName);
                hologram.setRunning(true);
            }
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, AdvancedHologramMenuStatePayload.fromBlockEntity(hologram, serverPlayer.getUUID()));
        });
    }

    public static void handleAdvancedHologramSaveScript(final AdvancedHologramSaveScriptPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof AdvancedHologramBlockEntity hologram)) {
                return;
            }
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            String fileName = sanitizeScriptFileName(payload.fileName());
            String content = payload.content() == null ? "" : payload.content();
            if (content.length() > 32767) {
                content = content.substring(0, 32767);
            }

            if (fileName == null) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.save_error_no_file"), true);
                return;
            }

            if (!hologram.isOwnedByPlayer(fileName, serverPlayer.getUUID())) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.save_error_not_owner"), true);
                return;
            }

            if (!hologram.saveScriptFile(serverLevel, fileName, content)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.save_error"), true);
                return;
            }

            hologram.setScriptOwner(serverLevel, fileName, serverPlayer.getUUID());
            hologram.setSelectedScriptFile(fileName);
            hologram.setScriptText(content);
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, AdvancedHologramMenuStatePayload.fromBlockEntity(hologram, serverPlayer.getUUID()));
            serverPlayer.displayClientMessage(
                    Component.translatable("container.overdrive.advanced_hologram_block.save_success", fileName), true);
        });
    }

    public static void handleAdvancedHologramCreateScript(final AdvancedHologramCreateScriptPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof AdvancedHologramBlockEntity hologram)) {
                return;
            }
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            String fileName = sanitizeScriptFileName(payload.fileName());
            String content = payload.content() == null ? "" : payload.content();
            if (content.length() > 32767) {
                content = content.substring(0, 32767);
            }
            if (fileName == null) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.create_error_name"), true);
                return;
            }

            Path target = hologram.ensureScriptsDirectory(serverLevel).resolve(fileName);
            if (Files.exists(target)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.create_error_exists"), true);
                return;
            }

            try {
                Files.writeString(target, content);
            } catch (IOException ignored) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.create_error"), true);
                return;
            }

            hologram.scanScriptFiles(serverLevel);
            hologram.setScriptOwner(serverLevel, fileName, serverPlayer.getUUID());
            hologram.setSelectedScriptFile(fileName);
            hologram.setRunning(true);
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, AdvancedHologramMenuStatePayload.fromBlockEntity(hologram, serverPlayer.getUUID()));
            serverPlayer.displayClientMessage(
                    Component.translatable("container.overdrive.advanced_hologram_block.create_success", fileName), true);
        });
    }

    public static void handleAdvancedHologramDeleteScript(final AdvancedHologramDeleteScriptPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            Level level = serverPlayer.level();
            BlockPos pos = payload.pos();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof AdvancedHologramBlockEntity hologram)) {
                return;
            }
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            String fileName = sanitizeScriptFileName(payload.fileName());
            if (fileName == null || !hologram.getAvailableScriptFiles().contains(fileName)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.delete_error_no_file"), true);
                return;
            }

            if (!hologram.isOwnedByPlayer(fileName, serverPlayer.getUUID())) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.delete_error_not_owner"), true);
                return;
            }

            if (!hologram.deleteScriptFile(serverLevel, fileName)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("container.overdrive.advanced_hologram_block.delete_error"), true);
                return;
            }

            hologram.removeScriptOwner(serverLevel, fileName);
            hologram.scanScriptFiles(serverLevel);
            if (fileName.equals(hologram.getSelectedScriptFile())) {
                hologram.setSelectedScriptFile(null);
                hologram.setRunning(false);
            }
            level.sendBlockUpdated(pos, hologram.getBlockState(), hologram.getBlockState(), 3);
            PacketDistributor.sendToPlayer(serverPlayer, AdvancedHologramMenuStatePayload.fromBlockEntity(hologram, serverPlayer.getUUID()));
            serverPlayer.displayClientMessage(
                    Component.translatable("container.overdrive.advanced_hologram_block.delete_success", fileName), true);
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

    private static @org.jetbrains.annotations.Nullable String sanitizeScriptFileName(String raw) {
        if (raw == null) {
            return null;
        }
        String name = raw.trim();
        if (name.isEmpty() || name.equals(".") || name.equals("..")) {
            return null;
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!name.toLowerCase(Locale.ROOT).endsWith(".lua")) {
            name = name + ".lua";
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

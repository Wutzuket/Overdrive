package de.wutzuket.overdrive.block.hologram;

import de.wutzuket.overdrive.menu.HologramMenu;
import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HologramBlockEntity extends BlockEntity implements MenuProvider {
    private static final String TAG_SELECTED_FILE = "SelectedHologramFile";
    private static final String TAG_SELECTED_JSON = "SelectedHologramJson";
    private static final String TAG_OFFSET_X = "OffsetX";
    private static final String TAG_OFFSET_Y = "OffsetY";
    private static final String TAG_OFFSET_Z = "OffsetZ";
    private static final String TAG_SCALE = "Scale";
    private static final String TAG_ROTATION_X = "RotationX";
    private static final String TAG_ROTATION_Y = "RotationY";
    private static final String TAG_ROTATION_Z = "RotationZ";
    private static final String TAG_SPIN = "Spin";

    private final List<String> availableJsonFiles = new ArrayList<>();
    private @Nullable Path hologramsDirectory;
    private @Nullable String selectedHologramFile;
    private @Nullable String selectedHologramJson;
    private transient @Nullable HologramModel cachedModel;
    private transient @Nullable String cachedModelSource;

    private double offsetX;
    private double offsetY = 1.0D;
    private double offsetZ;
    private double scale = 1.0D;
    private double rotationX;
    private double rotationY;
    private double rotationZ;
    private boolean spin = true;

    public HologramBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.HOLOGRAM_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            ensureHologramsDirectory(serverLevel);
            scanJsonFiles(serverLevel);
            reloadSelectedHologramJson(serverLevel);
        }
    }

    public Path ensureHologramsDirectory(ServerLevel serverLevel) {
        if (hologramsDirectory != null) {
            return hologramsDirectory;
        }

        Path worldRoot = serverLevel.getServer().getWorldPath(LevelResource.ROOT);
        Path hologramsPath = worldRoot.resolve("holograms");
        try {
            Files.createDirectories(hologramsPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create holograms directory at " + hologramsPath, exception);
        }
        hologramsDirectory = hologramsPath;
        return hologramsPath;
    }

    public List<String> scanJsonFiles(ServerLevel serverLevel) {
        Path hologramsPath = ensureHologramsDirectory(serverLevel);
        availableJsonFiles.clear();

        try (var stream = Files.list(hologramsPath)) {
            stream.filter(path -> Files.isRegularFile(path))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.toLowerCase().endsWith(".json"))
                    .filter(name -> !name.equals("owners.json"))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(availableJsonFiles::add);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan holograms directory at " + hologramsPath, exception);
        }

        if (selectedHologramFile != null && !availableJsonFiles.contains(selectedHologramFile)) {
            selectedHologramFile = null;
            reloadSelectedHologramJson(serverLevel);
            setChanged();
        }

        return Collections.unmodifiableList(new ArrayList<>(availableJsonFiles));
    }

    public List<String> getAvailableJsonFiles() {
        return Collections.unmodifiableList(availableJsonFiles);
    }

    public @Nullable String getSelectedHologramFile() {
        return selectedHologramFile;
    }

    public void setSelectedHologramFile(@Nullable String selectedHologramFile) {
        this.selectedHologramFile = selectedHologramFile;
        if (level instanceof ServerLevel serverLevel) {
            reloadSelectedHologramJson(serverLevel);
        } else {
            invalidateModelCache();
        }
        setChanged();
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public double getScale() {
        return scale;
    }

    public double getRotationX() {
        return rotationX;
    }

    public double getRotationY() {
        return rotationY;
    }

    public double getRotationZ() {
        return rotationZ;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
        setChanged();
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
        setChanged();
    }

    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
        setChanged();
    }

    public void setScale(double scale) {
        this.scale = scale;
        setChanged();
    }

    public void setRotationX(double rotationX) {
        this.rotationX = rotationX;
        setChanged();
    }

    public void setRotationY(double rotationY) {
        this.rotationY = rotationY;
        setChanged();
    }

    public void setRotationZ(double rotationZ) {
        this.rotationZ = rotationZ;
        setChanged();
    }

    public boolean getSpin() {
        return spin;
    }

    public void setSpin(boolean spin) {
        this.spin = spin;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.hologram_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new HologramMenu(containerId, inventory, this);
    }

    public @Nullable String getSelectedHologramJson() {
        return selectedHologramJson;
    }

    public @Nullable HologramModel getCachedModel() {
        if (selectedHologramJson == null || selectedHologramJson.isBlank()) {
            return null;
        }
        if (cachedModel != null && Objects.equals(cachedModelSource, selectedHologramJson)) {
            return cachedModel;
        }
        cachedModel = HologramModel.parse(selectedHologramJson);
        cachedModelSource = selectedHologramJson;
        return cachedModel;
    }

    public void reloadSelectedHologramJson(ServerLevel serverLevel) {
        if (selectedHologramFile == null) {
            selectedHologramJson = null;
            invalidateModelCache();
            setChanged();
            return;
        }

        Path hologramsPath = ensureHologramsDirectory(serverLevel);
        Path selectedPath = hologramsPath.resolve(selectedHologramFile);
        if (!Files.isRegularFile(selectedPath)) {
            selectedHologramJson = null;
            invalidateModelCache();
            setChanged();
            return;
        }

        try {
            selectedHologramJson = Files.readString(selectedPath);
        } catch (IOException exception) {
            selectedHologramJson = null;
        }
        invalidateModelCache();
        setChanged();
    }

    private void invalidateModelCache() {
        cachedModel = null;
        cachedModelSource = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (selectedHologramFile != null) {
            tag.putString(TAG_SELECTED_FILE, selectedHologramFile);
        }
        if (selectedHologramJson != null) {
            tag.putString(TAG_SELECTED_JSON, selectedHologramJson);
        }
        tag.putDouble(TAG_OFFSET_X, offsetX);
        tag.putDouble(TAG_OFFSET_Y, offsetY);
        tag.putDouble(TAG_OFFSET_Z, offsetZ);
        tag.putDouble(TAG_SCALE, scale);
        tag.putDouble(TAG_ROTATION_X, rotationX);
        tag.putDouble(TAG_ROTATION_Y, rotationY);
        tag.putDouble(TAG_ROTATION_Z, rotationZ);
        tag.putBoolean(TAG_SPIN, spin);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        selectedHologramFile = tag.contains(TAG_SELECTED_FILE) ? tag.getString(TAG_SELECTED_FILE) : null;
        selectedHologramJson = tag.contains(TAG_SELECTED_JSON) ? tag.getString(TAG_SELECTED_JSON) : null;
        offsetX = tag.contains(TAG_OFFSET_X) ? tag.getDouble(TAG_OFFSET_X) : 0.0D;
        offsetY = tag.contains(TAG_OFFSET_Y) ? tag.getDouble(TAG_OFFSET_Y) : 1.0D;
        offsetZ = tag.contains(TAG_OFFSET_Z) ? tag.getDouble(TAG_OFFSET_Z) : 0.0D;
        scale = tag.contains(TAG_SCALE) ? tag.getDouble(TAG_SCALE) : 1.0D;
        rotationX = tag.contains(TAG_ROTATION_X) ? tag.getDouble(TAG_ROTATION_X) : 0.0D;
        rotationY = tag.contains(TAG_ROTATION_Y) ? tag.getDouble(TAG_ROTATION_Y) : 0.0D;
        rotationZ = tag.contains(TAG_ROTATION_Z) ? tag.getDouble(TAG_ROTATION_Z) : 0.0D;
        spin = tag.contains(TAG_SPIN) ? tag.getBoolean(TAG_SPIN) : true;
        invalidateModelCache();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public record HologramModel(List<HologramElement> elements) {
        public static HologramModel parse(String json) {
            List<HologramElement> elements = new ArrayList<>();
            try {
                com.google.gson.JsonElement parsed = com.google.gson.JsonParser.parseString(json);
                if (!parsed.isJsonObject()) {
                    return new HologramModel(List.of());
                }

                com.google.gson.JsonObject root = parsed.getAsJsonObject();
                com.google.gson.JsonArray array = root.has("elements") && root.get("elements").isJsonArray()
                        ? root.getAsJsonArray("elements")
                        : null;
                if (array == null) {
                    return new HologramModel(List.of());
                }

                for (com.google.gson.JsonElement entry : array) {
                    if (!entry.isJsonObject()) {
                        continue;
                    }
                    com.google.gson.JsonObject elementObject = entry.getAsJsonObject();
                    double[] from = readVec3(elementObject, "from");
                    double[] to = readVec3(elementObject, "to");
                    if (from == null || to == null) {
                        continue;
                    }

                    ElementRotation rotation = null;
                    if (elementObject.has("rotation") && elementObject.get("rotation").isJsonObject()) {
                        com.google.gson.JsonObject rotationObject = elementObject.getAsJsonObject("rotation");
                        double[] origin = readVec3(rotationObject, "origin");
                        String axis = rotationObject.has("axis") ? rotationObject.get("axis").getAsString() : "y";
                        float angle = rotationObject.has("angle") ? rotationObject.get("angle").getAsFloat() : 0.0F;
                        boolean rescale = rotationObject.has("rescale") && rotationObject.get("rescale").getAsBoolean();
                        if (origin != null) {
                            rotation = new ElementRotation(origin[0] / 16.0D, origin[1] / 16.0D, origin[2] / 16.0D, axis, angle, rescale);
                        }
                    }

                    elements.add(new HologramElement(
                            from[0] / 16.0D, from[1] / 16.0D, from[2] / 16.0D,
                            to[0] / 16.0D, to[1] / 16.0D, to[2] / 16.0D,
                            rotation
                    ));
                }
            } catch (Exception ignored) {
                return new HologramModel(List.of());
            }
            return new HologramModel(List.copyOf(elements));
        }

        private static double[] readVec3(com.google.gson.JsonObject object, String key) {
            if (!object.has(key) || !object.get(key).isJsonArray()) {
                return null;
            }
            com.google.gson.JsonArray array = object.getAsJsonArray(key);
            if (array.size() < 3) {
                return null;
            }
            return new double[] {
                    array.get(0).getAsDouble(),
                    array.get(1).getAsDouble(),
                    array.get(2).getAsDouble()
            };
        }
    }

    public record HologramElement(double fromX, double fromY, double fromZ,
                                  double toX, double toY, double toZ,
                                  @Nullable ElementRotation rotation) {
    }

    public record ElementRotation(double originX, double originY, double originZ,
                                  String axis, float angle, boolean rescale) {
    }
}

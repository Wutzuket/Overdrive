package de.wutzuket.overdrive.block.advanced_hologram;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.menu.AdvancedHologramMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdvancedHologramBlockEntity extends BlockEntity implements MenuProvider {
    private static final String TAG_SCRIPT_TEXT = "AdvancedScriptText";
    private static final String TAG_RUNNING = "AdvancedRunning";
    private static final String TAG_POWERED = "AdvancedPowered";
    private static final String TAG_SELECTED_SCRIPT_FILE = "AdvancedScriptFile";
    private static final String OWNER_INDEX_FILE = "owners.json";

    private static final String DEFAULT_SCRIPT = """
            -- ===== Advanced Hologram Block - Example Script =====
            -- cube(x, y, z, sizeX, sizeY, sizeZ, rotX, rotY, rotZ, {r,g,b})
            -- object:transition(x, y, z, sizeX, sizeY, sizeZ, rotX, rotY, rotZ, {r,g,b}, durationTicks)
            -- sleep(ticks) pauses the script.
            -- is_powered shows whether the block receives a redstone signal.

            local boden = cube(0, -0.5, 0, 2, 0.05, 2, 0, 0, 0, {0, 1, 0})
            local kern = cube(0, 0.6, 0, 0.8, 0.8, 0.8, 0, 0, 0, {0.2, 0.8, 1})

            local tick = 0
            local phase = false

            while true do
                tick = tick + 1

                if is_powered then
                    kern.ry = kern.ry + 2
                end

                if tick % 40 == 0 then
                    phase = not phase
                    if phase then
                        boden:transition(0, -0.5, 0, 3, 0.05, 3, 0, 0, 30, {1, 0.5, 0.2}, 40)
                        kern:transition(0, 0.9, 0, 1.4, 1.4, 1.4, 0, 0, 15, {1, 0.4, 0.8}, 40)
                    else
                        boden:transition(0, -0.5, 0, 2, 0.05, 2, 0, 0, 0, {0, 1, 0}, 40)
                        kern:transition(0, 0.6, 0, 0.8, 0.8, 0.8, 0, 0, 0, {0.2, 0.8, 1}, 40)
                    end
                end

                sleep(1)
            end
            """;

    private String scriptText = DEFAULT_SCRIPT;
    private boolean running;
    private boolean isPowered;

    private final List<String> availableScriptFiles = new ArrayList<>();
    private final Map<String, UUID> scriptOwners = new HashMap<>();
    private transient @Nullable Path scriptsDirectory;
    private @Nullable String selectedScriptFile;

    private final List<String> consoleLines = new ArrayList<>();
    private int consoleRevision;

    private transient AdvancedHologramScriptEngine scriptEngine;
    private volatile List<HoloElement> renderElements = List.of();
    private transient String lastError;
    private transient long scriptRevision;
    private transient @Nullable String pendingPrompt;

    public AdvancedHologramBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.ADVANCED_HOLOGRAM_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedHologramBlockEntity blockEntity) {
        blockEntity.syncPoweredState(level, pos);
        blockEntity.tickScript();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AdvancedHologramBlockEntity blockEntity) {
        blockEntity.tickScript();
    }

    public void syncPoweredState(Level level, BlockPos pos) {
        boolean powered = level != null && level.hasNeighborSignal(pos);
        if (powered != isPowered) {
            isPowered = powered;
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
            }
        }
    }

    private void tickScript() {
        if (scriptEngine == null) {
            scriptEngine = new AdvancedHologramScriptEngine(this);
        }
        scriptEngine.tick(scriptText, running, isPowered);
    }

    public List<HoloElement> getRenderElements() {
        return renderElements;
    }

    public void setRenderElements(List<HoloElement> renderElements) {
        this.renderElements = renderElements;
    }

    public long getScriptRevision() {
        return scriptRevision;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public void addConsoleLine(String line) {
        consoleLines.add(line);
        if (consoleLines.size() > 100) {
            consoleLines.remove(0);
        }
        consoleRevision++;
    }

    public List<String> getConsoleLines() {
        return Collections.unmodifiableList(consoleLines);
    }

    public int getConsoleRevision() {
        return consoleRevision;
    }

    public void clearConsole() {
        consoleLines.clear();
        consoleRevision++;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText == null ? "" : scriptText;
        markScriptDirty();
        syncChange();
    }

    public boolean isRunning() {
        return running;
    }

    public @Nullable String getPendingPrompt() {
        return pendingPrompt;
    }

    public void setPendingPrompt(@Nullable String pendingPrompt) {
        this.pendingPrompt = pendingPrompt;
    }

    public void submitScriptInput(String input) {
        if (scriptEngine == null) {
            scriptEngine = new AdvancedHologramScriptEngine(this);
        }
        scriptEngine.submitInput(input);
    }

    public void setRunning(boolean running) {
        this.running = running;
        markScriptDirty();
        syncChange();
    }

    public boolean isPowered() {
        return isPowered;
    }

    public int getSignalStrength() {
        if (level == null) {
            return 0;
        }
        for (Direction direction : Direction.values()) {
            int strength = level.getSignal(worldPosition.relative(direction), direction);
            if (strength > 0) {
                return strength;
            }
        }
        return 0;
    }

    public int getSignalStrength(Direction direction) {
        if (level == null || direction == null) {
            return 0;
        }
        return level.getSignal(worldPosition.relative(direction), direction.getOpposite());
    }

    private void markScriptDirty() {
        scriptRevision++;
    }

    private void syncChange() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            ensureScriptsDirectory(serverLevel);
            loadOwnerIndex(serverLevel);
            scanScriptFiles(serverLevel);
            reloadSelectedScript(serverLevel);
        }
    }

    public Path ensureScriptsDirectory(ServerLevel serverLevel) {
        if (scriptsDirectory != null) {
            return scriptsDirectory;
        }

        Path worldRoot = serverLevel.getServer().getWorldPath(LevelResource.ROOT);
        Path scriptsPath = worldRoot.resolve("holograms");
        try {
            Files.createDirectories(scriptsPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create holograms directory at " + scriptsPath, exception);
        }
        scriptsDirectory = scriptsPath;
        return scriptsPath;
    }

    public List<String> scanScriptFiles(ServerLevel serverLevel) {
        Path scriptsPath = ensureScriptsDirectory(serverLevel);
        availableScriptFiles.clear();

        try (var stream = Files.list(scriptsPath)) {
            stream.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".lua"))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(availableScriptFiles::add);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan holograms directory at " + scriptsPath, exception);
        }

        if (selectedScriptFile != null && !availableScriptFiles.contains(selectedScriptFile)) {
            selectedScriptFile = null;
            setChanged();
        }

        return Collections.unmodifiableList(new ArrayList<>(availableScriptFiles));
    }

    public List<String> getAvailableScriptFiles() {
        return Collections.unmodifiableList(availableScriptFiles);
    }

    public @Nullable String getSelectedScriptFile() {
        return selectedScriptFile;
    }

    public void setSelectedScriptFile(@Nullable String selectedScriptFile) {
        this.selectedScriptFile = selectedScriptFile;
        if (level instanceof ServerLevel serverLevel) {
            reloadSelectedScript(serverLevel);
        }
        syncChange();
    }

    public void reloadSelectedScript(ServerLevel serverLevel) {
        if (selectedScriptFile == null) {
            return;
        }
        Path selectedPath = ensureScriptsDirectory(serverLevel).resolve(selectedScriptFile);
        if (!Files.isRegularFile(selectedPath)) {
            return;
        }
        try {
            setScriptText(Files.readString(selectedPath));
        } catch (IOException ignored) {
        }
    }

    public boolean saveScriptFile(ServerLevel serverLevel, String fileName, String content) {
        try {
            Files.writeString(ensureScriptsDirectory(serverLevel).resolve(fileName), content);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public boolean deleteScriptFile(ServerLevel serverLevel, String fileName) {
        try {
            return Files.deleteIfExists(ensureScriptsDirectory(serverLevel).resolve(fileName));
        } catch (IOException ignored) {
            return false;
        }
    }

    public void loadOwnerIndex(ServerLevel serverLevel) {
        scriptOwners.clear();
        Path index = ensureScriptsDirectory(serverLevel).resolve(OWNER_INDEX_FILE);
        if (!Files.isRegularFile(index)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(index)).getAsJsonObject();
            root.asMap().forEach((fileName, value) -> {
                try {
                    scriptOwners.put(fileName, UUID.fromString(value.getAsString()));
                } catch (IllegalArgumentException ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void persistOwnerIndex(ServerLevel serverLevel) {
        JsonObject root = new JsonObject();
        scriptOwners.forEach((fileName, owner) -> root.addProperty(fileName, owner.toString()));
        try {
            Files.writeString(ensureScriptsDirectory(serverLevel).resolve(OWNER_INDEX_FILE),
                    new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (IOException ignored) {
        }
    }

    public @Nullable UUID getScriptOwner(String fileName) {
        return scriptOwners.get(fileName);
    }

    public void setScriptOwner(ServerLevel serverLevel, String fileName, UUID owner) {
        scriptOwners.put(fileName, owner);
        persistOwnerIndex(serverLevel);
    }

    public void removeScriptOwner(ServerLevel serverLevel, String fileName) {
        scriptOwners.remove(fileName);
        persistOwnerIndex(serverLevel);
    }

    public boolean isOwnedByPlayer(String fileName, UUID playerUuid) {
        if (playerUuid == null || fileName == null) {
            return false;
        }
        UUID owner = scriptOwners.get(fileName);
        return owner == null || owner.equals(playerUuid);
    }

    public List<String> getAvailableScriptFilesForPlayer(UUID playerUuid) {
        return availableScriptFiles.stream()
                .filter(fileName -> isOwnedByPlayer(fileName, playerUuid))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(TAG_SCRIPT_TEXT, scriptText);
        tag.putBoolean(TAG_RUNNING, running);
        tag.putBoolean(TAG_POWERED, isPowered);
        if (selectedScriptFile != null) {
            tag.putString(TAG_SELECTED_SCRIPT_FILE, selectedScriptFile);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        String newScriptText = tag.contains(TAG_SCRIPT_TEXT) ? tag.getString(TAG_SCRIPT_TEXT) : DEFAULT_SCRIPT;
        boolean newRunning = tag.contains(TAG_RUNNING) && tag.getBoolean(TAG_RUNNING);
        boolean newPowered = tag.contains(TAG_POWERED) && tag.getBoolean(TAG_POWERED);
        selectedScriptFile = tag.contains(TAG_SELECTED_SCRIPT_FILE) ? tag.getString(TAG_SELECTED_SCRIPT_FILE) : null;
        boolean scriptChanged = !newScriptText.equals(scriptText) || newRunning != running;
        scriptText = newScriptText;
        running = newRunning;
        isPowered = newPowered;
        if (scriptChanged) {
            scriptRevision++;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.advanced_hologram_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AdvancedHologramMenu(containerId, inventory, this);
    }
}
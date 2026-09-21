package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.advanced_hologram.AdvancedHologramBlockEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AdvancedHologramMenu extends AbstractContainerMenu {
    private final AdvancedHologramBlockEntity blockEntity;

    private String clientScriptText;
    private boolean clientRunning;
    private boolean clientPowered;
    private List<String> clientScriptFiles = List.of();
    private String clientSelectedScriptFile;
    private boolean clientSynced;

    public AdvancedHologramMenu(int containerId, Inventory inventory, AdvancedHologramBlockEntity blockEntity) {
        super(ODMenuTypes.ADVANCED_HOLOGRAM_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        if (blockEntity != null) {
            copyFromBlockEntity();
        }
    }

    public AdvancedHologramMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (AdvancedHologramBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }

    private void copyFromBlockEntity() {
        clientScriptText = blockEntity.getScriptText();
        clientRunning = blockEntity.isRunning();
        clientPowered = blockEntity.isPowered();
        clientScriptFiles = blockEntity.getAvailableScriptFiles();
        clientSelectedScriptFile = blockEntity.getSelectedScriptFile();
    }

    public void applyClientSync(String scriptText, boolean running, boolean powered,
                                List<String> scriptFiles, String selectedScriptFile) {
        this.clientScriptText = scriptText;
        this.clientRunning = running;
        this.clientPowered = powered;
        this.clientScriptFiles = scriptFiles == null ? List.of() : List.copyOf(scriptFiles);
        this.clientSelectedScriptFile = selectedScriptFile;
        this.clientSynced = true;
    }

    public boolean hasClientSync() {
        return clientSynced;
    }

    public String getScriptText() {
        return clientSynced ? clientScriptText : blockEntity.getScriptText();
    }

    public boolean isRunning() {
        return clientSynced ? clientRunning : blockEntity.isRunning();
    }

    public boolean isPowered() {
        return clientSynced ? clientPowered : blockEntity.isPowered();
    }

    public List<String> getAvailableScriptFiles() {
        return clientSynced ? clientScriptFiles : blockEntity.getAvailableScriptFiles();
    }

    public String getSelectedScriptFile() {
        return clientSynced ? clientSelectedScriptFile : blockEntity.getSelectedScriptFile();
    }

    public AdvancedHologramBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
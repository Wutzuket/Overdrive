package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.hologram.HologramBlockEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HologramMenu extends AbstractContainerMenu {
    private final HologramBlockEntity blockEntity;

    private List<String> clientFiles = List.of();
    private @Nullable String clientSelectedFile;
    private double clientOffsetX;
    private double clientOffsetY = 1.0D;
    private double clientOffsetZ;
    private double clientScale = 1.0D;
    private double clientRotationX;
    private double clientRotationY;
    private double clientRotationZ;
    private boolean clientSpin = true;
    private boolean clientSynced;

    public HologramMenu(int containerId, Inventory inventory, HologramBlockEntity blockEntity) {
        super(ODMenuTypes.HOLOGRAM_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        if (blockEntity != null) {
            copyFromBlockEntity();
        }
    }

    public HologramMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (HologramBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }

    private void copyFromBlockEntity() {
        clientFiles = List.copyOf(blockEntity.getAvailableJsonFiles());
        clientSelectedFile = blockEntity.getSelectedHologramFile();
        clientOffsetX = blockEntity.getOffsetX();
        clientOffsetY = blockEntity.getOffsetY();
        clientOffsetZ = blockEntity.getOffsetZ();
        clientScale = blockEntity.getScale();
        clientRotationX = blockEntity.getRotationX();
        clientRotationY = blockEntity.getRotationY();
        clientRotationZ = blockEntity.getRotationZ();
        clientSpin = blockEntity.getSpin();
    }

    public void applyClientSync(List<String> files, @Nullable String selectedFile,
                                double offsetX, double offsetY, double offsetZ,
                                double scale, double rotationX, double rotationY, double rotationZ,
                                boolean spin) {
        this.clientFiles = List.copyOf(files);
        this.clientSelectedFile = selectedFile;
        this.clientOffsetX = offsetX;
        this.clientOffsetY = offsetY;
        this.clientOffsetZ = offsetZ;
        this.clientScale = scale;
        this.clientRotationX = rotationX;
        this.clientRotationY = rotationY;
        this.clientRotationZ = rotationZ;
        this.clientSpin = spin;
        this.clientSynced = true;
    }

    public boolean hasClientSync() {
        return clientSynced;
    }

    public List<String> getAvailableFiles() {
        return clientSynced ? clientFiles : blockEntity.getAvailableJsonFiles();
    }

    public @Nullable String getSelectedFile() {
        return clientSynced ? clientSelectedFile : blockEntity.getSelectedHologramFile();
    }

    public double getOffsetX() {
        return clientSynced ? clientOffsetX : blockEntity.getOffsetX();
    }

    public double getOffsetY() {
        return clientSynced ? clientOffsetY : blockEntity.getOffsetY();
    }

    public double getOffsetZ() {
        return clientSynced ? clientOffsetZ : blockEntity.getOffsetZ();
    }

    public double getScale() {
        return clientSynced ? clientScale : blockEntity.getScale();
    }

    public double getRotationX() {
        return clientSynced ? clientRotationX : blockEntity.getRotationX();
    }

    public double getRotationY() {
        return clientSynced ? clientRotationY : blockEntity.getRotationY();
    }

    public double getRotationZ() {
        return clientSynced ? clientRotationZ : blockEntity.getRotationZ();
    }

    public boolean getSpin() {
        return clientSynced ? clientSpin : blockEntity.getSpin();
    }

    public HologramBlockEntity getBlockEntity() {
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

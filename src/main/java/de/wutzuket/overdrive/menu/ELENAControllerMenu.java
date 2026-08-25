package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.elena.ELENAControllerEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class ELENAControllerMenu extends AbstractContainerMenu {
    private final ELENAControllerEntity blockEntity;
    private final int[] data = new int[8];

    public ELENAControllerMenu(int containerId, Inventory inventory, ELENAControllerEntity blockEntity) {
        super(ODMenuTypes.ELENA_CONTROLLER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getEnergy();
                    case 1 -> blockEntity.getMaxEnergy();
                    case 2 -> blockEntity.isValidStructure() ? 1 : 0;
                    case 3 -> blockEntity.isActive() ? 1 : 0;
                    case 4 -> blockEntity.getProgressTicks();
                    case 5 -> blockEntity.isHologramEnabled() ? 1 : 0;
                    case 6 -> (int) (blockEntity.getVacuumLevel() * 1000);
                    case 7 -> blockEntity.getMaxProgressTicks();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < data.length) {
                    data[index] = value;
                }
            }

            @Override
            public int getCount() {
                return data.length;
            }
        });
    }

    public ELENAControllerMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (ELENAControllerEntity) inventory.player.level()
                .getBlockEntity(buffer.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public ELENAControllerEntity getBlockEntity() { return blockEntity; }
    public int getEnergy() { return data[0]; }
    public int getMaxEnergy() { return data[1]; }
    public boolean isValidStructure() { return data[2] == 1; }
    public boolean isActive() { return data[3] == 1; }
    public int getProgressTicks() { return data[4]; }
    public boolean isHologramEnabled() { return data[5] == 1; }

    public int getEnergyScaled(int max) {
        int maxEnergy = getMaxEnergy();
        if (maxEnergy <= 0) return 0;
        return (int) ((long) getEnergy() * max / maxEnergy);
    }

    public int getVacuumLevel() { return data[6]; }

    public int getVacuumScaled(int max) {
        return getVacuumLevel() * max / 1000;
    }

    public int getMaxProgressTicks() { return data[7]; }
}

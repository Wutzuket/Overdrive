package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlockEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class VacuumPumpMenu extends AbstractContainerMenu {
    private final VacuumPumpBlockEntity blockEntity;
    private final int[] data = new int[8];

    public VacuumPumpMenu(int containerId, Inventory inventory, VacuumPumpBlockEntity blockEntity) {
        super(ODMenuTypes.VACUUM_PUMP_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        data[0] = blockEntity.getVacuumProgress();
        data[1] = blockEntity.getStatus().ordinal();
        data[2] = blockEntity.isValidStructure() ? 1 : 0;
        data[3] = blockEntity.isHologramEnabled() ? 1 : 0;
        data[4] = blockEntity.getEnergyStored();
        data[5] = blockEntity.getMaxEnergyStored();
        data[6] = blockEntity.getVolume();
        data[7] = blockEntity.getEnergyUsage();
        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getVacuumProgress();
                    case 1 -> blockEntity.getStatus().ordinal();
                    case 2 -> blockEntity.isValidStructure() ? 1 : 0;
                    case 3 -> blockEntity.isHologramEnabled() ? 1 : 0;
                    case 4 -> blockEntity.getEnergyStored();
                    case 5 -> blockEntity.getMaxEnergyStored();
                    case 6 -> blockEntity.getVolume();
                    case 7 -> blockEntity.getEnergyUsage();
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

    public VacuumPumpMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (VacuumPumpBlockEntity) inventory.player.level()
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

    public VacuumPumpBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getEnergyScaled(int max) {
        int maxEnergy = getMaxEnergy();
        if (maxEnergy <= 0) return 0;
        return (int) ((long) getEnergy() * max / maxEnergy);
    }

    public int getVacuumProgress() {
        return data[0];
    }

    public int getVolume() {
        return data[6];
    }

    public int getEnergy() {
        return data[4];
    }

    public int getMaxEnergy() {
        return data[5];
    }

    public int getEnergyUsage() {
        return data[7];
    }

    public int getStatusOrdinal() {
        return data[1];
    }

    public boolean isValidStructure() {
        return data[2] == 1;
    }

    public boolean isHologramEnabled() {
        return data[3] == 1;
    }
}

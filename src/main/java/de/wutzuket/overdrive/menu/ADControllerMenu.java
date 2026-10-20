package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.ad.ADControllerEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class ADControllerMenu extends AbstractContainerMenu {
    private final ADControllerEntity blockEntity;
    private final int[] data = new int[13];

    public ADControllerMenu(int containerId, Inventory inventory, ADControllerEntity blockEntity) {
        super(ODMenuTypes.AD_CONTROLLER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) (blockEntity.getEnergyStoredLong() & 0xFFFFFFFFL);
                    case 1 -> (int) (blockEntity.getEnergyStoredLong() >>> 32);
                    case 2 -> blockEntity.isValidStructure() ? 1 : 0;
                    case 3 -> blockEntity.isActive() ? 1 : 0;
                    case 4 -> blockEntity.getProgressTicks();
                    case 5 -> blockEntity.isHologramEnabled() ? 1 : 0;
                    case 6 -> (int) (blockEntity.getVacuumLevel() * 1000);
                    case 7 -> blockEntity.getMaxProgressTicks();
                    case 8 -> blockEntity.getEnergyRate();
                    case 9 -> blockEntity.getChargeScaled();
                    case 10 -> blockEntity.getChargeTicksElapsed();
                    case 11 -> blockEntity.getChargeTicksTotal();
                    // Produktion in mB/t, x100 als Festkomma (Brokerrechnung).
                    case 12 -> (int) Math.round(blockEntity.getProductionPerTick() * 100);
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

    public ADControllerMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (ADControllerEntity) inventory.player.level()
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

    public ADControllerEntity getBlockEntity() { return blockEntity; }

    public long getEnergyLong() {
        return ((long) data[1] << 32) | (data[0] & 0xFFFFFFFFL);
    }

    public long getMaxEnergyLong() {
        return ADControllerEntity.MAX_ENERGY_STORED;
    }

    public int getMaxRate() {
        return ADControllerEntity.MAX_ENERGY_RATE;
    }
    public boolean isValidStructure() { return data[2] == 1; }
    public boolean isActive() { return data[3] == 1; }
    public int getProgressTicks() { return data[4]; }
    public boolean isHologramEnabled() { return data[5] == 1; }

    public int getEnergyScaled(int max) {
        long maxEnergy = getMaxEnergyLong();
        if (maxEnergy <= 0) return 0;
        return (int) (getEnergyLong() * max / maxEnergy);
    }

    public int getVacuumLevel() { return data[6]; }

    public int getVacuumScaled(int max) {
        return getVacuumLevel() * max / 1000;
    }

    public int getMaxProgressTicks() { return data[7]; }

    public int getEnergyRateSynced() { return data[8]; }

    public int getChargeScaled() { return Math.min(200, Math.max(0, data[9])); }

    public int getChargeTicksElapsed() { return Math.max(0, data[10]); }

    public int getChargeTicksTotal() { return Math.max(1, data[11]); }

    /** Produktion der Maschine in mB pro Tick (z.B. 20000 -> "20 B/t"). */
    public double getProductionPerTickSynced() { return data[12] / 100.0; }
}

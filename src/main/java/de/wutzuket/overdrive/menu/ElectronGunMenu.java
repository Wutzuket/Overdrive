package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.tungsten.ElectronGunEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class ElectronGunMenu extends AbstractContainerMenu {
    private final ElectronGunEntity blockEntity;
    private final int[] data = new int[10];

    public ElectronGunMenu(int containerId, Inventory inventory, ElectronGunEntity blockEntity) {
        super(ODMenuTypes.ELECTRON_GUN_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getEnergyStored();
                    case 1 -> blockEntity.getMaxEnergyStored();
                    case 2 -> (int) (blockEntity.getVacuumLevel() * 1000);
                    case 3 -> blockEntity.isHologramEnabled() ? 1 : 0;
                    case 4 -> blockEntity.getAntimatterTank().getAmount(0);
                    case 5 -> blockEntity.getAntimatterTank().getCapacity(0);
                    case 6 -> blockEntity.isActive() ? 1 : 0;
                    case 7 -> blockEntity.getProgressTicks();
                    case 8 -> blockEntity.getCurrentRecipeProcessTime();
                    // Produktion in mB/t, x100 als Festkomma (Brokerrechnung).
                    case 9 -> (int) Math.round(blockEntity.getProductionPerTick() * 100);
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

    public ElectronGunMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (ElectronGunEntity) inventory.player.level()
                .getBlockEntity(buffer.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    public ElectronGunEntity getBlockEntity() { return blockEntity; }

    public int getEnergy() { return data[0]; }
    public int getMaxEnergy() { return data[1]; }
    public int getVacuumLevel() { return data[2]; }
    public boolean isHologramEnabled() { return data[3] == 1; }
    public int getFluidAmount() { return data[4]; }
    public int getFluidCapacity() { return data[5]; }
    public boolean isActive() { return data[6] == 1; }
    public int getProgressTicks() { return data[7]; }
    public int getRecipeProcessTime() { return data[8]; }

    /** Produktion der Kanone in mB pro Tick (z.B. 4000 -> "4 B/t"). */
    public double getProductionPerTickSynced() { return data[9] / 100.0; }

    public int getEnergyScaled(int max) {
        int maxEnergy = getMaxEnergy();
        if (maxEnergy <= 0) return 0;
        return (int) ((long) getEnergy() * max / maxEnergy);
    }

    public int getVacuumScaled(int max) {
        return getVacuumLevel() * max / 1000;
    }

    public int getProgressScaled(int max) {
        int recipeTime = getRecipeProcessTime();
        if (recipeTime <= 0) return 0;
        return (int) ((long) getProgressTicks() * max / recipeTime);
    }
}

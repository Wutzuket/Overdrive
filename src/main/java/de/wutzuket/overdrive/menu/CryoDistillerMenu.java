package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.distiller.CryoDistillerEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class CryoDistillerMenu extends AbstractContainerMenu {
    /** Indizes der synchronisierten Werte. */
    private static final int ENERGY = 0;
    private static final int MAX_ENERGY = 1;
    private static final int PROGRESS = 2;
    private static final int MAX_PROGRESS = 3;
    private static final int ACTIVE = 4;

    private final CryoDistillerEntity be;
    /**
     * Die Werte, die der Server schickt. Nur der Client liest daraus, damit
     * das GUI jeden Tick aktuell ist.
     */
    private final int[] data = new int[5];

    public CryoDistillerMenu(int id, Inventory inv, CryoDistillerEntity be) {
        super(ODMenuTypes.CRYO_DISTILLER_MENU.get(), id);
        this.be = be;

        data[ENERGY] = be.getEnergyStored();
        data[MAX_ENERGY] = be.getMaxEnergyStored();
        data[PROGRESS] = be.getProgress();
        data[MAX_PROGRESS] = be.getMaxProgress();
        data[ACTIVE] = be.isActive() ? 1 : 0;

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case ENERGY -> be.getEnergyStored();
                    case MAX_ENERGY -> be.getMaxEnergyStored();
                    case PROGRESS -> be.getProgress();
                    case MAX_PROGRESS -> be.getMaxProgress();
                    case ACTIVE -> be.isActive() ? 1 : 0;
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

    public CryoDistillerMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, (CryoDistillerEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public CryoDistillerEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() { return data[ENERGY]; }
    public int getMaxEnergy() { return data[MAX_ENERGY]; }
    public int getProgress() { return data[PROGRESS]; }
    public int getMaxProgress() { return data[MAX_PROGRESS]; }
    public boolean isActive() { return data[ACTIVE] == 1; }
}

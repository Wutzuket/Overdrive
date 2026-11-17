package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.scanner.scanner_blockentity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class scanner_menu extends AbstractContainerMenu {
    private static final int ITEM_SLOTS = 4;

    /** Indizes der synchronisierten Werte. */
    private static final int ENERGY = 0;
    private static final int MAX_ENERGY = 1;

    private final scanner_blockentity scanner;
    /**
     * Die Werte, die der Server schickt. Nur der Client liest daraus, damit
     * das GUI jeden Tick aktuell ist.
     */
    private final int[] data = new int[2];

    public scanner_menu(int id, Inventory inventory, scanner_blockentity scanner) {
        super(ODMenuTypes.SCANNER_MENU.get(), id);
        this.scanner = scanner;

        for (int i = 0; i < ITEM_SLOTS; i++) {
            int x = 26 + (i % 2) * 22;
            int y = 20 + (i / 2) * 22;
            addSlot(new SlotItemHandler(scanner.getItems(), i, x, y));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        data[ENERGY] = scanner.getEnergyStored();
        data[MAX_ENERGY] = scanner.getMaxEnergyStored();

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case ENERGY -> scanner.getEnergyStored();
                    case MAX_ENERGY -> scanner.getMaxEnergyStored();
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

    public scanner_menu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (scanner_blockentity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < ITEM_SLOTS) {
            if (!moveItemStackTo(stack, ITEM_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, ITEM_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return scanner.getBlockPos().distSqr(player.blockPosition()) <= 64;
    }

    public scanner_blockentity getBlockEntity() {
        return scanner;
    }

    public int getEnergy() { return data[ENERGY]; }
    public int getMaxEnergy() { return data[MAX_ENERGY]; }
}

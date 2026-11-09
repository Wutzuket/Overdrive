package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.scanner.scanner_blockentity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class scanner_menu extends AbstractContainerMenu {
    private static final int ITEM_SLOTS = 4;

    private final scanner_blockentity scanner;

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
}

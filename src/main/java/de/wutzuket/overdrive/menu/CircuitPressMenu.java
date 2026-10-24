package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.press.CircuitPressBlockEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class CircuitPressMenu extends AbstractContainerMenu {
    private final CircuitPressBlockEntity press;

    public CircuitPressMenu(int id, Inventory inventory, CircuitPressBlockEntity press) {
        super(ODMenuTypes.CIRCUIT_PRESS_MENU.get(), id);
        this.press = press;
        for (int i = 0; i < 4; i++) {
            int x = 28 + (i % 2) * 22;
            int y = 34 + (i / 2) * 22;
            addSlot(new SlotItemHandler(press.getItems(), i, x, y));
        }
        addSlot(new SlotItemHandler(press.getItems(), 4, 140, 45) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 110 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 168));
    }

    public CircuitPressMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (CircuitPressBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 5) {
            if (!moveItemStackTo(stack, 5, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 4, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }
    @Override public boolean stillValid(Player player) { return press.getBlockPos().distSqr(player.blockPosition()) <= 64; }
    public CircuitPressBlockEntity getBlockEntity() { return press; }
}

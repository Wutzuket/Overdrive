package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.press.CircuitPressBlockEntity;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class CircuitPressMenu extends AbstractContainerMenu {
    /** Indizes der synchronisierten Werte. */
    private static final int ENERGY = 0;
    private static final int MAX_ENERGY = 1;
    private static final int PROGRESS = 2;
    private static final int MAX_PROGRESS = 3;
    private static final int ACTIVE = 4;

    private final CircuitPressBlockEntity press;
    /**
     * Die Werte, die der Server schickt. Nur der Client liest daraus, damit
     * das GUI jeden Tick aktuell ist.
     */
    private final int[] data = new int[5];

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

        data[ENERGY] = press.getEnergyStored();
        data[MAX_ENERGY] = press.getMaxEnergyStored();
        data[PROGRESS] = press.getProgress();
        data[MAX_PROGRESS] = press.getMaxProgress();
        data[ACTIVE] = press.isActive() ? 1 : 0;

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case ENERGY -> press.getEnergyStored();
                    case MAX_ENERGY -> press.getMaxEnergyStored();
                    case PROGRESS -> press.getProgress();
                    case MAX_PROGRESS -> press.getMaxProgress();
                    case ACTIVE -> press.isActive() ? 1 : 0;
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
        } else if (!moveItemStackTo(stack, 0, 5, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }
    @Override public boolean stillValid(Player player) { return press.getBlockPos().distSqr(player.blockPosition()) <= 64; }
    public CircuitPressBlockEntity getBlockEntity() { return press; }

    public int getEnergy() { return data[ENERGY]; }
    public int getMaxEnergy() { return data[MAX_ENERGY]; }
    public int getProgress() { return data[PROGRESS]; }
    public int getMaxProgress() { return data[MAX_PROGRESS]; }
    public boolean isActive() { return data[ACTIVE] == 1; }
    public int getEnergyPerTick() { return ODConfig.CIRCUIT_PRESS_ENERGY_PER_TICK.get(); }
}

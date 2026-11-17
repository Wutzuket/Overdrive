package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.antimatter.AntimatterTankEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class AntimatterTankMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT_X = 44;
    public static final int INPUT_SLOT_Y = 35;
    public static final int OUTPUT_SLOT_X = 116;
    public static final int OUTPUT_SLOT_Y = 35;

    private final AntimatterTankEntity blockEntity;
    /** Indizes der synchronisierten Werte. */
    private static final int AMOUNT = 0;
    private static final int CAPACITY = 1;
    /**
     * Die Werte, die der Server schickt. Nur der Client liest daraus, damit
     * das GUI jeden Tick aktuell ist.
     */
    private final int[] data = new int[2];

    public AntimatterTankMenu(int id, Inventory inventory, AntimatterTankEntity blockEntity) {
        super(ODMenuTypes.ANTIMATTER_TANK_MENU.get(), id);
        this.blockEntity = blockEntity;

        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), AntimatterTankEntity.INPUT_SLOT,
                INPUT_SLOT_X, INPUT_SLOT_Y));
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), AntimatterTankEntity.OUTPUT_SLOT,
                OUTPUT_SLOT_X, OUTPUT_SLOT_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 110 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 168));
        }

        data[AMOUNT] = blockEntity.getAmount(0);
        data[CAPACITY] = blockEntity.getCapacity(0);

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case AMOUNT -> blockEntity.getAmount(0);
                    case CAPACITY -> blockEntity.getCapacity(0);
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

    public AntimatterTankMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, (AntimatterTankEntity) inventory.player.level()
                .getBlockEntity(buffer.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
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
        return true;
    }

    public AntimatterTankEntity getBlockEntity() {
        return blockEntity;
    }

    public int getAmount() {
        return data[AMOUNT];
    }

    public int getCapacity() {
        return data[CAPACITY];
    }
}

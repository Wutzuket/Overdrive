package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.tungsten.PositronVacuumOutputPortEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PositronVacuumOutputPortMenu extends AbstractContainerMenu {
    private final PositronVacuumOutputPortEntity blockEntity;
    private final int[] data = new int[3];

    public PositronVacuumOutputPortMenu(int containerId, Inventory inventory, PositronVacuumOutputPortEntity blockEntity) {
        super(ODMenuTypes.POSITRON_VACUUM_OUTPUT_PORT_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        IItemHandler itemHandler = blockEntity.getItemHandler();
        // Container Slot (oben rechts)
        this.addSlot(new SlotItemHandler(itemHandler, 0, 148, 35));

        // Spieler-Inventar (3 Reihen x 9 Slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Spieler-Hotbar (1 Reihe x 9 Slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getTank().getAmount(0);
                    case 1 -> blockEntity.getTank().getCapacity(0);
                    // Index des Antimatter-Typs im Tank (fuer die farbige Anzeige).
                    case 2 -> {
                        var type = blockEntity.getTank().getAntimatterInTank(0);
                        yield type == null ? -1 : de.wutzuket.overdrive.antimatter.ODAntimatters.getTypes().indexOf(type);
                    }
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < data.length) data[index] = value;
            }

            @Override
            public int getCount() { return data.length; }
        });
    }

    public PositronVacuumOutputPortMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (PositronVacuumOutputPortEntity) inventory.player.level()
                .getBlockEntity(buffer.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    public PositronVacuumOutputPortEntity getBlockEntity() { return blockEntity; }

    public int getFluidAmount() { return data[0]; }
    public int getFluidCapacity() { return data[1]; }

    /** @return der Antimatter-Typ im Tank oder {@code null}, wenn der Tank leer ist. */
    public de.wutzuket.overdrive.antimatter.Antimatter getAntimatterType() {
        int index = data[2];
        var types = de.wutzuket.overdrive.antimatter.ODAntimatters.getTypes();
        if (index < 0 || index >= types.size()) {
            return null;
        }
        return types.get(index);
    }
}
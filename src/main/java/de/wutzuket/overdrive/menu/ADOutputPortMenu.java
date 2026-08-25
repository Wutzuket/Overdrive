package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.ad.ADOutputPortEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ADOutputPortMenu extends AbstractContainerMenu {
    private final ADOutputPortEntity blockEntity;
    private final ContainerData data;

    public ADOutputPortMenu(int containerId, Inventory inventory, ADOutputPortEntity blockEntity) {
        super(ODMenuTypes.AD_OUTPUT_PORT_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 134, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getFluidAmount();
                    case 1 -> blockEntity.getFluidCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        addDataSlots(data);
    }

    public ADOutputPortMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (ADOutputPortEntity) inventory.player.level()
                .getBlockEntity(buffer.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex == 0) {
            if (!moveItemStackTo(stack, 1, 37, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                if (slotIndex < 28) {
                    if (!moveItemStackTo(stack, 28, 37, false)) return ItemStack.EMPTY;
                } else {
                    if (!moveItemStackTo(stack, 1, 28, false)) return ItemStack.EMPTY;
                }
            }
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getFluidAmount() { return data.get(0); }
    public int getFluidCapacity() { return data.get(1); }
    public ADOutputPortEntity getBlockEntity() { return blockEntity; }
}

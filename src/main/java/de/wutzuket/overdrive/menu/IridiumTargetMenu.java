package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.accelerator.IridiumTargetEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class IridiumTargetMenu extends AbstractContainerMenu {
    private final IridiumTargetEntity be;
    private final int[] data = new int[3];

    public IridiumTargetMenu(int containerId, Inventory inventory, IridiumTargetEntity be) {
        super(ODMenuTypes.IRIDIUM_TARGET_MENU.get(), containerId);
        this.be = be;
        data[0] = be.getTank().getFluidAmount();
        data[1] = be.getTank().getCapacity();
        data[2] = fluidId(be.getTank().getFluid());

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                if (be == null) return 0;
                return switch (index) {
                    case 0 -> be.getTank().getFluidAmount();
                    case 1 -> be.getTank().getCapacity();
                    case 2 -> fluidId(be.getTank().getFluid());
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

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    public IridiumTargetMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (IridiumTargetEntity) inventory.player.level()
                .getBlockEntity(buffer.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < 27) {
            if (!moveItemStackTo(stack, 27, slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, 27, false)) {
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

    public IridiumTargetEntity getBlockEntity() {
        return be;
    }

    // Synced getters for Screen (auf Client aus ContainerData)
    public int getFluidAmount() {
        return data[0];
    }

    public int getFluidCapacity() {
        return data[1];
    }

    private static int fluidId(FluidStack stack) {
        if (stack.isEmpty()) return -1;
        return BuiltInRegistries.FLUID.getId(stack.getFluid());
    }

    private static FluidStack stackById(int id, int amount) {
        if (id < 0 || amount <= 0) return FluidStack.EMPTY;
        var f = BuiltInRegistries.FLUID.byId(id);
        if (f == null || f == Fluids.EMPTY) return FluidStack.EMPTY;
        return new FluidStack(f, amount);
    }

    public FluidStack getFluidStack() {
        return stackById(data[2], data[0]);
    }

    public static String fluidName(FluidStack stack, String fallback) {
        if (stack.isEmpty()) return fallback;
        return stack.getHoverName().getString();
    }
}

package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.coalgenerator.CoalGeneratorEntity;
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

public class CoalGeneratorMenu extends AbstractContainerMenu {
    public static final int FUEL_SLOT_X = 28;
    public static final int FUEL_SLOT_Y = 34;

    /** Indizes der synchronisierten Werte. */
    private static final int ENERGY = 0;
    private static final int MAX_ENERGY = 1;
    private static final int ACTIVE = 2;
    private static final int BURN_TIME = 3;
    private static final int CURRENT_BURN_TIME = 4;

    private final CoalGeneratorEntity blockEntity;
    /**
     * Die Werte, die der Server schickt. Nur der Client liest daraus, damit
     * das GUI jeden Tick aktuell ist.
     */
    private final int[] data = new int[5];

    public CoalGeneratorMenu(int id, Inventory inv, CoalGeneratorEntity blockEntity) {
        super(ODMenuTypes.COAL_GENERATOR_MENU.get(), id);
        this.blockEntity = blockEntity;

        this.addSlot(new SlotItemHandler(blockEntity.getFuelHandler(), 0, FUEL_SLOT_X, FUEL_SLOT_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 110 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 168));
        }

        data[ENERGY] = blockEntity.getEnergyStored();
        data[MAX_ENERGY] = blockEntity.getMaxEnergyStored();
        data[ACTIVE] = blockEntity.isActive() ? 1 : 0;
        data[BURN_TIME] = blockEntity.getBurnTime();
        data[CURRENT_BURN_TIME] = blockEntity.getCurrentBurnTime();

        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case ENERGY -> blockEntity.getEnergyStored();
                    case MAX_ENERGY -> blockEntity.getMaxEnergyStored();
                    case ACTIVE -> blockEntity.isActive() ? 1 : 0;
                    case BURN_TIME -> blockEntity.getBurnTime();
                    case CURRENT_BURN_TIME -> blockEntity.getCurrentBurnTime();
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

    public CoalGeneratorMenu(int id, Inventory inv, RegistryFriendlyByteBuf buffer) {
        this(id, inv, (CoalGeneratorEntity) inv.player.level().getBlockEntity(buffer.readBlockPos()));
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
    public boolean stillValid(Player player) {
        return true;
    }

    public CoalGeneratorEntity getBlockEntity() {
        return blockEntity;
    }

    public int getEnergy() {
        return data[ENERGY];
    }

    public int getMaxEnergy() {
        return data[MAX_ENERGY];
    }

    public boolean isActive() {
        return data[ACTIVE] == 1;
    }

    public int getBurnTime() {
        return data[BURN_TIME];
    }

    public int getCurrentBurnTime() {
        return data[CURRENT_BURN_TIME];
    }

    public int getEnergyPerTick() {
        return ODConfig.COAL_GENERATOR_FE_PER_TICK.get();
    }

    public int getEnergyScaled(int max) {
        int maxEnergy = getMaxEnergy();
        if (maxEnergy <= 0) return 0;
        return (int) ((long) getEnergy() * max / maxEnergy);
    }
}

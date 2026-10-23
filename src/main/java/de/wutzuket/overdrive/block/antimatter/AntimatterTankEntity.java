package de.wutzuket.overdrive.block.antimatter;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.AntimatterTank;
import de.wutzuket.overdrive.antimatter.IAntimatterHandler;
import de.wutzuket.overdrive.antimatter.ODAntimatters;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.item.AntimatterCellItem;
import de.wutzuket.overdrive.menu.AntimatterTankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity of the antimatter tank. It holds the actual antimatter and provides the two item
 * slots that are needed to fill and empty cells.
 */
public class AntimatterTankEntity extends BlockEntity implements IAntimatterHandler, MenuProvider {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final AntimatterTank tank = new AntimatterTank(ODConfig.ANTIMATTER_TANK_CAPACITY.get());
    /** A creative tank always has a full amount of its antimatter. */
    private boolean creative;
    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == OUTPUT_SLOT) {
                return false;
            }
            return stack.getItem() instanceof AntimatterCellItem;
        }
    };

    public AntimatterTankEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.ANTIMATTER_TANK.get(), pos, state);
    }

    // --- ticking -----------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, AntimatterTankEntity tankEntity) {
        if (level.isClientSide) {
            return;
        }
        tankEntity.processCell();
    }

    /**
     * Moves the antimatter of the cell in the input slot into the tank or the other way round. The
     * processed cell always ends up in the output slot. Only one cell is processed per cycle, a
     * cell that is filled is taken out of the input slot and put into the output slot.
     */
    public void processCell() {
        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) {
            return;
        }
        if (!(input.getItem() instanceof AntimatterCellItem cell)) {
            return;
        }
        if (!items.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return;
        }

        int count = input.getCount();
        int amount = cell.getAmount(input);
        Antimatter type = cell.getType(input);

        if (amount <= 0) {
            fillCell(cell, input, count, type);
        } else {
            emptyCell(cell, input, count, amount, type);
        }
        setChanged();
    }

    /** Fills an empty cell with the antimatter of the tank. */
    private void fillCell(AntimatterCellItem cell, ItemStack input, int count, @Nullable Antimatter type) {
        Antimatter tankType = tank.getAntimatterInTank(0);
        if (tankType == null || tank.getAmount(0) <= 0) {
            return;
        }
        if (type != null && !type.equals(tankType)) {
            return;
        }
        int wanted = Math.min(cell.getCapacity(), tank.getAmount(0));
        if (wanted <= 0) {
            return;
        }
        int extracted = tank.extractAntimatter(0, wanted, false);
        if (extracted <= 0) {
            return;
        }
        ItemStack filled = new ItemStack(ODItems.getCellFor(tankType));
        cell.setContent(filled, tankType, extracted);
        takeOneCell(input, count);
        items.setStackInSlot(OUTPUT_SLOT, filled);
    }

    /** Empties a full cell into the tank. */
    private void emptyCell(AntimatterCellItem cell, ItemStack input, int count, int amount, @Nullable Antimatter type) {
        if (type == null || amount <= 0) {
            return;
        }
        Antimatter tankType = tank.getAntimatterInTank(0);
        if (tankType != null && !tankType.equals(type)) {
            return;
        }
        // the amount is the content of the cell, not the number of cells in the stack
        int total = amount;
        int space = tank.getCapacity(0) - tank.getAmount(0);
        if (space < total) {
            return;
        }
        int inserted = tank.insertAntimatter(0, type, total, false);
        if (inserted < total) {
            return;
        }
        ItemStack empty = new ItemStack(ODItems.EMPTY_ANTIMATTER_CELL.get());
        takeOneCell(input, count);
        items.setStackInSlot(OUTPUT_SLOT, empty);
    }

    /** Takes one cell out of the input slot, the rest stays in the slot. */
    private void takeOneCell(ItemStack input, int count) {
        if (count > 1) {
            input.shrink(1);
            items.setStackInSlot(INPUT_SLOT, input);
        } else {
            items.setStackInSlot(INPUT_SLOT, ItemStack.EMPTY);
        }
    }

    // --- capability --------------------------------------------------------

    @Override
    public int getTanks() {
        return tank.getTanks();
    }

    @Override
    @Nullable
    public Antimatter getAntimatterInTank(int tankIndex) {
        return tank.getAntimatterInTank(tankIndex);
    }

    @Override
    public int getAmount(int tankIndex) {
        return tank.getAmount(tankIndex);
    }

    @Override
    public int getCapacity(int tankIndex) {
        return tank.getCapacity(0);
    }

    @Override
    public int insertAntimatter(int tankIndex, @Nullable Antimatter type, int amount, boolean simulate) {
        if (creative) {
            // ein kreativer Tank hat unendlich viel, er nimmt alles an und gibt alles her
            if (type == null || !type.equals(tank.getAntimatterInTank(0))) {
                return 0;
            }
            return amount;
        }
        int inserted = tank.insertAntimatter(tankIndex, type, amount, simulate);
        if (inserted > 0 && !simulate) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return inserted;
    }

    @Override
    public int extractAntimatter(int tankIndex, int amount, boolean simulate) {
        if (creative) {
            // der Inhalt eines kreativen Tanks geht nie aus
            if (tank.getAmount(0) <= 0) {
                return 0;
            }
            return amount;
        }
        int extracted = tank.extractAntimatter(tankIndex, amount, simulate);
        if (extracted > 0 && !simulate) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return extracted;
    }

    /**
     * Turns this tank into a creative tank that always holds the given type of antimatter.
     *
     * @param creative true to make the tank creative, false to make it normal again
     * @param type the type of antimatter the creative tank creates
     */
    public void setCreative(boolean creative, @Nullable Antimatter type) {
        this.creative = creative;
        if (creative) {
            tank.setCapacity(ODConfig.ANTIMATTER_TANK_CAPACITY.get());
            tank.setContent(type, tank.getCapacity(0));
        }
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /** @return true when this tank always has a full amount of antimatter. */
    public boolean isCreative() {
        return creative;
    }

    public AntimatterTank getTank() {
        return tank;
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public ItemStack getInputStack() {
        return items.getStackInSlot(INPUT_SLOT);
    }

    public ItemStack getOutputStack() {
        return items.getStackInSlot(OUTPUT_SLOT);
    }

    /** @return the fill level of the tank in the range 0..1. */
    public float getFillLevel() {
        int capacity = tank.getCapacity(0);
        return capacity <= 0 ? 0 : (float) tank.getAmount(0) / capacity;
    }

    // --- menu --------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.antimatter_tank");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AntimatterTankMenu(id, inventory, this);
    }

    // --- syncing -----------------------------------------------------------

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("amount", tank.getAmount(0));
        if (tank.getAntimatterInTank(0) != null) {
            tag.putString("type", tank.getAntimatterInTank(0).id().toString());
        }
        tag.putInt("capacity", tank.getCapacity(0));
        tag.putBoolean("creative", creative);
        tag.put("items", items.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // the capacity has to be known before the content is loaded, otherwise the amount would be
        // cut down when the capacity of the tank was changed in the config
        if (tag.contains("capacity")) {
            tank.setCapacity(tag.getInt("capacity"));
        }
        creative = tag.getBoolean("creative");
        int amount = Math.max(0, tag.getInt("amount"));
        Antimatter type = null;
        if (tag.contains("type")) {
            type = ODAntimatters.get(ResourceLocation.parse(tag.getString("type")));
        }
        tank.setContent(type, amount);
    }
}

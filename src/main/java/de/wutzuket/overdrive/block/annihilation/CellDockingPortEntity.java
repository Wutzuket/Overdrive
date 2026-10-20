package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.AntimatterTank;
import de.wutzuket.overdrive.antimatter.ODAntimatters;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.item.AntimatterCellItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CellDockingPortEntity extends BlockEntity {
    // 2 KB (2.000.000 mB) pro Seite
    private final AntimatterTank tank = new AntimatterTank(2_000_000);

    /** Slot 0 nimmt die Zelle auf, Slot 1 ist die leere Zelle, die zurueckgegeben wird. */
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
            return slot == 0 && stack.getItem() instanceof AntimatterCellItem;
        }
    };

    public CellDockingPortEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.ANNIHILATION_CELL_PORT.get(), pos, state); }
    public AntimatterTank getTank(){ return tank; }

    /** @return the slots of the port, the first one takes the cell, the second one the empty cell. */
    public ItemStackHandler getItemHandler(){ return items; }

    public static void tick(Level level, BlockPos pos, BlockState state, CellDockingPortEntity port) {
        if (level.isClientSide) {
            return;
        }
        port.processCell();
    }

    /**
     * Takes the antimatter out of the cell in the input slot and puts it into the tank. The empty
     * cell ends up in the output slot, so the port can be filled again right away.
     */
    public void processCell() {
        ItemStack input = items.getStackInSlot(0);
        if (input.isEmpty() || !items.getStackInSlot(1).isEmpty()) {
            return;
        }
        if (!(input.getItem() instanceof AntimatterCellItem cell)) {
            return;
        }
        int amount = cell.getAmount(input);
        Antimatter type = cell.getType(input);
        if (type == null || amount <= 0) {
            return;
        }
        // der Tank darf nur eine Sorte enthalten
        if (tank.getAntimatterInTank(0) != null && !tank.getAntimatterInTank(0).equals(type)) {
            return;
        }
        int space = tank.getCapacity(0) - tank.getAmount(0);
        if (space <= 0) {
            return;
        }
        int moved = Math.min(space, amount);
        int inserted = tank.insertAntimatter(0, type, moved, false);
        if (inserted < moved) {
            return;
        }
        // die Zelle leeren und in den Ausgabeschlitten legen
        ItemStack empty = new ItemStack(ODItems.EMPTY_ANTIMATTER_CELL.get());
        items.setStackInSlot(1, empty);
        if (input.getCount() > 1) {
            input.shrink(1);
            items.setStackInSlot(0, input);
        } else {
            items.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public boolean hasAntiprotonFluid(){
        Antimatter type = tank.getAntimatterInTank(0);
        if (type == null || tank.getAmount(0) <= 0) return false;
        return type.equals(ODAntimatters.ANTIPROTONS)
                || type.equals(ODAntimatters.DECELERATED_ANTIPROTONS)
                || type.equals(ODAntimatters.COOLED_ANTIPROTONS);
    }
    public boolean hasPositronFluid(){
        Antimatter type = tank.getAntimatterInTank(0);
        return type != null && type.equals(ODAntimatters.POSITRONS) && tank.getAmount(0) > 0;
    }
    public boolean consumeFluid(int amount){
        return tank.extractAntimatter(0, amount, false) == amount;
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){
        super.saveAdditional(tag,r);
        CompoundTag tankTag = new CompoundTag();
        tank.saveToNBT(tankTag);
        tag.put("tank", tankTag);
        tag.put("items", items.serializeNBT(r));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){
        super.loadAdditional(tag,r);
        if (tag.contains("tank")) tank.loadFromNBT(tag.getCompound("tank"));
        if (tag.contains("items")) items.deserializeNBT(r, tag.getCompound("items"));
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}

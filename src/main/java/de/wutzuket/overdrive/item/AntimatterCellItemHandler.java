package de.wutzuket.overdrive.item;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.IAntimatterHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes an antimatter cell through the common antimatter item capability.
 */
public final class AntimatterCellItemHandler implements IAntimatterHandler {

    private final ItemStack stack;
    private final AntimatterCellItem cell;

    public AntimatterCellItemHandler(ItemStack stack) {
        this.stack = stack;
        this.cell = (AntimatterCellItem) stack.getItem();
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    @Nullable
    public Antimatter getAntimatterInTank(int tank) {
        return tank == 0 && getAmount(0) > 0 ? cell.getType(stack) : null;
    }

    @Override
    public int getAmount(int tank) {
        return tank == 0 ? cell.getAmount(stack) : 0;
    }

    @Override
    public int getCapacity(int tank) {
        return tank == 0 ? cell.getCapacity() : 0;
    }

    @Override
    public int insertAntimatter(int tank, @Nullable Antimatter type, int amount, boolean simulate) {
        if (tank != 0 || type == null || amount <= 0 || !isCompatibleWithCell(type)) {
            return 0;
        }

        int accepted = Math.min(amount, getCapacity(0) - getAmount(0));
        if (accepted > 0 && !simulate) {
            cell.setContent(stack, type, getAmount(0) + accepted);
        }
        return accepted;
    }

    private boolean isCompatibleWithCell(Antimatter type) {
        Antimatter stored = cell.getType(stack);
        return stored == null || stored.equals(type);
    }

    @Override
    public int extractAntimatter(int tank, int amount, boolean simulate) {
        if (tank != 0 || amount <= 0) {
            return 0;
        }

        int extracted = Math.min(amount, getAmount(0));
        if (extracted > 0 && !simulate) {
            Antimatter type = cell.getType(stack);
            cell.setContent(stack, type, getAmount(0) - extracted);
        }
        return extracted;
    }
}

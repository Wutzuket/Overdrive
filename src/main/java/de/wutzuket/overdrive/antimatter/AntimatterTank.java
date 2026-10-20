package de.wutzuket.overdrive.antimatter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * A simple single tank implementation of {@link IAntimatterHandler}. A tank can only ever hold
 * one antimatter type at a time, exactly like the tanks of other technical mods.
 */
public class AntimatterTank implements IAntimatterHandler {

    private int capacity;

    @Nullable
    private Antimatter type;
    private int amount;

    /** Called whenever the content of the tank changed. */
    private Runnable onChanged = () -> {
    };

    public AntimatterTank(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Changes the capacity of the tank. This is needed when tanks are combined into a bigger
     * structure, the amount that does not fit any more is removed.
     */
    public void setCapacity(int newCapacity) {
        this.capacity = Math.max(0, newCapacity);
        if (this.amount > this.capacity) {
            this.amount = this.capacity;
        }
        if (this.amount <= 0) {
            this.type = null;
        }
    }

    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    @Nullable
    public Antimatter getAntimatterInTank(int tank) {
        return tank == 0 ? type : null;
    }

    @Override
    public int getAmount(int tank) {
        return tank == 0 ? amount : 0;
    }

    @Override
    public int getCapacity(int tank) {
        return tank == 0 ? capacity : 0;
    }

    @Override
    public int insertAntimatter(int tank, @Nullable Antimatter insertType, int insertAmount, boolean simulate) {
        if (tank != 0 || insertType == null || insertAmount <= 0) {
            return 0;
        }
        if (type != null && !type.equals(insertType)) {
            return 0;
        }
        int accepted = Math.min(insertAmount, capacity - amount);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            type = insertType;
            amount += accepted;
            onChanged.run();
        }
        return accepted;
    }

    @Override
    public int extractAntimatter(int tank, int extractAmount, boolean simulate) {
        if (tank != 0 || extractAmount <= 0 || amount <= 0) {
            return 0;
        }
        int extracted = Math.min(extractAmount, amount);
        if (!simulate) {
            amount -= extracted;
            if (amount <= 0) {
                amount = 0;
                type = null;
            }
            onChanged.run();
        }
        return extracted;
    }

    /** @return true when the tank currently holds nothing. */
    public boolean isEmpty() {
        return amount <= 0;
    }

    /** Empties the tank completely. */
    public void clear() {
        amount = 0;
        type = null;
    }

    /** Sets the content of the tank without any safety checks. Used for loading and syncing. */
    public void setContent(@Nullable Antimatter newType, int newAmount) {
        this.type = newAmount > 0 ? newType : null;
        this.amount = Math.max(0, Math.min(newAmount, capacity));
        if (this.amount == 0) {
            this.type = null;
        }
        onChanged.run();
    }

    /** Writes the content of the tank into the given tag. */
    public void saveToNBT(CompoundTag tag) {
        tag.putInt("amount", amount);
        if (type != null) {
            tag.putString("type", type.id().toString());
        } else {
            tag.remove("type");
        }
    }

    /** Reads the content of the tank from the given tag. */
    public void loadFromNBT(CompoundTag tag) {
        int newAmount = Math.max(0, Math.min(tag.getInt("amount"), capacity));
        Antimatter newType = null;
        if (tag.contains("type")) {
            newType = ODAntimatters.get(ResourceLocation.parse(tag.getString("type")));
        }
        this.amount = newAmount;
        this.type = newAmount > 0 ? newType : null;
    }
}

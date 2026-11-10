package de.wutzuket.overdrive.antimatter;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A handler that exposes several tanks and routes the antimatter by its type. Every tank can be
 * bound to a fixed antimatter type, then only that type can be stored in it. Tanks without a
 * bound type take every type as long as they are empty.
 *
 * <p>This is the reference implementation for machines that keep one tank per antimatter type.
 * The pipe network does not know the machines, it asks the handler which tank belongs to the
 * transported type, so a single pipe network can supply every tank of the machine.</p>
 */
public class MultiTankAntimatterHandler implements IAntimatterHandler {

    private final List<AntimatterTank> tanks = new ArrayList<>();

    /** The type a tank is bound to, null for tanks that accept every type. */
    private final List<Antimatter> boundTypes = new ArrayList<>();

    /** Adds a tank that accepts every type as long as it is empty. */
    public MultiTankAntimatterHandler addTank(AntimatterTank tank) {
        return addTank(tank, null);
    }

    /** Adds a tank that only accepts the given type. */
    public MultiTankAntimatterHandler addTank(Antimatter type, int capacity) {
        return addTank(new AntimatterTank(capacity), type);
    }

    /** Adds a tank that only accepts the given type, null lets the tank accept every type. */
    public MultiTankAntimatterHandler addTank(AntimatterTank tank, @Nullable Antimatter type) {
        tanks.add(tank);
        boundTypes.add(type);
        return this;
    }

    /** @return the tank at the given index. */
    public AntimatterTank getTank(int index) {
        return tanks.get(index);
    }

    /** @return the type the given tank is bound to or null when it accepts every type. */
    @Nullable
    public Antimatter getBoundType(int index) {
        return boundTypes.get(index);
    }

    @Override
    public int getTanks() {
        return tanks.size();
    }

    @Override
    @Nullable
    public Antimatter getAntimatterInTank(int tank) {
        if (tank < 0 || tank >= tanks.size()) {
            return null;
        }
        return tanks.get(tank).getAntimatterInTank(0);
    }

    @Override
    public int getAmount(int tank) {
        if (tank < 0 || tank >= tanks.size()) {
            return 0;
        }
        return tanks.get(tank).getAmount(0);
    }

    @Override
    public int getCapacity(int tank) {
        if (tank < 0 || tank >= tanks.size()) {
            return 0;
        }
        return tanks.get(tank).getCapacity(0);
    }

    @Override
    public int insertAntimatter(int tank, @Nullable Antimatter type, int amount, boolean simulate) {
        if (tank < 0 || tank >= tanks.size()) {
            return 0;
        }
        if (!isCompatible(tank, type)) {
            return 0;
        }
        return tanks.get(tank).insertAntimatter(0, type, amount, simulate);
    }

    @Override
    public int extractAntimatter(int tank, int amount, boolean simulate) {
        if (tank < 0 || tank >= tanks.size()) {
            return 0;
        }
        return tanks.get(tank).extractAntimatter(0, amount, simulate);
    }

    @Override
    public boolean isCompatible(int tank, @Nullable Antimatter type) {
        if (tank < 0 || tank >= tanks.size()) {
            return false;
        }
        Antimatter bound = boundTypes.get(tank);
        if (bound != null && type != null && !bound.equals(type)) {
            return false;
        }
        return tanks.get(tank).isCompatible(0, type);
    }

    /**
     * A tank that is bound to the wanted type is preferred, so a machine with one tank per type
     * always fills the tank that belongs to the type.
     */
    @Override
    public int getInsertTank(@Nullable Antimatter type) {
        if (type != null) {
            for (int tank = 0; tank < tanks.size(); tank++) {
                Antimatter bound = boundTypes.get(tank);
                if (bound != null && bound.equals(type) && isCompatible(tank, type)) {
                    return tank;
                }
            }
        }
        for (int tank = 0; tank < tanks.size(); tank++) {
            if (boundTypes.get(tank) == null && isCompatible(tank, type)) {
                return tank;
            }
        }
        return -1;
    }

    @Override
    public int getExtractTank(@Nullable Antimatter type) {
        for (int tank = 0; tank < tanks.size(); tank++) {
            Antimatter bound = boundTypes.get(tank);
            if (bound != null && type != null && !bound.equals(type)) {
                continue;
            }
            Antimatter stored = getAntimatterInTank(tank);
            if (stored != null && getAmount(tank) > 0 && (type == null || stored.equals(type))) {
                return tank;
            }
        }
        return -1;
    }
}

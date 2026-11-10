package de.wutzuket.overdrive.antimatter;

import org.jetbrains.annotations.Nullable;

/**
 * Capability interface for everything that is able to store antimatter. It is the antimatter
 * counterpart of NeoForge's {@code IFluidHandler} but completely independent from the vanilla
 * fluid system.
 */
public interface IAntimatterHandler {

    /** @return the number of tanks this handler exposes. */
    int getTanks();

    /**
     * @param tank the tank index
     * @return the antimatter type currently stored in the tank or {@code null} when the tank is empty
     */
    @Nullable
    Antimatter getAntimatterInTank(int tank);

    /**
     * @param tank the tank index
     * @return the amount of antimatter units stored in the tank
     */
    int getAmount(int tank);

    /**
     * @param tank the tank index
     * @return the maximum amount of antimatter units the tank can hold
     */
    int getCapacity(int tank);

    /**
     * Inserts antimatter into the given tank.
     *
     * @param tank     the tank index
     * @param type     the antimatter type that should be inserted
     * @param amount   the maximum amount of units to insert
     * @param simulate when true nothing is actually inserted
     * @return the amount of units that were (or would be) inserted
     */
    int insertAntimatter(int tank, @Nullable Antimatter type, int amount, boolean simulate);

    /**
     * Extracts antimatter from the given tank. The type of the extracted antimatter is whatever
     * is stored in the tank.
     *
     * @param tank     the tank index
     * @param amount   the maximum amount of units to extract
     * @param simulate when true nothing is actually extracted
     * @return the amount of units that were (or would be) extracted
     */
    int extractAntimatter(int tank, int amount, boolean simulate);

    /** @return true when this handler accepts antimatter at all. */
    default boolean canInsert() {
        return true;
    }

    /** @return true when this handler allows antimatter to be taken out. */
    default boolean canExtract() {
        return true;
    }

    /**
     * @param tank the tank index
     * @param type the type to check
     * @return true when the tank is empty or already holds the given type
     */
    default boolean isCompatible(int tank, @Nullable Antimatter type) {
        if (type == null) {
            return true;
        }
        Antimatter stored = getAntimatterInTank(tank);
        return stored == null || stored.equals(type);
    }

    /**
     * Finds the tank the given antimatter type should be inserted into.
     *
     * <p>The default implementation returns the first tank that is compatible with the type.
     * That keeps single tank machines (which only have tank 0) working exactly like before.
     * Machines that keep a different type in every tank should override this method and
     * return the tank that matches the type.</p>
     *
     * @param type the antimatter that should be inserted, may be null
     * @return the index of the tank, or -1 when the type cannot be stored at all
     */
    default int getInsertTank(@Nullable Antimatter type) {
        for (int tank = 0; tank < getTanks(); tank++) {
            if (isCompatible(tank, type)) {
                return tank;
            }
        }
        return -1;
    }

    /**
     * Finds the tank the antimatter should be taken from.
     *
     * <p>The default implementation returns the first tank that actually holds antimatter and
     * that matches the given type (a null type matches every type).</p>
     *
     * @param type the antimatter that is wanted, may be null to accept any type
     * @return the index of the tank, or -1 when no tank holds the wanted antimatter
     */
    default int getExtractTank(@Nullable Antimatter type) {
        for (int tank = 0; tank < getTanks(); tank++) {
            Antimatter stored = getAntimatterInTank(tank);
            if (stored != null && getAmount(tank) > 0 && (type == null || stored.equals(type))) {
                return tank;
            }
        }
        return -1;
    }
}

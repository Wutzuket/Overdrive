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
}

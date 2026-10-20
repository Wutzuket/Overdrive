package de.wutzuket.overdrive.antimatter;

/**
 * Marker interface for blocks that pipes are allowed to connect to. Every block that exposes the
 * antimatter capability and should be reachable by pipes has to implement this interface.
 */
public interface IAntimatterPort {

    /** @return true when antimatter pipes may connect to this block. */
    default boolean canConnectAntimatter() {
        return true;
    }
}

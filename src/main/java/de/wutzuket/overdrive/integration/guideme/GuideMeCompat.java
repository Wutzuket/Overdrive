package de.wutzuket.overdrive.integration.guideme;

import net.neoforged.fml.ModList;

/**
 * Optional integration with GuideME.
 *
 * <p>GuideME (https://modrinth.com/mod/guideme) renders the in-game handbook of
 * Overdrive. It is <b>not</b> a hard dependency: without GuideME the mod works
 * exactly as before, the handbook is simply not available.
 *
 * <p>Only {@link #isLoaded()} may be called unconditionally. Every call that
 * touches the GuideME API itself lives in {@link GuideMeGuide}, which is only
 * loaded when GuideME is actually present.
 */
public final class GuideMeCompat {

    public static final String GUIDE_MOD_ID = "guideme";

    private GuideMeCompat() {
    }

    /**
     * @return {@code true} if GuideME is installed and the handbook can be shown
     */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(GUIDE_MOD_ID);
    }
}

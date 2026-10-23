package de.wutzuket.overdrive.antimatter;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Holder for the custom antimatter capability. It is registered as a sided block capability so
 * machines can decide per face whether they want to interact with antimatter.
 */
public final class ODAntimatterCapability {

    /** Capability that exposes {@link IAntimatterHandler} implementations. */
    public static final BlockCapability<IAntimatterHandler, @Nullable Direction> ANTIMATTER_HANDLER =
            BlockCapability.createSided(Overdrive.id("antimatter_handler"), IAntimatterHandler.class);

    /** Capability exposed by antimatter cell items. */
    public static final ItemCapability<IAntimatterHandler, Void> ANTIMATTER_ITEM_HANDLER =
            ItemCapability.createVoid(Overdrive.id("antimatter_handler"), IAntimatterHandler.class);

    private ODAntimatterCapability() {
    }
}

package de.wutzuket.overdrive.antimatter;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for the custom antimatter types. This is a completely separate registry that has
 * no relation to Minecraft's fluid registries.
 *
 * <p>To add a new type of antimatter only one line has to be added to {@link #TYPES}. The cell
 * item, its texture, the creative tab entry and the JEI ingredient are created automatically
 * from it.</p>
 */
public final class ODAntimatters {

    /** Key of the custom {@code overdrive:antimatter} registry. */
    public static final ResourceKey<Registry<Antimatter>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Overdrive.id("antimatter"));

    /** The custom registry instance. Synced to the client so cells can be rendered properly. */
    public static final Registry<Antimatter> REGISTRY =
            new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();

    /**
     * All antimatter types of the mod. Adding a new type here is enough: the cell item, its
     * texture, the creative tab entry and the JEI ingredient are generated automatically.
     */
    private static final List<Antimatter> TYPES = List.of(
            new Antimatter(Overdrive.id("antiprotons"), "antimatter.overdrive.antiprotons", 0xFFD500FF),
            new Antimatter(Overdrive.id("decelerated_antiprotons"),
                    "antimatter.overdrive.decelerated_antiprotons", 0xFFFF5A00),
            new Antimatter(Overdrive.id("cooled_antiprotons"),
                    "antimatter.overdrive.cooled_antiprotons", 0xFF2E7BFF),
            new Antimatter(Overdrive.id("positrons"), "antimatter.overdrive.positrons", 0xFFFF00D4));

    /** The types by their registry name, for the codecs. */
    private static final Map<ResourceLocation, Antimatter> BY_ID = new LinkedHashMap<>();

    static {
        for (Antimatter type : TYPES) {
            BY_ID.put(type.id(), type);
        }
    }

    public static final Antimatter ANTIPROTONS = TYPES.get(0);
    public static final Antimatter DECELERATED_ANTIPROTONS = TYPES.get(1);
    public static final Antimatter COOLED_ANTIPROTONS = TYPES.get(2);
    public static final Antimatter POSITRONS = TYPES.get(3);

    private ODAntimatters() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NewRegistryEvent.class, event -> event.register(REGISTRY));
    }

    /** @return all antimatter types of the mod. */
    public static List<Antimatter> getTypes() {
        return TYPES;
    }

    /** @return the antimatter with the given id or {@code null} when it is unknown. */
    public static Antimatter get(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Antimatter direct = BY_ID.get(id);
        if (direct != null) {
            return direct;
        }
        // Fallback auf das Registry, falls der Typ dort zuerst registriert wurde.
        for (Antimatter type : REGISTRY) {
            if (id.equals(type.id())) {
                return type;
            }
        }
        return null;
    }

    /** @return the id of the given antimatter or {@code null} when it is unknown. */
    public static ResourceLocation getKey(Antimatter antimatter) {
        return antimatter == null ? null : antimatter.id();
    }
}

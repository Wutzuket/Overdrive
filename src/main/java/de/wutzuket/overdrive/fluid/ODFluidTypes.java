package de.wutzuket.overdrive.fluid;

import de.wutzuket.overdrive.Overdrive;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ODFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Overdrive.MODID);

    public static final Supplier<FluidType> ANTIPROTONS_TYPE = FLUID_TYPES.register("antiprotons",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.antiprotons")
                            .canSwim(false)
                            .canDrown(false)
                            .viscosity(100)
                            .density(1000)
            ));

    public static final Supplier<FluidType> DECELERATED_ANTIPROTONS_TYPE = FLUID_TYPES.register("decelerated_antiprotons",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.decelerated_antiprotons")
                            .canSwim(false)
                            .canDrown(false)
                            .viscosity(120)
                            .density(1100)
            ));

    public static final Supplier<FluidType> COOLED_ANTIPROTONS_TYPE = FLUID_TYPES.register("cooled_antiprotons",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.cooled_antiprotons")
                            .canSwim(false)
                            .canDrown(false)
                            .viscosity(150)
                            .density(1200)
            ));

    public static final Supplier<FluidType> POSITRONS_TYPE = FLUID_TYPES.register("positrons",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.positrons")
                            .canSwim(false)
                            .canDrown(false)
                            .viscosity(100)
                            .density(1000)
            ));
    public static final Supplier<FluidType> SODIUM_TYPE = FLUID_TYPES.register("sodium",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.sodium")
                            .canSwim(false).canDrown(false).viscosity(200).density(1500)
            ));
    public static final Supplier<FluidType> CRYOFLUID_TYPE = FLUID_TYPES.register("cryofluid",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.cryofluid")
                            .canSwim(false).canDrown(false).viscosity(80).density(900)
            ));
    public static final Supplier<FluidType> STEAM_TYPE = FLUID_TYPES.register("steam",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.steam")
                            .canSwim(false).canDrown(false).viscosity(100).density(500).temperature(500).lightLevel(5)
            ));
    public static final Supplier<FluidType> SUPERHEATED_SODIUM_TYPE = FLUID_TYPES.register("superheated_sodium",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.superheated_sodium")
                            .canSwim(false).canDrown(false).viscosity(150).density(1400).temperature(800).lightLevel(10)
            ));
    public static final Supplier<FluidType> LIQUID_IRIDIUM_TYPE = FLUID_TYPES.register("liquid_iridium",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.liquid_iridium")
                            .canSwim(false).canDrown(false).viscosity(600).density(8000).temperature(1200).lightLevel(7)
            ));
    public static final Supplier<FluidType> SUPERHEATED_CRYOFLUID_TYPE = FLUID_TYPES.register("superheated_cryofluid",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.superheated_cryofluid")
                            .canSwim(false).canDrown(false).viscosity(60).density(800).temperature(600).lightLevel(8)
            ));

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}

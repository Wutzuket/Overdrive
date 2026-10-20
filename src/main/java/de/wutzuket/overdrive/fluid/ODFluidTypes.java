package de.wutzuket.overdrive.fluid;

import de.wutzuket.overdrive.Overdrive;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.FLUID_TYPES, Overdrive.MODID);

    // Die Antiprotonen und Positronen sind keine Fluide mehr, sie werden ueber das
    // Antimatter-System bewegt (Antimatter-Rohrnetz und Antimatter-Zellen).

    public static final Supplier<FluidType> CRYOFLUID_TYPE = FLUID_TYPES.register("cryofluid",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.cryofluid")
                            .canSwim(false)
                            .canDrown(false)
                            .viscosity(80)
                            .density(900)
            ));

    public static final Supplier<FluidType> STEAM_TYPE = FLUID_TYPES.register("steam",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.steam")
                            .canSwim(false)
                            .canDrown(false)
                            .viscosity(100)
                            .density(500)
                            .temperature(500)
                            .lightLevel(5)
            ));

    public static final Supplier<FluidType> LIQUID_IRIDIUM_TYPE = FLUID_TYPES.register("liquid_iridium",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.liquid_iridium")
                            .canSwim(false)
                            .canDrown(false)
                            .viscosity(600)
                            .density(8000)
                            .temperature(1200)
                            .lightLevel(7)
            ));

    public static final Supplier<FluidType> SUPERHEATED_CRYOFLUID_TYPE = FLUID_TYPES.register("superheated_cryofluid",
            () -> new FluidType(
                    FluidType.Properties.create()
                            .descriptionId("fluid.overdrive.superheated_cryofluid")
                            .canSwim(false)
                            .canDrown(false)
                            .viscosity(60)
                            .density(800)
                            .temperature(600)
                            .lightLevel(8)
            ));

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}

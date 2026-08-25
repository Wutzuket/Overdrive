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

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}

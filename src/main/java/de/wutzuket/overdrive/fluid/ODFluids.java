package de.wutzuket.overdrive.fluid;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.FLUID, Overdrive.MODID);

    public static final Supplier<FlowingFluid> ANTIPROTONS_SOURCE = FLUIDS.register("antiprotons",
            () -> new BaseFlowingFluid.Source(ODFluids.ANTIPROTONS_PROPERTIES));

    public static final Supplier<FlowingFluid> ANTIPROTONS_FLOWING = FLUIDS.register("flowing_antiprotons",
            () -> new BaseFlowingFluid.Flowing(ODFluids.ANTIPROTONS_PROPERTIES));

    private static final BaseFlowingFluid.Properties ANTIPROTONS_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    () -> ODFluidTypes.ANTIPROTONS_TYPE.get(),
                    ANTIPROTONS_SOURCE,
                    ANTIPROTONS_FLOWING
            )
                    .block(() -> (net.minecraft.world.level.block.LiquidBlock) ODFluidBlocks.ANTIPROTONS.get())
                    .bucket(() -> ODItemsFluid.ANTIPROTONS_BUCKET.get());

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
    }
}

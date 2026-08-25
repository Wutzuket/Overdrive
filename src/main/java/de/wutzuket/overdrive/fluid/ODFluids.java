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

    public static final Supplier<FlowingFluid> DECELERATED_ANTIPROTONS_SOURCE = FLUIDS.register("decelerated_antiprotons",
            () -> new BaseFlowingFluid.Source(ODFluids.DECELERATED_ANTIPROTONS_PROPERTIES));

    public static final Supplier<FlowingFluid> DECELERATED_ANTIPROTONS_FLOWING = FLUIDS.register("flowing_decelerated_antiprotons",
            () -> new BaseFlowingFluid.Flowing(ODFluids.DECELERATED_ANTIPROTONS_PROPERTIES));

    private static final BaseFlowingFluid.Properties DECELERATED_ANTIPROTONS_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    () -> ODFluidTypes.DECELERATED_ANTIPROTONS_TYPE.get(),
                    DECELERATED_ANTIPROTONS_SOURCE,
                    DECELERATED_ANTIPROTONS_FLOWING
            )
                    .block(() -> (net.minecraft.world.level.block.LiquidBlock) ODFluidBlocks.DECELERATED_ANTIPROTONS.get())
                    .bucket(() -> ODItemsFluid.DECELERATED_ANTIPROTONS_BUCKET.get());

    public static final Supplier<FlowingFluid> COOLED_ANTIPROTONS_SOURCE = FLUIDS.register("cooled_antiprotons",
            () -> new BaseFlowingFluid.Source(ODFluids.COOLED_ANTIPROTONS_PROPERTIES));

    public static final Supplier<FlowingFluid> COOLED_ANTIPROTONS_FLOWING = FLUIDS.register("flowing_cooled_antiprotons",
            () -> new BaseFlowingFluid.Flowing(ODFluids.COOLED_ANTIPROTONS_PROPERTIES));

    private static final BaseFlowingFluid.Properties COOLED_ANTIPROTONS_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    () -> ODFluidTypes.COOLED_ANTIPROTONS_TYPE.get(),
                    COOLED_ANTIPROTONS_SOURCE,
                    COOLED_ANTIPROTONS_FLOWING
            )
                    .block(() -> (net.minecraft.world.level.block.LiquidBlock) ODFluidBlocks.COOLED_ANTIPROTONS.get())
                    .bucket(() -> ODItemsFluid.COOLED_ANTIPROTONS_BUCKET.get());

    public static final Supplier<FlowingFluid> POSITRONS_SOURCE = FLUIDS.register("positrons",
            () -> new BaseFlowingFluid.Source(ODFluids.POSITRONS_PROPERTIES));

    public static final Supplier<FlowingFluid> POSITRONS_FLOWING = FLUIDS.register("flowing_positrons",
            () -> new BaseFlowingFluid.Flowing(ODFluids.POSITRONS_PROPERTIES));

    private static final BaseFlowingFluid.Properties POSITRONS_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    () -> ODFluidTypes.POSITRONS_TYPE.get(),
                    POSITRONS_SOURCE,
                    POSITRONS_FLOWING
            )
                    .block(() -> (net.minecraft.world.level.block.LiquidBlock) ODFluidBlocks.POSITRONS.get())
                    .bucket(() -> ODItemsFluid.POSITRONS_BUCKET.get());

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
    }
}

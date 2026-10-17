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

    // Antimaterie-Fluide haben wieder LiquidBlocks und koennen normal
    // in der Welt platziert und durch Pipes transportiert werden.
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

    public static final Supplier<FlowingFluid> CRYOFLUID_SOURCE = FLUIDS.register("cryofluid",
            () -> new BaseFlowingFluid.Source(ODFluids.CRYOFLUID_PROPERTIES));
    public static final Supplier<FlowingFluid> CRYOFLUID_FLOWING = FLUIDS.register("flowing_cryofluid",
            () -> new BaseFlowingFluid.Flowing(ODFluids.CRYOFLUID_PROPERTIES));
    private static final BaseFlowingFluid.Properties CRYOFLUID_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    () -> ODFluidTypes.CRYOFLUID_TYPE.get(),
                    CRYOFLUID_SOURCE,
                    CRYOFLUID_FLOWING
            )
                    .block(() -> (net.minecraft.world.level.block.LiquidBlock) ODFluidBlocks.CRYOFLUID.get())
                    .bucket(() -> ODItemsFluid.CRYOFLUID_BUCKET.get());

    public static final Supplier<FlowingFluid> STEAM_SOURCE = FLUIDS.register("steam",
            () -> new BaseFlowingFluid.Source(ODFluids.STEAM_PROPERTIES));
    public static final Supplier<FlowingFluid> STEAM_FLOWING = FLUIDS.register("flowing_steam",
            () -> new BaseFlowingFluid.Flowing(ODFluids.STEAM_PROPERTIES));
    private static final BaseFlowingFluid.Properties STEAM_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    () -> ODFluidTypes.STEAM_TYPE.get(),
                    STEAM_SOURCE,
                    STEAM_FLOWING
            )
                    .block(() -> (net.minecraft.world.level.block.LiquidBlock) ODFluidBlocks.STEAM.get())
                    .bucket(() -> ODItemsFluid.STEAM_BUCKET.get());

    public static final Supplier<FlowingFluid> LIQUID_IRIDIUM_SOURCE = FLUIDS.register("liquid_iridium",
            () -> new BaseFlowingFluid.Source(ODFluids.LIQUID_IRIDIUM_PROPERTIES));
    public static final Supplier<FlowingFluid> LIQUID_IRIDIUM_FLOWING = FLUIDS.register("flowing_liquid_iridium",
            () -> new BaseFlowingFluid.Flowing(ODFluids.LIQUID_IRIDIUM_PROPERTIES));
    private static final BaseFlowingFluid.Properties LIQUID_IRIDIUM_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    () -> ODFluidTypes.LIQUID_IRIDIUM_TYPE.get(),
                    LIQUID_IRIDIUM_SOURCE,
                    LIQUID_IRIDIUM_FLOWING
            )
                    .block(() -> (net.minecraft.world.level.block.LiquidBlock) ODFluidBlocks.LIQUID_IRIDIUM.get())
                    .bucket(() -> ODItemsFluid.LIQUID_IRIDIUM_BUCKET.get());

    public static final Supplier<FlowingFluid> SUPERHEATED_CRYOFLUID_SOURCE = FLUIDS.register("superheated_cryofluid",
            () -> new BaseFlowingFluid.Source(ODFluids.SUPERHEATED_CRYOFLUID_PROPERTIES));
    public static final Supplier<FlowingFluid> SUPERHEATED_CRYOFLUID_FLOWING = FLUIDS.register("flowing_superheated_cryofluid",
            () -> new BaseFlowingFluid.Flowing(ODFluids.SUPERHEATED_CRYOFLUID_PROPERTIES));
    private static final BaseFlowingFluid.Properties SUPERHEATED_CRYOFLUID_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    () -> ODFluidTypes.SUPERHEATED_CRYOFLUID_TYPE.get(),
                    SUPERHEATED_CRYOFLUID_SOURCE,
                    SUPERHEATED_CRYOFLUID_FLOWING
            )
                    .block(() -> (net.minecraft.world.level.block.LiquidBlock) ODFluidBlocks.SUPERHEATED_CRYOFLUID.get())
                    .bucket(() -> ODItemsFluid.SUPERHEATED_CRYOFLUID_BUCKET.get());

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
    }
}

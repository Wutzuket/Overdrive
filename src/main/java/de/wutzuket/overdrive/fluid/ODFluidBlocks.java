package de.wutzuket.overdrive.fluid;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODFluidBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Overdrive.MODID);

    public static final Supplier<Block> ANTIPROTONS = BLOCKS.register("antiprotons",
            () -> new LiquidBlock(ODFluids.ANTIPROTONS_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final Supplier<Block> DECELERATED_ANTIPROTONS = BLOCKS.register("decelerated_antiprotons",
            () -> new LiquidBlock(ODFluids.DECELERATED_ANTIPROTONS_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final Supplier<Block> COOLED_ANTIPROTONS = BLOCKS.register("cooled_antiprotons",
            () -> new LiquidBlock(ODFluids.COOLED_ANTIPROTONS_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final Supplier<Block> POSITRONS = BLOCKS.register("positrons",
            () -> new LiquidBlock(ODFluids.POSITRONS_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final Supplier<Block> CRYOFLUID = BLOCKS.register("cryofluid",
            () -> new LiquidBlock(ODFluids.CRYOFLUID_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final Supplier<Block> STEAM = BLOCKS.register("steam",
            () -> new LiquidBlock(ODFluids.STEAM_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable().lightLevel(s -> 5)));
    public static final Supplier<Block> LIQUID_IRIDIUM = BLOCKS.register("liquid_iridium",
            () -> new LiquidBlock(ODFluids.LIQUID_IRIDIUM_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable().lightLevel(s -> 7)));
    public static final Supplier<Block> SUPERHEATED_CRYOFLUID = BLOCKS.register("superheated_cryofluid",
            () -> new LiquidBlock(ODFluids.SUPERHEATED_CRYOFLUID_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable().lightLevel(s -> 8)));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}

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

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}

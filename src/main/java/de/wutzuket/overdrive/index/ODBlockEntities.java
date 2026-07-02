package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.energy.CreativeEnergyCubeBlockEntity;
import de.wutzuket.overdrive.block.scanner.scanner_blockentity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ODBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Overdrive.MODID);

    public static final java.util.function.Supplier<BlockEntityType<scanner_blockentity>> SCANNER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "scanner",
                    () -> BlockEntityType.Builder.of(scanner_blockentity::new, ODBlocks.SCANNER_BLOCK.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<CreativeEnergyCubeBlockEntity>> CREATIVE_ENERGY_CUBE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "creative_energy_cube",
                    () -> BlockEntityType.Builder.of(CreativeEnergyCubeBlockEntity::new, ODBlocks.CREATIVE_ENERGY_CUBE.get()).build(null)
            );

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}

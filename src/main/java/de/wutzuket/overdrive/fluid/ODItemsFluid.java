package de.wutzuket.overdrive.fluid;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODItemsFluid {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Overdrive.MODID);

    // The antiprotons and positrons are not in buckets, they are transported with the antimatter
    // pipe network and the antimatter cells instead.

    public static final Supplier<Item> CRYOFLUID_BUCKET = ITEMS.register("cryofluid_bucket",
            () -> new BucketItem(ODFluids.CRYOFLUID_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> STEAM_BUCKET = ITEMS.register("steam_bucket",
            () -> new BucketItem(ODFluids.STEAM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> LIQUID_IRIDIUM_BUCKET = ITEMS.register("liquid_iridium_bucket",
            () -> new BucketItem(ODFluids.LIQUID_IRIDIUM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> SUPERHEATED_CRYOFLUID_BUCKET = ITEMS.register("superheated_cryofluid_bucket",
            () -> new BucketItem(ODFluids.SUPERHEATED_CRYOFLUID_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

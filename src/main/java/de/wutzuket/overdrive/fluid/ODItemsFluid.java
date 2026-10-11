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

    public static final Supplier<Item> ANTIPROTONS_BUCKET = ITEMS.register("antiprotons_bucket",
            () -> new BucketItem(ODFluids.ANTIPROTONS_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final Supplier<Item> DECELERATED_ANTIPROTONS_BUCKET = ITEMS.register("decelerated_antiprotons_bucket",
            () -> new BucketItem(ODFluids.DECELERATED_ANTIPROTONS_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final Supplier<Item> COOLED_ANTIPROTONS_BUCKET = ITEMS.register("cooled_antiprotons_bucket",
            () -> new BucketItem(ODFluids.COOLED_ANTIPROTONS_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final Supplier<Item> POSITRONS_BUCKET = ITEMS.register("positrons_bucket",
            () -> new BucketItem(ODFluids.POSITRONS_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> SODIUM_BUCKET = ITEMS.register("sodium_bucket",
            () -> new BucketItem(ODFluids.SODIUM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> CRYOFLUID_BUCKET = ITEMS.register("cryofluid_bucket",
            () -> new BucketItem(ODFluids.CRYOFLUID_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> STEAM_BUCKET = ITEMS.register("steam_bucket",
            () -> new BucketItem(ODFluids.STEAM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> SUPERHEATED_SODIUM_BUCKET = ITEMS.register("superheated_sodium_bucket",
            () -> new BucketItem(ODFluids.SUPERHEATED_SODIUM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> LIQUID_IRIDIUM_BUCKET = ITEMS.register("liquid_iridium_bucket",
            () -> new BucketItem(ODFluids.LIQUID_IRIDIUM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final Supplier<Item> SUPERHEATED_CRYOFLUID_BUCKET = ITEMS.register("superheated_cryofluid_bucket",
            () -> new BucketItem(ODFluids.SUPERHEATED_CRYOFLUID_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

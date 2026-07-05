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

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

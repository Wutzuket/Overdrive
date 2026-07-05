package de.wutzuket.overdrive.recipe;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Overdrive.MODID);

    public static final Supplier<RecipeSerializer<CollisionRecipe>> COLLISION_SERIALIZER =
            RECIPE_SERIALIZERS.register("collision", CollisionRecipeSerializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}

package de.wutzuket.overdrive.recipe;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Overdrive.MODID);

    public static final Supplier<RecipeType<CollisionRecipe>> COLLISION =
            RECIPE_TYPES.register("accelerator_collision", () -> RecipeType.simple(Overdrive.id("accelerator_collision")));

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
    }
}

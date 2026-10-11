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

    public static final Supplier<RecipeSerializer<AntiprotonCoolingRecipe>> AD_SERIALIZER =
            RECIPE_SERIALIZERS.register("ad_recipe", () -> new AntiprotonCoolingRecipeSerializer(ODRecipeTypes.AD_RECIPE));

    public static final Supplier<RecipeSerializer<AntiprotonCoolingRecipe>> ELENA_SERIALIZER =
            RECIPE_SERIALIZERS.register("elena_recipe", () -> new AntiprotonCoolingRecipeSerializer(ODRecipeTypes.ELENA_RECIPE));

    public static final Supplier<RecipeSerializer<PositronExtractionRecipe>> POSITRON_EXTRACTION_SERIALIZER =
            RECIPE_SERIALIZERS.register("positron_extraction", PositronExtractionRecipeSerializer::new);

    public static final Supplier<RecipeSerializer<AnnihilationRecipe>> ANNIHILATION_SERIALIZER =
            RECIPE_SERIALIZERS.register("annihilation_generator", AnnihilationRecipeSerializer::new);
    public static final Supplier<RecipeSerializer<CoolingTowerRecipe>> COOLING_TOWER_SERIALIZER =
            RECIPE_SERIALIZERS.register("cooling_tower", CoolingTowerRecipeSerializer::new);
    public static final Supplier<RecipeSerializer<ReactorCoolingRecipe>> REACTOR_COOLING_SERIALIZER =
            RECIPE_SERIALIZERS.register("reactor_cooling", ReactorCoolingRecipeSerializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}

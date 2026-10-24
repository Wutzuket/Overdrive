package de.wutzuket.overdrive.recipe;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe_serializer;
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
    public static final Supplier<RecipeSerializer<CryoDistillerRecipe>> CRYO_DISTILLER_SERIALIZER =
            RECIPE_SERIALIZERS.register("catalytic_reactor", CryoDistillerRecipeSerializer::new);
    public static final Supplier<RecipeSerializer<CircuitPressRecipe>> CIRCUIT_PRESS_SERIALIZER =
            RECIPE_SERIALIZERS.register("circuit_press", CircuitPressRecipeSerializer::new);
    public static final Supplier<RecipeSerializer<scanner_recipe>> SCANNER_SERIALIZER =
            RECIPE_SERIALIZERS.register("scanner", scanner_recipe_serializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}

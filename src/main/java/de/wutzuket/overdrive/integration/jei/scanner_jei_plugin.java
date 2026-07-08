package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe_manager;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.CollisionRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JeiPlugin
public class scanner_jei_plugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("overdrive", "scanner_jei");
    private static IJeiRuntime jeiRuntime;

    public static IJeiRuntime getRuntime() {
        return jeiRuntime;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new scanner_recipe_category(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new AcceleratorRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(scanner_recipe_category.TYPE, scanner_recipe_manager.INSTANCE.getRecipes().stream().toList());

        if (Minecraft.getInstance().level != null) {
            RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
            List<CollisionRecipe> collisionRecipes = new ArrayList<>();
            for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
                if (holder.value().getType() == ODRecipeTypes.COLLISION.get() && holder.value() instanceof CollisionRecipe recipe) {
                    collisionRecipes.add(recipe);
                }
            }
            registration.addRecipes(AcceleratorRecipeCategory.TYPE, collisionRecipes);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ODItems.SCANNER_ITEM.get()), scanner_recipe_category.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.PARTICLE_ACCELERATOR_CONTROLLER.get()), AcceleratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ACCELERATOR_CASING.get()), AcceleratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ACCELERATOR_ENERGY_INPUT_PORT.get()), AcceleratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ACCELERATOR_VACUUM_INPUT_PORT.get()), AcceleratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.IRIDIUM_TARGET.get()), AcceleratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ACCELERATOR_OUTPUT.get()), AcceleratorRecipeCategory.TYPE);
    }
}

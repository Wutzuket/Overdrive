package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe_manager;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.AntiprotonCoolingRecipe;
import de.wutzuket.overdrive.recipe.CollisionRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import de.wutzuket.overdrive.recipe.PositronExtractionRecipe;
import de.wutzuket.overdrive.integration.jei.PositronExtractionRecipeCategory;
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
        registration.addRecipeCategories(new ADRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ELENARecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new PositronExtractionRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
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

        List<AntiprotonCoolingRecipe> adRecipes = new ArrayList<>();
        List<AntiprotonCoolingRecipe> elenaRecipes = new ArrayList<>();
        List<PositronExtractionRecipe> positronRecipes = new ArrayList<>();
        if (Minecraft.getInstance().level != null) {
            RecipeManager rmanager = Minecraft.getInstance().level.getRecipeManager();
            for (RecipeHolder<?> holder : rmanager.getRecipes()) {
                if (holder.value() instanceof AntiprotonCoolingRecipe recipe) {
                    if (recipe.getType() == ODRecipeTypes.AD_RECIPE.get()) {
                        adRecipes.add(recipe);
                    } else if (recipe.getType() == ODRecipeTypes.ELENA_RECIPE.get()) {
                        elenaRecipes.add(recipe);
                    }
                }
                if (holder.value().getType() == ODRecipeTypes.POSITRON_EXTRACTION.get() && holder.value() instanceof PositronExtractionRecipe recipe) {
                    positronRecipes.add(recipe);
                }
            }
        }
        registration.addRecipes(ADRecipeCategory.TYPE, adRecipes);
        registration.addRecipes(ELENARecipeCategory.TYPE, elenaRecipes);
        registration.addRecipes(PositronExtractionRecipeCategory.TYPE, positronRecipes);
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

        registration.addRecipeCatalyst(new ItemStack(ODItems.AD_CONTROLLER.get()), ADRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.AD_CASING.get()), ADRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.AD_ENERGY_INPUT_PORT.get()), ADRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.AD_VACUUM_PORT.get()), ADRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.AD_INPUT_PORT.get()), ADRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.AD_OUTPUT_PORT.get()), ADRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ODItems.ELENA_CONTROLLER.get()), ELENARecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ELENA_CASING.get()), ELENARecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ELENA_ENERGY_INPUT_PORT.get()), ELENARecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ELENA_VACUUM_PORT.get()), ELENARecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ELENA_INPUT_PORT.get()), ELENARecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.ELENA_OUTPUT_PORT.get()), ELENARecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ODItems.ELECTRON_GUN.get()), PositronExtractionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.WOLFRAM_TARGET.get()), PositronExtractionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.WOLFRAM_MESH.get()), PositronExtractionRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ODItems.POSITRON_VACUUM_OUTPUT_PORT.get()), PositronExtractionRecipeCategory.TYPE);
    }
}

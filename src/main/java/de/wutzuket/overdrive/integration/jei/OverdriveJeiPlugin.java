package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.antimatter.AntimatterStack;
import de.wutzuket.overdrive.antimatter.ODAntimatters;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class OverdriveJeiPlugin implements IModPlugin {
    public static final String MODID = Overdrive.MODID;

    private static @Nullable IJeiRuntime RUNTIME;

    public static @Nullable IJeiRuntime getRuntime() {
        return RUNTIME;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        RUNTIME = runtime;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new AcceleratorRecipeCategory(guiHelper),
                new ADCoolingRecipeCategory(guiHelper),
                new ELENACoolingRecipeCategory(guiHelper),
                new AnnihilationRecipeCategory(guiHelper),
                new ReactorCoolingRecipeCategory(guiHelper),
                new CoolingTowerCategory(guiHelper),
                new PositronExtractionCategory(guiHelper),
                new CryoDistillerCategory(guiHelper),
                new CircuitPressCategory(guiHelper)
        );
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        // Die Antimatter werden bei JEI wie ein Fluid registriert, damit sie in der
        // Zutatenliste auftauchen und eigene Rezept-Slots bekommen. Die Liste entsteht
        // automatisch aus den Antimatter-Typen.
        List<AntimatterStack> stacks = new ArrayList<>();
        for (de.wutzuket.overdrive.antimatter.Antimatter type : ODAntimatters.getTypes()) {
            stacks.add(new AntimatterStack(type, 1000));
        }
        registration.register(AntimatterIngredientType.INSTANCE, stacks,
                new AntimatterIngredientHelper(),
                AntimatterIngredientRenderer.INSTANCE,
                AntimatterStack.CODEC);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        registration.addRecipes(AcceleratorRecipeCategory.TYPE, collect(level, ODRecipeTypes.COLLISION.get()));
        registration.addRecipes(ADCoolingRecipeCategory.TYPE, collect(level, ODRecipeTypes.AD_RECIPE.get()));
        registration.addRecipes(ELENACoolingRecipeCategory.TYPE, collect(level, ODRecipeTypes.ELENA_RECIPE.get()));
        registration.addRecipes(AnnihilationRecipeCategory.TYPE, collect(level, ODRecipeTypes.ANNIHILATION.get()));
        registration.addRecipes(ReactorCoolingRecipeCategory.TYPE, collect(level, ODRecipeTypes.REACTOR_COOLING.get()));
        registration.addRecipes(CoolingTowerCategory.TYPE, collect(level, ODRecipeTypes.COOLING_TOWER.get()));
        registration.addRecipes(PositronExtractionCategory.TYPE, collect(level, ODRecipeTypes.POSITRON_EXTRACTION.get()));
        registration.addRecipes(CryoDistillerCategory.TYPE, collect(level, ODRecipeTypes.CRYO_DISTILLER.get()));
        registration.addRecipes(CircuitPressCategory.TYPE, collect(level, ODRecipeTypes.CIRCUIT_PRESS.get()));
    }

    /**
     * Rezepte eines Vanilla-Rezepttyps einsammeln. AD und ELENA teilen sich dieselbe
     * Rezeptklasse, deshalb wird hier bewusst ueber den Rezepttyp und nicht ueber die
     * Klasse gefiltert.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> List<T> collect(ClientLevel level,
                                                          net.minecraft.world.item.crafting.RecipeType<?> type) {
        List<T> result = new ArrayList<>();
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (recipe.getType() == type) {
                result.add((T) recipe);
            }
        }
        return result;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        catalyst(registration, ODBlocks.PARTICLE_ACCELERATOR_CONTROLLER.get(), AcceleratorRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.IRIDIUM_TARGET.get(), AcceleratorRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.ACCELERATOR_OUTPUT.get(), AcceleratorRecipeCategory.TYPE);

        catalyst(registration, ODBlocks.AD_CONTROLLER.get(), ADCoolingRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.AD_INPUT_PORT.get(), ADCoolingRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.AD_OUTPUT_PORT.get(), ADCoolingRecipeCategory.TYPE);

        catalyst(registration, ODBlocks.ELENA_CONTROLLER.get(), ELENACoolingRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.ELENA_INPUT_PORT.get(), ELENACoolingRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.ELENA_OUTPUT_PORT.get(), ELENACoolingRecipeCategory.TYPE);

        catalyst(registration, ODBlocks.ANNIHILATION_CONTROLLER.get(), AnnihilationRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.CELL_DOCKING_PORT.get(), AnnihilationRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.ANNIHILATION_CONTROLLER.get(), ReactorCoolingRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.COOLANT_INPUT_PORT.get(), ReactorCoolingRecipeCategory.TYPE);
        catalyst(registration, ODBlocks.STEAM_OUTPUT_PORT.get(), ReactorCoolingRecipeCategory.TYPE);

        catalyst(registration, ODBlocks.COOLING_TOWER_CONTROLLER.get(), CoolingTowerCategory.TYPE);
        catalyst(registration, ODBlocks.COOLING_TOWER_INPUT_PORT.get(), CoolingTowerCategory.TYPE);
        catalyst(registration, ODBlocks.COOLING_TOWER_OUTPUT_PORT.get(), CoolingTowerCategory.TYPE);

        catalyst(registration, ODBlocks.ELECTRON_GUN.get(), PositronExtractionCategory.TYPE);
        catalyst(registration, ODBlocks.TUNGSTEN_TARGET.get(), PositronExtractionCategory.TYPE);
        catalyst(registration, ODBlocks.POSITRON_VACUUM_OUTPUT_PORT.get(), PositronExtractionCategory.TYPE);

        catalyst(registration, ODItems.CRYO_DISTILLER.get(), CryoDistillerCategory.TYPE);
        catalyst(registration, ODItems.CIRCUIT_PRESS.get(), CircuitPressCategory.TYPE);
    }

    private static <T> void catalyst(IRecipeCatalystRegistration registration, ItemLike item, RecipeType<T> type) {
        registration.addRecipeCatalyst(new ItemStack(item.asItem()), type);
    }
}
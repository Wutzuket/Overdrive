package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.CryoDistillerRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * JEI-Kategorie fuer den Cryo Distiller.
 * Zeigt Fluid-Input, Fluid-Output, Dauer, Stromverbrauch und den geforderten
 * Katalysator-Block als 3D-Render unter der Maschine.
 */
@SuppressWarnings("removal")
public class CryoDistillerCategory implements IRecipeCategory<CryoDistillerRecipe> {
    public static final RecipeType<CryoDistillerRecipe> TYPE =
            RecipeType.create(OverdriveJeiPlugin.MODID, "cryo_distiller", CryoDistillerRecipe.class);

    public static final int WIDTH = 200;
    public static final int HEIGHT = 140;

    private final IDrawable background;
    private final IDrawable icon;

    public CryoDistillerCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(ODBlocks.CRYO_DISTILLER.get());
    }

    @Override
    public RecipeType<CryoDistillerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.overdrive.cryo_distiller.title");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CryoDistillerRecipe recipe,
                          @NotNull mezz.jei.api.recipe.IFocusGroup focuses) {
        if (!recipe.inputFluid().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, CryoDistillerWidget.INPUT_SLOT_X, CryoDistillerWidget.INPUT_SLOT_Y)
                    .addFluidStack(recipe.inputFluid().getFluid(), recipe.inputFluid().getAmount(), DataComponentPatch.EMPTY);
        }
        if (!recipe.outputFluid().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, CryoDistillerWidget.OUTPUT_SLOT_X, CryoDistillerWidget.OUTPUT_SLOT_Y)
                    .addFluidStack(recipe.outputFluid().getFluid(), recipe.outputFluid().getAmount(), DataComponentPatch.EMPTY);
        }
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                .addItemStack(new ItemStack(ODBlocks.CRYO_DISTILLER.get().asItem()));
    }

    @Override
    public void createRecipeExtras(@NotNull mezz.jei.api.gui.widgets.IRecipeExtrasBuilder builder,
                                   @NotNull CryoDistillerRecipe recipe,
                                   @NotNull mezz.jei.api.recipe.IFocusGroup focuses) {
        CryoDistillerWidget widget = new CryoDistillerWidget(recipe);
        builder.addWidget(widget);
        builder.addGuiEventListener(widget);
    }

    @Override
    public void draw(@NotNull CryoDistillerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
    }
}
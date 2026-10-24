package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.CircuitPressRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CircuitPressCategory implements IRecipeCategory<CircuitPressRecipe> {
    public static final RecipeType<CircuitPressRecipe> TYPE =
            RecipeType.create(Overdrive.MODID, "circuit_press", CircuitPressRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;

    public CircuitPressCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(180, 90);
        icon = helper.createDrawableItemLike(ODBlocks.CIRCUIT_PRESS.get());
    }
    @Override public RecipeType<CircuitPressRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.overdrive.circuit_press.title"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public IDrawable getBackground() { return background; }
    @Override public int getWidth() { return 180; }
    @Override public int getHeight() { return 90; }

    @Override public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CircuitPressRecipe recipe,
                                     @NotNull mezz.jei.api.recipe.IFocusGroup focuses) {
        int index = 0;
        for (var ingredient : recipe.ingredients()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + index++ * 22, 28).addIngredients(ingredient);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 28).addItemStack(recipe.output());
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                .addItemStack(new ItemStack(ODBlocks.CIRCUIT_PRESS.get()));
    }

    @Override public void draw(@NotNull CircuitPressRecipe recipe, @NotNull IRecipeSlotsView slots,
                               @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                Component.literal(recipe.processTime() + " t | " + recipe.energyPerTick() + " FE/t"), 8, 68, 0xFFAAAAAA);
    }
}

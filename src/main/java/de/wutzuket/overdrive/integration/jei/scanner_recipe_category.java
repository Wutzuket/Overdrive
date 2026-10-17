package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe;
import de.wutzuket.overdrive.index.ODItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class scanner_recipe_category implements IRecipeCategory<scanner_recipe> {
    public static final RecipeType<scanner_recipe> TYPE =
            RecipeType.create(Overdrive.MODID, "scanner", scanner_recipe.class);

    private final IDrawable icon;
    private final IDrawable background;

    public scanner_recipe_category(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ODItems.SCANNER.get()));
        this.background = guiHelper.createBlankDrawable(scanner_recipe_widget.WIDTH, scanner_recipe_widget.HEIGHT);
    }

    @Override
    public RecipeType<scanner_recipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.overdrive.scanner.title");
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
        return scanner_recipe_widget.WIDTH;
    }

    @Override
    public int getHeight() {
        return scanner_recipe_widget.HEIGHT;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull scanner_recipe recipe, @NotNull IFocusGroup focuses) {
        for (ItemStack cost : recipe.getMaterialCosts()) {
            addInputStacks(builder, cost);
        }

        builder.addOutputSlot(scanner_recipe_widget.RESULT_X + 1, scanner_recipe_widget.RESULT_Y + 1).addItemStack(recipe.getResultStack());
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(recipe.getPreviewIngredientStacks());
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull scanner_recipe recipe, @NotNull IFocusGroup focuses) {
        scanner_recipe_widget widget = new scanner_recipe_widget(recipe);
        builder.addWidget(widget);
        builder.addGuiEventListener(widget);
    }

    @Override
    public void draw(@NotNull scanner_recipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics,
                     double mouseX, double mouseY) {
    }

    private static void addInputStacks(IRecipeLayoutBuilder builder, ItemStack cost) {
        int remaining = cost.getCount();
        int maxStackSize = cost.getMaxStackSize();

        while (remaining > 0) {
            ItemStack stack = cost.copy();
            stack.setCount(Math.min(maxStackSize, remaining));
            remaining -= stack.getCount();
            builder.addSlot(RecipeIngredientRole.INPUT, 9999, 9999).addItemStack(stack);
        }
    }
}

package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.CollisionRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class AcceleratorRecipeCategory implements IRecipeCategory<CollisionRecipe> {

    public static final RecipeType<CollisionRecipe> TYPE =
            RecipeType.create(Overdrive.MODID, "accelerator_collision", CollisionRecipe.class);

    private static final int WIDTH = 180;
    private static final int HEIGHT = 80;

    private static final int COLOR_TEXT = 0x404040;

    private final IDrawable icon;
    private final IDrawable background;
    private final Component title;

    public AcceleratorRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ODItems.IRIDIUM_BLOCK.get()));
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.title = Component.translatable("jei.overdrive.accelerator_collision.title");
    }

    @Override
    public RecipeType<CollisionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CollisionRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 30)
                .addIngredients(recipe.inputTarget());

        FluidStack output = recipe.outputFluid();
        if (!output.isEmpty()) {
            int capacity = (int) Math.max(2000, output.getAmount());

            builder.addSlot(RecipeIngredientRole.OUTPUT, 145, 15)
                    .addFluidStack(output.getFluid(), output.getAmount())
                    .setFluidRenderer(capacity, false, 16, 48);
        }
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull CollisionRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addWidget(new AcceleratorRecipeWidget());
    }

    @Override
    public void draw(@NotNull CollisionRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, Component.translatable("jei.overdrive.accelerator_collision.input"), 15, 18, COLOR_TEXT, false);

        FluidStack output = recipe.outputFluid();
        if (!output.isEmpty()) {
            Component resultTitle = Component.translatable("jei.overdrive.accelerator_collision.result");
            int resultWidth = font.width(resultTitle);
            guiGraphics.drawString(font, resultTitle, 161 - resultWidth, 3, COLOR_TEXT, false);
        }
    }
}
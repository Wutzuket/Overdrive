package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.PositronExtractionRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
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
public class PositronExtractionRecipeCategory implements IRecipeCategory<PositronExtractionRecipe> {

    public static final RecipeType<PositronExtractionRecipe> TYPE =
            RecipeType.create(Overdrive.MODID, "positron_extraction", PositronExtractionRecipe.class);

    private static final int WIDTH = 180;
    private static final int HEIGHT = 80;
    private static final int COLOR_TEXT = 0x404040;

    private final IDrawable icon;
    private final IDrawable background;
    private final Component title;

    public PositronExtractionRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ODItems.WOLFRAM_MESH.get()));
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.title = Component.translatable("jei.overdrive.positron_extraction.title");
    }

    @Override
    public RecipeType<PositronExtractionRecipe> getRecipeType() { return TYPE; }
    @Override
    public Component getTitle() { return title; }
    @Override
    public IDrawable getIcon() { return icon; }
    @Override
    public IDrawable getBackground() { return background; }
    @Override
    public int getWidth() { return WIDTH; }
    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull PositronExtractionRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 30)
                .addItemStack(new ItemStack(ODItems.WOLFRAM_MESH.get()));

        FluidStack output = recipe.outputFluid();
        if (!output.isEmpty()) {
            int capacity = (int) Math.max(2000, output.getAmount());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 15)
                    .addFluidStack(output.getFluid(), output.getAmount())
                    .setFluidRenderer(capacity, false, 16, 48);
        if (!recipe.outputCell().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 145, 30)
                    .addItemStack(recipe.outputCell());
        }
    }

    }

    @Override
    public void draw(@NotNull PositronExtractionRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, Component.translatable("jei.overdrive.positron_extraction.input"), 15, 18, COLOR_TEXT, false);
        guiGraphics.drawString(font, Component.translatable("jei.overdrive.positron_extraction.output"), 85, 4, COLOR_TEXT, false);

        guiGraphics.drawString(font, Component.literal("➔"), 72, 34, 0x8B8B8B, false);

        String energyStr = recipe.energyCost() + " FE";
        guiGraphics.drawString(font, Component.literal(energyStr), 15, 66, COLOR_TEXT, false);

        String meshStr = "Mesh: -" + recipe.meshDurabilityCost();
        guiGraphics.drawString(font, Component.literal(meshStr), 85, 66, COLOR_TEXT, false);
    }
}

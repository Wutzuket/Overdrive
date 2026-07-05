package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.CollisionRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class AcceleratorRecipeCategory implements IRecipeCategory<CollisionRecipe> {
    public static final RecipeType<CollisionRecipe> TYPE =
            RecipeType.create(Overdrive.MODID, "collision", CollisionRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEIGHT = 80;

    private final IDrawable icon;
    private final IDrawable background;

    public AcceleratorRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ODItems.IRIDIUM_BLOCK.get()));
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<CollisionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.overdrive.collision.title");
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
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 18)
                .addIngredients(recipe.inputTarget());

        FluidStack output = recipe.outputFluid();
        if (!output.isEmpty()) {
            ItemStack bucket = new ItemStack(output.getFluid().getBucket());
            if (!bucket.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 18)
                        .addItemStack(bucket);
            }
        }
    }

    @Override
    public void draw(@NotNull CollisionRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        NumberFormat fmt = NumberFormat.getInstance();

        String energyStr = Component.translatable("jei.overdrive.collision.energy", fmt.format(recipe.energyCost())).getString();
        guiGraphics.drawString(Minecraft.getInstance().font, energyStr, 4, 42, 0xFF5555, false);

        String timeStr = Component.translatable("jei.overdrive.collision.time", recipe.processTime()).getString();
        guiGraphics.drawString(Minecraft.getInstance().font, timeStr, 4, 54, 0x5555FF, false);

        String durabilityStr = Component.translatable("jei.overdrive.collision.durability", recipe.durabilityCost()).getString();
        guiGraphics.drawString(Minecraft.getInstance().font, durabilityStr, 4, 66, 0xFFAA00, false);

        FluidStack output = recipe.outputFluid();
        if (!output.isEmpty()) {
            String fluidStr = Component.translatable("jei.overdrive.collision.fluid", output.getHoverName().getString(), output.getAmount()).getString();
            guiGraphics.drawString(Minecraft.getInstance().font, fluidStr, 76, 42, 0x5555FF, false);
        }

        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.overdrive.collision.target"), 4, 6, 0x404040, false);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.overdrive.collision.result"), 120, 6, 0x404040, false);
    }
}

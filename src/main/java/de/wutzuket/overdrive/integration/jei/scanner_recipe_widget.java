package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import java.text.NumberFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class scanner_recipe_widget implements IRecipeWidget, IJeiGuiEventListener {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 140;
    private static final int STRUCTURE_WIDTH = 100;
    private static final int GRID_X = 22;
    private static final int GRID_Y = 34;
    private static final int SLOT_SPACING = 18;
    private static final int PREV_X = 8;
    private static final int NEXT_X = 104;
    private static final int BUTTON_Y = 18;
    private static final int BUTTON_SIZE = 12;
    private static final int PANEL_TOP = 10;
    static final int RESULT_X = 118;
    static final int RESULT_Y = 48;
    private static final float STRUCTURE_CENTER_X = GRID_X + (SLOT_SPACING * 1.5F);
    private static final float STRUCTURE_CENTER_Y = GRID_Y + (SLOT_SPACING * 1.5F) + 2.0F;
    private static final float ALL_LAYERS_SCALE = 7.5F;
    private static final float SINGLE_LAYER_SCALE = 9.0F;

    private final scanner_recipe recipe;
    /** 0 = all layers, 1..layerCount = individual layer (1-based) */
    private int viewIndex = 0;

    public scanner_recipe_widget(scanner_recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(0, 0);
    }

    @Override
    public ScreenRectangle getArea() {
        return new ScreenRectangle(0, 0, WIDTH, HEIGHT);
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();

        //drawPanelFrame(guiGraphics);
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.overdrive.scanner.structure"), 8, 8, 0x404040, false);
        guiGraphics.drawString(minecraft.font, getViewLabel(), 34, 20, 0x404040, false);
        drawArrowButton(guiGraphics, minecraft, PREV_X, BUTTON_Y, "<");
        drawArrowButton(guiGraphics, minecraft, NEXT_X, BUTTON_Y, ">");

        drawStructure(guiGraphics, minecraft, mouseX, mouseY);

        if (recipe.acceptMirrored()) {
            guiGraphics.drawString(minecraft.font, Component.translatable("jei.overdrive.scanner.mirrored"), 8, HEIGHT - 10, 0x606060, false);
        }

        if (recipe.energy() > 0) {
            String formatted = NumberFormat.getInstance().format(recipe.energy());
            String energyText = Component.translatable("jei.overdrive.scanner.energy", formatted).getString();
            guiGraphics.drawString(minecraft.font, Component.literal(energyText), 8, HEIGHT - 22, 0xFF5555, false);
        }

        drawResult(guiGraphics, minecraft);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (isInside(mouseX, mouseY, PREV_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            tooltip.add(Component.translatable("jei.overdrive.scanner.prev_layer"));
            return;
        }
        if (isInside(mouseX, mouseY, NEXT_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            tooltip.add(Component.translatable("jei.overdrive.scanner.next_layer"));
            return;
        }

        ItemStack stack = getStackAt(mouseX, mouseY);
        if (stack != null) {
            tooltip.addAll(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
            return;
        }

        if (isInside(mouseX, mouseY, RESULT_X, RESULT_Y, 16, 16)) {
            tooltip.addAll(Screen.getTooltipFromItem(Minecraft.getInstance(), recipe.getResultStack()));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInside(mouseX, mouseY, PREV_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            changeView(-1);
            return true;
        }
        if (isInside(mouseX, mouseY, NEXT_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            changeView(1);
            return true;
        }

        ItemStack stack = getStackAt(mouseX, mouseY);
        if (stack != null && !stack.isEmpty()) {
            IJeiRuntime runtime = scanner_jei_plugin.getRuntime();
            if (runtime != null) {
                var focus = runtime.getJeiHelpers().getFocusFactory()
                        .createFocus(button == 0 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, stack);
                runtime.getRecipesGui().show(focus);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0D) {
            changeView(-1);
            return true;
        }
        if (scrollY < 0.0D) {
            changeView(1);
            return true;
        }
        return false;
    }

    private Component getViewLabel() {
        if (viewIndex == 0) {
            return Component.translatable("jei.overdrive.scanner.all_layers");
        }
        return Component.translatable("jei.overdrive.scanner.layer", viewIndex, recipe.layerCount());
    }

    private void drawPanelFrame(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, PANEL_TOP, STRUCTURE_WIDTH, PANEL_TOP + 1, 0xFF000000);
        guiGraphics.fill(STRUCTURE_WIDTH - 1, 0, STRUCTURE_WIDTH, HEIGHT, 0xFF000000);
        guiGraphics.fill(0, HEIGHT - 1, WIDTH, HEIGHT, 0xFF000000);
    }

    private void drawArrowButton(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, String arrow) {
        guiGraphics.fill(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, 0xFFB0B0B0);
        guiGraphics.fill(x + 1, y + 1, x + BUTTON_SIZE - 1, y + BUTTON_SIZE - 1, 0xFF404040);
        guiGraphics.drawString(minecraft.font, Component.literal(arrow), x + 4, y + 2, 0xFFFFFF, false);
    }

    private void drawStructure(GuiGraphics guiGraphics, Minecraft minecraft, double mouseX, double mouseY) {
        float scale = viewIndex == 0 ? ALL_LAYERS_SCALE : SINGLE_LAYER_SCALE;
        jei_block_render_helper.renderStructure(guiGraphics, recipe, viewIndex, STRUCTURE_CENTER_X, STRUCTURE_CENTER_Y, scale);
    }

    private void drawResult(GuiGraphics guiGraphics, Minecraft minecraft) {
        ItemStack result = recipe.getResultStack();
        drawSlot(guiGraphics, RESULT_X, RESULT_Y);
        guiGraphics.renderItem(result, RESULT_X + 1, RESULT_Y + 1);
        guiGraphics.renderItemDecorations(minecraft.font, result, RESULT_X + 1, RESULT_Y + 1);
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.overdrive.scanner.result"), RESULT_X - 2, RESULT_Y - 12, 0x404040, false);
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF373737);
    }

    private @Nullable ItemStack getStackAt(double mouseX, double mouseY) {
        float scale = viewIndex == 0 ? ALL_LAYERS_SCALE : SINGLE_LAYER_SCALE;
        return jei_block_render_helper.getHoveredBlock(recipe, viewIndex, STRUCTURE_CENTER_X, STRUCTURE_CENTER_Y, scale, mouseX, mouseY);
    }

    private void changeView(int delta) {
        int viewCount = recipe.layerCount() + 1;
        viewIndex = Math.floorMod(viewIndex + delta, viewCount);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}

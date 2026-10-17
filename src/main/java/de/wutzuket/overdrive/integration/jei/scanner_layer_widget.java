package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.block.scanner.recipe.scanner_block_matcher;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class scanner_layer_widget implements IRecipeWidget, IJeiGuiEventListener {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 90;

    // Grid leicht verschoben, um Abstand zu den Buttons zu gewinnen
    private static final int GRID_X = 22;
    private static final int GRID_Y = 30;
    private static final int SLOT_SPACING = 18;

    // Buttons weiter nach außen gerückt
    private static final int PREV_X = 12;
    private static final int NEXT_X = 252;
    private static final int BUTTON_Y = 8;
    private static final int BUTTON_SIZE = 12;

    // Resultat-Anzeige leicht angepasst
    private static final int RESULT_X = 134;
    private static final int RESULT_Y = 38;

    private final scanner_recipe recipe;
    private int layer;

    public scanner_layer_widget(scanner_recipe recipe) {
        this.recipe = recipe;
        this.layer = 0;
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

        // 1. Titel & Layer-Anzeige sauber zentrieren (Verhindert Überlappung komplett)
        Component layerText = Component.translatable("jei.overdrive.scanner.layer", layer + 1, recipe.layerCount());
        int textWidth = minecraft.font.width(layerText);
        guiGraphics.drawString(minecraft.font, layerText, (WIDTH - textWidth) / 2, 10, 0x404040, false);

        // 2. Interaktive Buttons zeichnen
        boolean hoverPrev = isInside(mouseX, mouseY, PREV_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE);
        boolean hoverNext = isInside(mouseX, mouseY, NEXT_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE);
        drawArrowButton(guiGraphics, PREV_X, BUTTON_Y, "<", hoverPrev);
        drawArrowButton(guiGraphics, NEXT_X, BUTTON_Y, ">", hoverNext);

        // 3. Grid & Resultat beschriften
        drawLayerGrid(guiGraphics, minecraft);

        guiGraphics.drawString(minecraft.font, Component.translatable("jei.overdrive.scanner.result"), RESULT_X - 6, RESULT_Y - 12, 0x404040, false);

        // Gespiegelt-Text unten links sauber platzieren
        if (recipe.acceptMirrored()) {
            guiGraphics.drawString(minecraft.font, Component.translatable("jei.overdrive.scanner.mirrored"), 12, HEIGHT - 14, 0x606060, false);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        // 1. Standard-Buttons abfangen
        if (isInside(mouseX, mouseY, PREV_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            tooltip.add(Component.translatable("jei.overdrive.scanner.prev_layer"));
            return;
        }
        if (isInside(mouseX, mouseY, NEXT_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            tooltip.add(Component.translatable("jei.overdrive.scanner.next_layer"));
            return;
        }

        // 2. 3D-Struktur abfragen (Hier übergeben wir die Parameter, die auch beim Rendern genutzt werden)
        // viewIndex: 0 = Alle Layer, layer + 1 = Einzelner Layer
        int viewIndex = layer + 1;

        // WICHTIG: Nutze hier exakt dieselben Werte für centerX, centerY und scale wie in deiner Haupt-Renderschleife!
        // Falls deine Werte im Screen abweichen, passe diese 3 Variablen hier an:
        float structureCenterX = 60.0F;  // Beispielwert, passe ihn an deine Render-Position an
        float structureCenterY = 55.0F;  // Beispielwert, passe ihn an deine Render-Position an
        float structureScale = 18.0F;    // Beispielwert, passe ihn an deine Render-Skalierung an

        ItemStack hoveredBlock = jei_block_render_helper.getHoveredBlock(
                recipe, viewIndex, structureCenterX, structureCenterY, structureScale, mouseX, mouseY
        );

        if (hoveredBlock != null && !hoveredBlock.isEmpty()) {
            tooltip.add(hoveredBlock.getHoverName());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        if (isInside(mouseX, mouseY, PREV_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            changeLayer(-1);
            return true;
        }
        if (isInside(mouseX, mouseY, NEXT_X, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            changeLayer(1);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0D) {
            changeLayer(-1);
            return true;
        }
        if (scrollY < 0.0D) {
            changeLayer(1);
            return true;
        }
        return false;
    }

    // Verschönerte Buttons mit Hover-Effekt (Minecraft-Style)
    private void drawArrowButton(GuiGraphics guiGraphics, int x, int y, String arrow, boolean hovered) {
        int baseColor = hovered ? 0xFFCCCCCC : 0xFF8B8B8B; // Heller beim Hovern
        guiGraphics.fill(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, 0xFF373737); // Äußerer Rand
        guiGraphics.fill(x + 1, y + 1, x + BUTTON_SIZE - 1, y + BUTTON_SIZE - 1, baseColor); // Button-Körper
        guiGraphics.fill(x + 1, y + 1, x + BUTTON_SIZE - 2, y + BUTTON_SIZE - 2, hovered ? 0xFFEEEEEE : 0xFFFFFFFF); // Highlight oben/links
        guiGraphics.fill(x + 2, y + 2, x + BUTTON_SIZE - 2, y + BUTTON_SIZE - 2, baseColor);

        Minecraft minecraft = Minecraft.getInstance();
        int arrowWidth = minecraft.font.width(arrow);
        guiGraphics.drawString(minecraft.font, arrow, x + (BUTTON_SIZE - arrowWidth) / 2 + 1, y + 2, 0x000000, false);
    }

    // Slots im klassischen Minecraft-Inventar-Design gezeichnet
    private void drawLayerGrid(GuiGraphics guiGraphics, Minecraft minecraft) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = GRID_X + (col * SLOT_SPACING);
                int y = GRID_Y + (row * SLOT_SPACING);

                // Dreidimensionaler Slot-Effekt (Dunkler Hintergrund, helle Kanten unten/rechts)
                guiGraphics.fill(x, y, x + 18, y + 18, 0xFF373737);
                guiGraphics.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF);
                guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);

                if (!recipe.isActiveCell(layer, row, col)) {
                    continue;
                }

                scanner_block_matcher matcher = recipe.getMatcher(layer, row, col);
                List<ItemStack> stacks = matcher.displayStacks();
                if (stacks.isEmpty()) {
                    guiGraphics.drawString(minecraft.font, "?", x + 6, y + 5, 0xE0E0E0, false);
                    continue;
                }

                ItemStack stack = stacks.get(0);
                // Items leicht versetzt (+1), da der Slot jetzt 18x18 statt 16x16 Pixel groß ist
                guiGraphics.renderItem(stack, x + 1, y + 1);
                guiGraphics.renderItemDecorations(minecraft.font, stack, x + 1, y + 1);
            }
        }
    }

    private void changeLayer(int delta) {
        int layerCount = recipe.layerCount();
        if (layerCount <= 1) {
            layer = 0;
            return;
        }
        layer = Math.floorMod(layer + delta, layerCount);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
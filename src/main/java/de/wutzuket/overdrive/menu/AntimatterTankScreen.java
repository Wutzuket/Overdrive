package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AntimatterTankScreen extends AbstractContainerScreen<AntimatterTankMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 192;

    /** Position and size of the tank gauge. */
    private static final int TANK_X = 12;
    private static final int TANK_Y = 20;
    private static final int TANK_W = 26;
    private static final int TANK_H = 84;

    private static final int INPUT_LABEL_X = 54;
    private static final int OUTPUT_LABEL_X = 90;

    public AntimatterTankScreen(AntimatterTankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.titleLabelY = 4;
        this.inventoryLabelY = HEIGHT - 94;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        GuiStyle.panel(g, x, y, WIDTH, HEIGHT);

        renderTank(g, x + TANK_X, y + TANK_Y);

        // arrow between the slots
        int arrowX = x + AntimatterTankMenu.INPUT_SLOT_X + 20;
        int arrowY = y + AntimatterTankMenu.INPUT_SLOT_Y + 8;
        g.fill(arrowX, arrowY, arrowX + 10, arrowY + 2, GuiStyle.COL_TEXT_DIM);
        g.fill(arrowX + 8, arrowY - 2, arrowX + 10, arrowY + 5, GuiStyle.COL_TEXT_DIM);

        GuiStyle.slotFrames(g, menu, x, y);
    }

    private void renderTank(GuiGraphics g, int x, int y) {
        GuiStyle.inset(g, x, y, TANK_W, TANK_H);
        int innerX = x + 2;
        int innerY = y + 2;
        int innerW = TANK_W - 4;
        int innerH = TANK_H - 4;

        g.fill(innerX, innerY, innerX + innerW, innerY + innerH, GuiStyle.COL_SCREEN);

        AntimatterTankMenu menu = this.menu;
        int amount = menu.getAmount();
        int capacity = menu.getCapacity();
        if (amount > 0 && capacity > 0) {
            int fill = Math.max(1, (int) ((long) amount * innerH / capacity));
            int top = innerY + innerH - fill;
            Antimatter type = menu.getBlockEntity().getAntimatterInTank(0);
            int color = type == null ? GuiStyle.COL_CYAN : type.color();
            g.fill(innerX, top, innerX + innerW, innerY + innerH, color);
            g.fill(innerX, top, innerX + innerW, top + 2, 0x45FFFFFF);
            g.fill(innerX, innerY + innerH - 2, innerX + innerW, innerY + innerH, 0x30000000);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, title, 8, 4, GuiStyle.COL_TEXT, false);

        AntimatterTankMenu menu = this.menu;
        Antimatter type = menu.getBlockEntity().getAntimatterInTank(0);

        g.drawString(font, "Input", INPUT_LABEL_X, AntimatterTankMenu.INPUT_SLOT_Y - 10, GuiStyle.COL_TEXT_DIM, false);
        g.drawString(font, "Output", OUTPUT_LABEL_X, AntimatterTankMenu.INPUT_SLOT_Y - 10, GuiStyle.COL_TEXT_DIM, false);

        int infoY = AntimatterTankMenu.INPUT_SLOT_Y + 26;
        g.drawString(font, "Content:", 44, infoY, GuiStyle.COL_TEXT_DIM, false);
        String content = type == null ? "Empty" : type.displayName().getString();
        g.drawString(font, content, 44, infoY + 10, type == null ? GuiStyle.COL_TEXT_DIM : type.color(), false);
        g.drawString(font, menu.getAmount() + " / " + menu.getCapacity() + " mB", 44, infoY + 20, GuiStyle.COL_TEXT, false);
        if (menu.getBlockEntity().isCreative()) {
            g.drawString(font, Component.translatable("gui.overdrive.antimatter_tank.creative"),
                    44, infoY + 30, GuiStyle.COL_YELLOW, false);
        }

        g.drawString(font, this.playerInventoryTitle, 8, this.inventoryLabelY, GuiStyle.COL_TEXT_DIM, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);
        renderTooltip(g, mx, my);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        AntimatterTankMenu menu = this.menu;

        if (mx >= x + TANK_X && mx <= x + TANK_X + TANK_W
                && my >= y + TANK_Y && my <= y + TANK_Y + TANK_H) {
            List<Component> lines = new ArrayList<>();
            Antimatter type = menu.getBlockEntity().getAntimatterInTank(0);
            lines.add(Component.literal(type == null ? "Empty" : type.displayName().getString()));
            lines.add(Component.literal(menu.getAmount() + " / " + menu.getCapacity() + " mB"));
            g.renderTooltip(font, lines, java.util.Optional.empty(), mx, my);
            return;
        }

        ItemStack input = menu.getBlockEntity().getInputStack();
        if (!input.isEmpty() && isHovering(x + AntimatterTankMenu.INPUT_SLOT_X, y + AntimatterTankMenu.INPUT_SLOT_Y,
                x + AntimatterTankMenu.INPUT_SLOT_X + 16, y + AntimatterTankMenu.INPUT_SLOT_Y + 16, mx, my)) {
            g.renderTooltip(font, List.of(input.getHoverName()), java.util.Optional.empty(), mx, my);
            return;
        }

        super.renderTooltip(g, mx, my);
    }
}

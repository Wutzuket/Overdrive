package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.scanner.scanner_blockentity;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class scanner_screen extends AbstractContainerScreen<scanner_menu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int ENERGY_X = 8;
    private static final int ENERGY_Y = 18;
    private static final int ENERGY_W = 12;
    private static final int ENERGY_H = 44;
    private static final int ITEMS_LABEL_X = 26;
    private static final int ITEMS_LABEL_Y = 10;
    private static final int STATUS_X = 80;
    private static final int STATUS_Y = 52;
    private static final int RESULT_X = 140;
    private static final int RESULT_Y = 30;

    public scanner_screen(scanner_menu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        titleLabelY = 4;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        scanner_blockentity scanner = menu.getBlockEntity();

        GuiStyle.panel(g, x, y, WIDTH, HEIGHT);
        GuiStyle.metricGauge(g, font, x + ENERGY_X, y + ENERGY_Y, ENERGY_W, ENERGY_H,
                scanner.getEnergyStored() / (float) Math.max(1, scanner.getMaxEnergyStored()),
                GuiStyle.COL_GREEN, null);
        GuiStyle.slotFrames(g, menu, x, y);
        GuiStyle.slotFrame(g, x + RESULT_X, y + RESULT_Y);

        ItemStack result = scanner.getMatchedResultStack();
        if (!result.isEmpty()) {
            g.renderItem(result, x + RESULT_X + 1, y + RESULT_Y + 1);
            g.renderItemDecorations(font, result, x + RESULT_X + 1, y + RESULT_Y + 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        scanner_blockentity scanner = menu.getBlockEntity();

        g.drawString(font, title, 8, 4, GuiStyle.COL_TEXT, false);
        g.drawString(font, Component.translatable("container.overdrive.scanner.items"),
                ITEMS_LABEL_X, ITEMS_LABEL_Y, GuiStyle.COL_TEXT_DIM, false);
        g.drawString(font, Component.translatable("container.overdrive.scanner.result"),
                RESULT_X - 2, RESULT_Y - 10, GuiStyle.COL_TEXT_DIM, false);

        Component status = statusText(scanner);
        int statusColor = scanner.isAnimating() || scanner.isWaitingToDrop()
                ? GuiStyle.COL_YELLOW
                : scanner.getMatchedRecipeId() != null ? GuiStyle.COL_GREEN : GuiStyle.COL_TEXT_DIM;
        g.drawString(font, status, STATUS_X, STATUS_Y, statusColor, false);

        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, GuiStyle.COL_TEXT_DIM, false);
    }

    private static Component statusText(scanner_blockentity scanner) {
        if (scanner.isAnimating()) {
            return Component.translatable("container.overdrive.scanner.status.scanning");
        }
        if (scanner.isWaitingToDrop()) {
            return Component.translatable("container.overdrive.scanner.status.dropping");
        }
        if (scanner.getMatchedRecipeId() != null) {
            return Component.translatable("container.overdrive.scanner.status.ready");
        }
        return Component.translatable("container.overdrive.scanner.status.idle");
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        scanner_blockentity scanner = menu.getBlockEntity();

        int energyX = leftPos + ENERGY_X;
        int energyY = topPos + ENERGY_Y;
        if (mouseX >= energyX && mouseX <= energyX + ENERGY_W
                && mouseY >= energyY && mouseY <= energyY + ENERGY_H) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("container.overdrive.scanner.energy"));
            lines.add(Component.literal(ODFormat.formatEnergyPair(
                    scanner.getEnergyStored(), scanner.getMaxEnergyStored())));
            g.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
            return;
        }

        int resultX = leftPos + RESULT_X;
        int resultY = topPos + RESULT_Y;
        if (mouseX >= resultX && mouseX <= resultX + 18
                && mouseY >= resultY && mouseY <= resultY + 18) {
            ItemStack result = scanner.getMatchedResultStack();
            if (!result.isEmpty()) {
                g.renderTooltip(font, result, mouseX, mouseY);
                return;
            }
        }

        super.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}

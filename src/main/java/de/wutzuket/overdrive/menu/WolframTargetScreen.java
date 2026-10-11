package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.GuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WolframTargetScreen extends AbstractContainerScreen<WolframTargetMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public WolframTargetScreen(WolframTargetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);
        GuiStyle.inset(guiGraphics, x + 6, y + 80, 164, 82);
        GuiStyle.slotFrames(guiGraphics, menu, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, GuiStyle.COL_TEXT, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.wolfram_target.slot"),
                8, 22, GuiStyle.COL_CYAN, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                GuiStyle.COL_TEXT_DIM, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

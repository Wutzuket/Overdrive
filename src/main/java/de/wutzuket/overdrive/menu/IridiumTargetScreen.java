package de.wutzuket.overdrive.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class IridiumTargetScreen extends AbstractContainerScreen<IridiumTargetMenu> {
    // Wir nutzen die Dispenser-Textur, da sie die perfekte Höhe (166px) für das Inventar besitzt
    private static final ResourceLocation CONTAINER_BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public IridiumTargetScreen(IridiumTargetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;

        // Positioniert den automatischen "Inventar"-Text korrekt über den Slots unten
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 1. Das standardmäßige Minecraft-Inventar unten und den Rahmen zeichnen
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 2. Den oberen Bereich großzügiger übermalen (x+6 bis x+170 und y+16 bis y+72)
        // Das deckt die Ränder der Dispenser-Slots vollständig ab
        guiGraphics.fill(x + 6, y + 16, x + 170, y + 72, 0xFF3A3A3A);

        // 3. Einen einzelnen Standard-Slot aus der Textur ausschneiden und über das Ziel-Feld legen
        guiGraphics.blit(CONTAINER_BACKGROUND, x + 79, y + 34, 61, 17, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Titel des Blocks oben links (Dunkelgrau, da der Hintergrund dort jetzt hell ist)
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);

        // "Target Slot"-Text (In Weiß, damit er auf dem dunkelgrauen Kasten lesbar bleibt)
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.iridium_target.slot"), 8, 22, 0xFFFFFF, false);

        // Der standardmäßige "Inventar"-Schriftzug für die Spielerslots unten
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // Aktiviert Tooltips, wenn man mit der Maus über Items fährt
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
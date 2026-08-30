package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.network.HologramCreateFilePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

public class CreateHologramScreen extends Screen {
    private static final int WIDTH = 360;
    private static final int HEIGHT = 226;
    private static final int MAX_CONTENT_LENGTH = 65536;

    private final HologramMenu menu;
    private final Inventory inventory;
    private EditBox nameField;
    private MultiLineEditBox jsonField;
    private Component error;

    public CreateHologramScreen(HologramMenu menu, Inventory inventory) {
        super(Component.translatable("container.overdrive.hologram_block.create"));
        this.menu = menu;
        this.inventory = inventory;
    }

    @Override
    protected void init() {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        nameField = new EditBox(this.font, x + 8, y + 28, 300, 18,
                Component.translatable("container.overdrive.hologram_block.create_name"));
        nameField.setMaxLength(64);
        nameField.setFilter(s -> s.matches("[A-Za-z0-9._\\- ]*"));
        nameField.setFocused(true);
        addRenderableWidget(nameField);

        jsonField = new MultiLineEditBox(this.font, x + 8, y + 56, 344, 130,
                Component.translatable("container.overdrive.hologram_block.create_json_placeholder"),
                Component.translatable("container.overdrive.hologram_block.create_json"));
        jsonField.setCharacterLimit(MAX_CONTENT_LENGTH);
        addRenderableWidget(jsonField);

        addRenderableWidget(Button.builder(Component.translatable("container.overdrive.hologram_block.create_save"), button -> createFile())
                .bounds(x + 8, y + 194, 80, 18)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("container.overdrive.hologram_block.create_cancel"), button -> close())
                .bounds(x + 272, y + 194, 80, 18)
                .build());
    }

    private void createFile() {
        String rawName = nameField == null ? "" : nameField.getValue().trim();
        error = null;
        if (rawName.isEmpty()) {
            error = Component.translatable("container.overdrive.hologram_block.create_error_name");
            return;
        }

        String fileName = rawName;
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".json")) {
            fileName = fileName + ".json";
        }
        if (menu.getAvailableFiles().contains(fileName)) {
            error = Component.translatable("container.overdrive.hologram_block.create_error_exists");
            return;
        }

        String content = jsonField.getValue();
        try {
            com.google.gson.JsonParser.parseString(content);
        } catch (Exception ignored) {
            error = Component.translatable("container.overdrive.hologram_block.create_error_json");
            return;
        }

        PacketDistributor.sendToServer(new HologramCreateFilePayload(menu.getBlockEntity().getBlockPos(), fileName, content));
        close();
    }

    private void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new HologramScreen(this.menu, this.inventory,
                    Component.translatable("container.overdrive.hologram_block")));
        }
    }

    @Override
    public void onClose() {
        close();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x40000000);

        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFD8D8D8);
        guiGraphics.fill(x, y, x + WIDTH, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + HEIGHT, 0xFFFFFFFF);
        guiGraphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFF5A5A5A);
        guiGraphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF5A5A5A);

        guiGraphics.drawString(this.font, this.title, x + 8, y + 6, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.hologram_block.create_name"), x + 8, y + 18, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.hologram_block.create_json"), x + 8, y + 46, 0x404040, false);
        if (error != null) {
            guiGraphics.drawString(this.font, error, x + 96, y + 178, 0xFFFF5555, false);
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}
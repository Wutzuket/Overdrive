package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.network.HologramMenuStatePayload;
import de.wutzuket.overdrive.network.HologramMenuUpdatePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HologramScreen extends AbstractContainerScreen<HologramMenu> {
    private static final int WIDTH = 360;
    private static final int HEIGHT = 226;
    private static final int FILE_VISIBLE_COUNT = 8;
    private static final double SLIDER_STEP = 0.1D;
    private static final int EDIT_WIDTH = 48;

    private ValueSlider offsetXSlider;
    private ValueSlider offsetYSlider;
    private ValueSlider offsetZSlider;
    private ValueSlider scaleSlider;
    private ValueSlider rotationXSlider;
    private ValueSlider rotationYSlider;
    private ValueSlider rotationZSlider;
    private boolean controlsBuilt;
    private int fileScrollIndex;

    public HologramScreen(HologramMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        rebuildControls();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (menu.hasClientSync() && !controlsBuilt) {
            rebuildControls();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.isDragging() && this.getFocused() != null) {
            return this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public void applyServerState(HologramMenuStatePayload payload) {
        boolean fileStateChanged = !menu.getAvailableFiles().equals(payload.availableFiles())
                || !Objects.equals(menu.getSelectedFile(), payload.selectedFile())
                || menu.getSpin() != payload.spin();
        menu.applyClientSync(payload.availableFiles(), payload.selectedFile(),
                payload.offsetX(), payload.offsetY(), payload.offsetZ(),
                payload.scale(), payload.rotationX(), payload.rotationY(), payload.rotationZ(),
                payload.spin());
        if (fileStateChanged) {
            controlsBuilt = false;
        }
    }

    private void rebuildControls() {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        clearWidgets();

        List<String> files = menu.getAvailableFiles();
        int maxScroll = Math.max(0, files.size() - FILE_VISIBLE_COUNT);
        int selectedIndex = menu.getSelectedFile() == null ? -1 : files.indexOf(menu.getSelectedFile());
        if (selectedIndex >= 0) {
            fileScrollIndex = Math.max(0, Math.min(selectedIndex - 2, maxScroll));
        } else {
            fileScrollIndex = Math.max(0, Math.min(fileScrollIndex, maxScroll));
        }

        addRenderableWidget(new ButtonHelper(left + 112, top + 24, 16, 14, Component.literal("^"), button -> {
            if (fileScrollIndex > 0) {
                fileScrollIndex--;
                rebuildControls();
            }
        }, GuiStyle.COL_CYAN));

        addRenderableWidget(new ButtonHelper(left + 112, top + 188, 16, 14, Component.literal("v"), button -> {
            if (fileScrollIndex < maxScroll) {
                fileScrollIndex++;
                rebuildControls();
            }
        }, GuiStyle.COL_CYAN));

        addRenderableWidget(new ButtonHelper(left + 8, top + 204, 20, 18, Component.literal("+"),
                button -> openCreateScreen(), GuiStyle.COL_CYAN));

        addRenderableWidget(new ButtonHelper(left + 30, top + 204, 40, 18,
                Component.translatable("container.overdrive.hologram_block.none_select"),
                button -> selectNoFile(), GuiStyle.COL_CYAN));

        boolean spin = menu.getBlockEntity().getSpin();
        addRenderableWidget(new ButtonHelper(left + 72, top + 204, 44, 18,
                Component.translatable(spin
                        ? "container.overdrive.hologram_block.spin_on"
                        : "container.overdrive.hologram_block.spin_off"),
                button -> toggleSpin(), GuiStyle.COL_CYAN));

        int listStart = fileScrollIndex;
        int listEnd = Math.min(files.size(), listStart + FILE_VISIBLE_COUNT);
        int buttonX = left + 8;
        int buttonY = top + 24;
        int buttonWidth = 108;
        int buttonHeight = 18;
        for (int i = listStart; i < listEnd; i++) {
            String fileName = files.get(i);
            boolean selected = fileName.equals(menu.getSelectedFile());
            String prefix = selected ? "> " : "  ";
            addRenderableWidget(new ButtonHelper(buttonX, buttonY + (i - listStart) * 20, buttonWidth, buttonHeight,
                    Component.literal(prefix + fileName), button -> selectFile(fileName), GuiStyle.COL_CYAN));
        }

        int sliderX = left + 146;
        int sliderY = top + 24;
        int sliderWidth = 140;
        int sliderHeight = 20;
        int gap = 4;

        offsetXSlider = addSliderRow(sliderX, sliderY, sliderWidth, sliderHeight,
                Component.translatable("container.overdrive.hologram_block.offset_x"), -8.0D, 8.0D,
                menu.getOffsetX(), value -> menu.getBlockEntity().setOffsetX(value));
        offsetYSlider = addSliderRow(sliderX, sliderY + (sliderHeight + gap), sliderWidth, sliderHeight,
                Component.translatable("container.overdrive.hologram_block.offset_y"), -8.0D, 8.0D,
                menu.getOffsetY(), value -> menu.getBlockEntity().setOffsetY(value));
        offsetZSlider = addSliderRow(sliderX, sliderY + 2 * (sliderHeight + gap), sliderWidth, sliderHeight,
                Component.translatable("container.overdrive.hologram_block.offset_z"), -8.0D, 8.0D,
                menu.getOffsetZ(), value -> menu.getBlockEntity().setOffsetZ(value));
        scaleSlider = addSliderRow(sliderX, sliderY + 3 * (sliderHeight + gap), sliderWidth, sliderHeight,
                Component.translatable("container.overdrive.hologram_block.scale"), 0.1D, 4.0D,
                menu.getScale(), value -> menu.getBlockEntity().setScale(value));
        rotationXSlider = addSliderRow(sliderX, sliderY + 4 * (sliderHeight + gap), sliderWidth, sliderHeight,
                Component.translatable("container.overdrive.hologram_block.rotation_x"), -180.0D, 180.0D,
                menu.getRotationX(), value -> menu.getBlockEntity().setRotationX(value));
        rotationYSlider = addSliderRow(sliderX, sliderY + 5 * (sliderHeight + gap), sliderWidth, sliderHeight,
                Component.translatable("container.overdrive.hologram_block.rotation_y"), -180.0D, 180.0D,
                menu.getRotationY(), value -> menu.getBlockEntity().setRotationY(value));
        rotationZSlider = addSliderRow(sliderX, sliderY + 6 * (sliderHeight + gap), sliderWidth, sliderHeight,
                Component.translatable("container.overdrive.hologram_block.rotation_z"), -180.0D, 180.0D,
                menu.getRotationZ(), value -> menu.getBlockEntity().setRotationZ(value));

        controlsBuilt = true;
    }

    private ValueSlider addSliderRow(int x, int y, int width, int height, Component label,
                                     double min, double max, double current, ValueConsumer consumer) {
        ValueSlider slider = new ValueSlider(x, y, width, height, label, min, max, current, consumer);

        EditBox edit = new EditBox(this.font, x + width + 8, y, EDIT_WIDTH, height,
                Component.translatable("container.overdrive.hologram_block.value")) {
            @Override
            public void setFocused(boolean focused) {
                super.setFocused(focused);
                if (!focused) {
                    setValue(formatValue(slider.getValue()));
                }
            }
        };
        edit.setMaxLength(8);
        edit.setFilter(s -> s.matches("[-+]?[0-9]*\\.?[0-9]*"));
        edit.setValue(formatValue(current));

        slider.setLinkedEdit(edit);

        edit.setResponder(text -> {
            if (!edit.isFocused()) {
                return;
            }
            try {
                double parsed = Double.parseDouble(text);
                if (!Double.isNaN(parsed)) {
                    slider.setValueFromActual(Mth.clamp(parsed, min, max));
                }
            } catch (NumberFormatException ignored) {
            }
        });

        addRenderableWidget(slider);
        addRenderableWidget(edit);
        return slider;
    }

    private void selectFile(String fileName) {
        menu.applyClientSync(menu.getAvailableFiles(), fileName,
                offsetXSlider.getValue(), offsetYSlider.getValue(), offsetZSlider.getValue(),
                scaleSlider.getValue(), rotationXSlider.getValue(), rotationYSlider.getValue(), rotationZSlider.getValue(),
                menu.getBlockEntity().getSpin());
        sendCurrentStateToServer();
        rebuildControls();
    }

    private void selectNoFile() {
        menu.applyClientSync(menu.getAvailableFiles(), null,
                offsetXSlider.getValue(), offsetYSlider.getValue(), offsetZSlider.getValue(),
                scaleSlider.getValue(), rotationXSlider.getValue(), rotationYSlider.getValue(), rotationZSlider.getValue(),
                menu.getBlockEntity().getSpin());
        sendCurrentStateToServer();
        rebuildControls();
    }

    private void toggleSpin() {
        menu.getBlockEntity().setSpin(!menu.getBlockEntity().getSpin());
        sendCurrentStateToServer();
        rebuildControls();
    }

    private void openCreateScreen() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.setScreen(new CreateHologramScreen(this.menu, this.minecraft.player.getInventory()));
        }
    }

    private void sendCurrentStateToServer() {
        PacketDistributor.sendToServer(new HologramMenuUpdatePayload(
                menu.getBlockEntity().getBlockPos(),
                menu.getSelectedFile(),
                offsetXSlider.getValue(),
                offsetYSlider.getValue(),
                offsetZSlider.getValue(),
                scaleSlider.getValue(),
                rotationXSlider.getValue(),
                rotationYSlider.getValue(),
                rotationZSlider.getValue(),
                menu.getBlockEntity().getSpin()
        ));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);
        GuiStyle.inset(guiGraphics, x + 140, y + 18, 206, 188);

        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.hologram_block.files"),
                x + 8, y + 14, GuiStyle.COL_TEXT_DIM, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.hologram_block.transform"),
                x + 146, y + 14, GuiStyle.COL_TEXT_DIM, false);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, GuiStyle.COL_TEXT, false);
        if (menu.getSelectedFile() != null) {
            guiGraphics.drawString(this.font, Component.literal(menu.getSelectedFile()), 8, 194, GuiStyle.COL_CYAN, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.hologram_block.none"), 8, 194, GuiStyle.COL_TEXT_DIM, false);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private interface ValueConsumer {
        void accept(double value);
    }

    private final class ValueSlider extends AbstractSliderButton {
        private final Component label;
        private final double min;
        private final double max;
        private final ValueConsumer consumer;
        private EditBox linkedEdit;

        private ValueSlider(int x, int y, int width, int height, Component label, double min, double max, double current,
                            ValueConsumer consumer) {
            super(x, y, width, height, label, normalize(current, min, max));
            this.label = label;
            this.min = min;
            this.max = max;
            this.consumer = consumer;
            updateMessage();
        }

        public void setLinkedEdit(EditBox edit) {
            this.linkedEdit = edit;
        }

        public void setValueFromActual(double actual) {
            double newValue = normalize(actual, min, max);
            if (newValue != this.value) {
                this.value = newValue;
                this.applyValue();
            } else {
                this.updateMessage();
            }
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("container.overdrive.hologram_block.value", label, formatValue(getActualValue())));
        }

        @Override
        protected void applyValue() {
            double actual = getActualValue();
            consumer.accept(actual);
            updateMessage();
            if (linkedEdit != null && !linkedEdit.isFocused()) {
                linkedEdit.setValue(formatValue(actual));
            }
            sendCurrentStateToServer();
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
                double direction = keyCode == GLFW.GLFW_KEY_LEFT ? -1.0D : 1.0D;
                setValueFromActual(getActualValue() + direction * SLIDER_STEP);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        public double getValue() {
            return getActualValue();
        }

        private double getActualValue() {
            return min + (max - min) * this.value;
        }

        private static double normalize(double actual, double min, double max) {
            if (max <= min) {
                return 0.0D;
            }
            return Math.max(0.0D, Math.min(1.0D, (actual - min) / (max - min)));
        }
    }

    private static String formatValue(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
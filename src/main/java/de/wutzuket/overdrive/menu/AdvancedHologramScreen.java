package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.advanced_hologram.AdvancedHologramBlockEntity;
import de.wutzuket.overdrive.network.AdvancedHologramConsoleInputPayload;
import de.wutzuket.overdrive.network.AdvancedHologramCreateScriptPayload;
import de.wutzuket.overdrive.network.AdvancedHologramDeleteScriptPayload;
import de.wutzuket.overdrive.network.AdvancedHologramMenuStatePayload;
import de.wutzuket.overdrive.network.AdvancedHologramRunPayload;
import de.wutzuket.overdrive.network.AdvancedHologramSaveScriptPayload;
import de.wutzuket.overdrive.network.AdvancedHologramSelectScriptPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdvancedHologramScreen extends AbstractContainerScreen<AdvancedHologramMenu> {
    private static final int WIDTH = 470;
    private static final int HEIGHT = 252;
    private static final int FILES_VISIBLE = 4;

    private boolean createMode;
    private boolean editMode;
    private @Nullable String editFileName;
    private boolean deleteArmed;
    private LuaScriptEditor editor;
    private ConsoleWidget console;
    private EditBox nameField;
    private boolean controlsBuilt;
    private int fileScrollIndex;
    private boolean rebuildRequested;
    private boolean clearEditorOnCreate;

    public AdvancedHologramScreen(AdvancedHologramMenu menu, Inventory inventory, Component title) {
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
        if (rebuildRequested || (menu.hasClientSync() && !controlsBuilt)) {
            rebuildRequested = false;
            rebuildControls();
        }
        if (console != null && console.isInputPending() && !console.isFocused()) {
            console.setFocused(true);
            setFocused(console);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean textFieldFocused = (nameField != null && nameField.isFocused())
                || (editor != null && editor.isFocused())
                || (console != null && console.isInputPending() && console.isFocused());
        if (textFieldFocused && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void applyServerState(AdvancedHologramMenuStatePayload payload) {
        boolean fileChange = !menu.getAvailableScriptFiles().equals(payload.scriptFiles())
                || !Objects.equals(menu.getSelectedScriptFile(), payload.selectedScriptFile());
        menu.applyClientSync(payload.scriptText(), payload.running(), payload.powered(),
                payload.scriptFiles(), payload.selectedScriptFile());
        if (fileChange) {
            controlsBuilt = false;
        }
        if (editor != null && !editor.isFocused() && !editor.getText().equals(payload.scriptText())) {
            editor.setText(payload.scriptText());
        }
    }

    private void rebuildControls() {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        clearWidgets();

        if (createMode || editMode) {
            nameField = null;
            editor = new LuaScriptEditor(left + 170, top + 20, 292, 176, this.font);
            editor.setText(clearEditorOnCreate ? "" : menu.getScriptText());
            clearEditorOnCreate = false;
            addRenderableWidget(editor);

            if (createMode) {
                nameField = new EditBox(this.font, left + 8, top + 22, 150, 18, Component.empty());
                nameField.setMaxLength(64);
                nameField.setValue("");
                nameField.setFocused(true);
                addRenderableWidget(nameField);

                addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.create_confirm"), button -> createScript())
                        .bounds(left + 170, top + 224, 150, 18)
                        .build());
                addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.create_cancel"), button -> {
                            createMode = false;
                            rebuildRequested = true;
                        })
                        .bounds(left + 326, top + 224, 136, 18)
                        .build());
            } else {
                editor.setFocused(true);
                addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.edit_save"), button -> saveScript())
                        .bounds(left + 170, top + 224, 150, 18)
                        .build());
                addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.edit_cancel"), button -> {
                            editMode = false;
                            editFileName = null;
                            deleteArmed = false;
                            rebuildRequested = true;
                        })
                        .bounds(left + 326, top + 224, 136, 18)
                        .build());
            }
        } else {
            console = new ConsoleWidget(left + 170, top + 20, 292, 176);
            addRenderableWidget(console);

            List<String> files = menu.getAvailableScriptFiles();
            String selected = menu.getSelectedScriptFile();
            int maxScroll = Math.max(0, files.size() - FILES_VISIBLE);
            fileScrollIndex = Math.max(0, Math.min(fileScrollIndex, maxScroll));

            for (int i = 0; i < FILES_VISIBLE; i++) {
                int index = fileScrollIndex + i;
                if (index >= files.size()) {
                    break;
                }
                String name = files.get(index);
                String label = (name.equals(selected) ? "\u00a7a> " : "  ") + truncateLabel(name, 10);
                int fileIndex = index;
                addRenderableWidget(Button.builder(Component.literal(label), button -> selectScriptFile(files.get(fileIndex)))
                        .bounds(left + 8, top + 22 + i * 20, 150, 18)
                        .build());
            }

            addRenderableWidget(Button.builder(Component.literal("^"), button -> {
                        if (fileScrollIndex > 0) {
                            fileScrollIndex--;
                            rebuildRequested = true;
                        }
                    })
                    .bounds(left + 8, top + 106, 73, 18)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("v"), button -> {
                        if (fileScrollIndex + FILES_VISIBLE < menu.getAvailableScriptFiles().size()) {
                            fileScrollIndex++;
                            rebuildRequested = true;
                        }
                    })
                    .bounds(left + 87, top + 106, 73, 18)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.create"), button -> {
                        createMode = true;
                        clearEditorOnCreate = true;
                        rebuildRequested = true;
                    })
                    .bounds(left + 8, top + 128, 150, 18)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.none"), button -> selectScriptFile(null))
                    .bounds(left + 8, top + 150, 150, 18)
                    .build());

            String selectedFile = menu.getSelectedScriptFile();
            boolean hasSelectedFile = selectedFile != null;
            Button editButton = Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.edit"), button -> enterEditMode())
                    .bounds(left + 8, top + 172, 73, 18)
                    .build();
            editButton.active = hasSelectedFile;
            addRenderableWidget(editButton);
            Button deleteButton = Button.builder(
                            Component.translatable(deleteArmed && hasSelectedFile
                                    ? "container.overdrive.advanced_hologram_block.delete_confirm"
                                    : "container.overdrive.advanced_hologram_block.delete"),
                            button -> onDeleteClick())
                    .bounds(left + 87, top + 172, 73, 18)
                    .build();
            deleteButton.active = hasSelectedFile;
            addRenderableWidget(deleteButton);

            addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.start"), button -> startScript())
                    .bounds(left + 170, top + 224, 73, 18)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.stop"), button -> stopScript())
                    .bounds(left + 249, top + 224, 73, 18)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("container.overdrive.advanced_hologram_block.clear"), button -> {
                        AdvancedHologramBlockEntity blockEntity = menu.getBlockEntity();
                        if (blockEntity != null) {
                            blockEntity.clearConsole();
                        }
                    })
                    .bounds(left + 390, top + 224, 72, 18)
                    .build());
        }

        controlsBuilt = true;
    }

    private static String truncateLabel(String name, int maxChars) {
        if (name.length() <= maxChars) {
            return name;
        }
        return name.substring(0, maxChars) + "...";
    }

    private void selectScriptFile(@Nullable String fileName) {
        if (menu.getBlockEntity() != null) {
            PacketDistributor.sendToServer(new AdvancedHologramSelectScriptPayload(menu.getBlockEntity().getBlockPos(), fileName));
        }
    }

    private void createScript() {
        if (menu.getBlockEntity() == null) {
            return;
        }
        String name = nameField.getValue() == null ? "" : nameField.getValue();
        PacketDistributor.sendToServer(new AdvancedHologramCreateScriptPayload(
                menu.getBlockEntity().getBlockPos(), name, editor.getText()));
        createMode = false;
        rebuildRequested = true;
    }

    private void enterEditMode() {
        if (menu.getSelectedScriptFile() == null) {
            return;
        }
        editMode = true;
        editFileName = menu.getSelectedScriptFile();
        deleteArmed = false;
        rebuildRequested = true;
    }

    private void saveScript() {
        if (menu.getBlockEntity() == null || editFileName == null) {
            return;
        }
        PacketDistributor.sendToServer(new AdvancedHologramSaveScriptPayload(
                menu.getBlockEntity().getBlockPos(), editFileName, editor.getText()));
        editMode = false;
        editFileName = null;
        deleteArmed = false;
        rebuildRequested = true;
    }

    private void onDeleteClick() {
        String selectedFile = menu.getSelectedScriptFile();
        if (selectedFile == null || menu.getBlockEntity() == null) {
            deleteArmed = false;
            return;
        }
        if (!deleteArmed) {
            deleteArmed = true;
            rebuildRequested = true;
            return;
        }
        deleteArmed = false;
        PacketDistributor.sendToServer(new AdvancedHologramDeleteScriptPayload(menu.getBlockEntity().getBlockPos(), selectedFile));
    }

    private void startScript() {
        if (menu.getBlockEntity() != null) {
            PacketDistributor.sendToServer(new AdvancedHologramRunPayload(menu.getBlockEntity().getBlockPos(), true));
        }
    }

    private void stopScript() {
        if (menu.getBlockEntity() != null) {
            PacketDistributor.sendToServer(new AdvancedHologramRunPayload(menu.getBlockEntity().getBlockPos(), false));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFD8D8D8);
        guiGraphics.fill(x, y, x + WIDTH, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + HEIGHT, 0xFFFFFFFF);
        guiGraphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFF5A5A5A);
        guiGraphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF5A5A5A);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (createMode) {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.advanced_hologram_block.create_name"), 8, 6, 0x404040, false);
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.advanced_hologram_block.script"), 170, 6, 0x404040, false);
        } else if (editMode) {
            guiGraphics.drawString(this.font, Component.literal(editFileName == null ? "" : truncateLabel(editFileName, 12)), 8, 6, 0x404040, false);
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.advanced_hologram_block.script"), 170, 6, 0x404040, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.advanced_hologram_block.files"), 8, 6, 0x404040, false);
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.advanced_hologram_block.console"), 170, 6, 0x404040, false);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private final class ConsoleWidget extends AbstractWidget {
        private final int lineHeight;
        private int scrollLines;
        private int lastRevision = -1;
        private String inputBuffer = "";
        private int selStartLine = -1;
        private int selStartCol = -1;
        private int selEndLine = -1;
        private int selEndCol = -1;

        private ConsoleWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            this.lineHeight = AdvancedHologramScreen.this.font.lineHeight + 1;
        }

        private List<String> lines() {
            List<String> result = new ArrayList<>();
            AdvancedHologramBlockEntity blockEntity = menu.getBlockEntity();
            boolean running = blockEntity != null ? menu.isRunning() : false;
            boolean powered = blockEntity != null ? menu.isPowered() : false;
            result.add((running ? "\u00a7aRunning" : "\u00a77Stopped")
                    + "   \u00a7" + (powered ? "ePowered" : "8Not powered"));
            if (blockEntity != null) {
                result.addAll(blockEntity.getConsoleLines());
            }
            return result;
        }

        private void clampScroll(int total) {
            int visible = visibleLineCount();
            scrollLines = Math.max(0, Math.min(scrollLines, Math.max(0, total - visible)));
        }

        private int visibleLineCount() {
            return Math.max(1, (height - 6) / lineHeight - (isInputPending() ? 1 : 0));
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (!this.isMouseOver(mouseX, mouseY)) {
                return false;
            }
            if (verticalAmount != 0.0D) {
                scrollLines += verticalAmount > 0 ? -3 : 3;
                clampScroll(lines().size());
            }
            return true;
        }

        private boolean isInputPending() {
            AdvancedHologramBlockEntity blockEntity = menu.getBlockEntity();
            return blockEntity != null && blockEntity.getPendingPrompt() != null;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.isMouseOver(mouseX, mouseY)) {
                if (isInputPending()) {
                    setFocused(true);
                    return true;
                }
                int[] pos = colAtPosition(mouseX, mouseY);
                selStartLine = pos[0];
                selStartCol = pos[1];
                selEndLine = pos[0];
                selEndCol = pos[1];
                return super.mouseClicked(mouseX, mouseY, button);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (button == 0 && !isInputPending() && selStartLine >= 0) {
                int[] pos = colAtPosition(mouseX, mouseY);
                selEndLine = pos[0];
                selEndCol = pos[1];
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        private int[] colAtPosition(double mouseX, double mouseY) {
            List<String> allLines = lines();
            int lineIndex = scrollLines + (int) Math.floor((mouseY - (getY() + 2)) / lineHeight);
            lineIndex = Math.max(0, Math.min(lineIndex, Math.max(0, allLines.size() - 1)));
            int relX = (int) mouseX - (getX() + 3);
            String line = allLines.get(lineIndex);
            int col = 0;
            int cum = 0;
            for (; col < line.length(); col++) {
                int charWidth = AdvancedHologramScreen.this.font.width(String.valueOf(line.charAt(col)));
                if (cum + charWidth / 2.0F > relX) {
                    break;
                }
                cum += charWidth;
            }
            return new int[]{lineIndex, col};
        }

        private int[] selectionRangeInLine(int lineIndex, String line) {
            if (selStartLine < 0 || selEndLine < 0) {
                return null;
            }
            int aLine = Math.min(selStartLine, selEndLine);
            int bLine = Math.max(selStartLine, selEndLine);
            if (lineIndex < aLine || lineIndex > bLine) {
                return null;
            }
            int start;
            int end;
            if (aLine == bLine) {
                start = Math.min(selStartCol, selEndCol);
                end = Math.max(selStartCol, selEndCol);
            } else if (lineIndex == aLine) {
                start = lineIndex == selStartLine ? selStartCol : selEndCol;
                end = line.length();
            } else if (lineIndex == bLine) {
                start = 0;
                end = lineIndex == selEndLine ? selEndCol : selStartCol;
            } else {
                start = 0;
                end = line.length();
            }
            start = Math.max(0, Math.min(start, line.length()));
            end = Math.max(start, Math.min(end, line.length()));
            return start == end ? null : new int[]{start, end};
        }

        private String selectedText() {
            if (selStartLine < 0 || selEndLine < 0) {
                return "";
            }
            List<String> allLines = lines();
            StringBuilder result = new StringBuilder();
            int aLine = Math.min(selStartLine, selEndLine);
            int bLine = Math.max(selStartLine, selEndLine);
            for (int i = aLine; i <= bLine; i++) {
                if (i >= allLines.size()) {
                    break;
                }
                String line = allLines.get(i);
                int start = 0;
                int end = line.length();
                if (i == aLine) {
                    start = i == selStartLine ? selStartCol : selEndCol;
                }
                if (i == bLine) {
                    end = i == selEndLine ? selEndCol : selStartCol;
                }
                start = Math.max(0, Math.min(start, line.length()));
                end = Math.max(start, Math.min(end, line.length()));
                result.append(stripColorCodes(line.substring(start, end)));
                if (i != bLine) {
                    result.append('\n');
                }
            }
            return result.toString();
        }

        private static String stripColorCodes(String input) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c == '\u00a7' && i + 1 < input.length()) {
                    i++;
                    continue;
                }
                result.append(c);
            }
            return result.toString();
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_C && net.minecraft.client.gui.screens.Screen.hasControlDown() && selStartLine >= 0) {
                String selected = selectedText();
                if (!selected.isEmpty()) {
                    AdvancedHologramScreen.this.minecraft.keyboardHandler.setClipboard(selected);
                }
                return true;
            }
            if (!isInputPending()) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !inputBuffer.isEmpty()) {
                inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                String input = inputBuffer;
                inputBuffer = "";
                AdvancedHologramBlockEntity blockEntity = menu.getBlockEntity();
                if (blockEntity != null) {
                    blockEntity.submitScriptInput(input);
                    PacketDistributor.sendToServer(new AdvancedHologramConsoleInputPayload(blockEntity.getBlockPos(), input));
                }
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            if (isInputPending() && !Character.isISOControl(codePoint) && inputBuffer.length() < 255) {
                inputBuffer += codePoint;
                return true;
            }
            return super.charTyped(codePoint, modifiers);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF10141C);
            graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, 0xFF1B222D);
            int borderColor = isHoveredOrFocused() ? 0xFF66D9FF : 0xFF3A4854;
            graphics.fill(getX(), getY(), getX() + width, getY() + 1, borderColor);
            graphics.fill(getX(), getY(), getX() + 1, getY() + height, borderColor);
            graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, borderColor);
            graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, borderColor);

            AdvancedHologramBlockEntity blockEntity = menu.getBlockEntity();
            int revision = blockEntity != null ? blockEntity.getConsoleRevision() : 0;
            if (revision != lastRevision) {
                lastRevision = revision;
                int visible = visibleLineCount();
                int total = lines().size();
                scrollLines = Math.max(0, total - visible);
            }

            List<String> allLines = lines();
            int visibleLines = visibleLineCount();
            clampScroll(allLines.size());

            int startY = getY() + 2;
            graphics.enableScissor(getX() + 2, startY, getX() + width - 2, getY() + height - 2);
            for (int i = scrollLines; i < allLines.size(); i++) {
                int lineY = startY + (i - scrollLines) * lineHeight;
                if (lineY + AdvancedHologramScreen.this.font.lineHeight > getY() + height - 2) {
                    break;
                }
                String line = allLines.get(i);
                int[] selection = selectionRangeInLine(i, line);
                if (selection != null) {
                    int selStartX = getX() + 3 + AdvancedHologramScreen.this.font.width(line.substring(0, selection[0]));
                    int selWidth = AdvancedHologramScreen.this.font.width(line.substring(selection[0], selection[1]));
                    graphics.fill(selStartX, lineY, selStartX + selWidth, lineY + AdvancedHologramScreen.this.font.lineHeight, 0xFF2E6DB4);
                }
                graphics.drawString(AdvancedHologramScreen.this.font, Component.literal(line), getX() + 3, lineY, 0xFFFFFFFF, false);
            }

            if (isInputPending() && blockEntity != null) {
                String prompt = blockEntity.getPendingPrompt();
                boolean showCursor = (System.currentTimeMillis() / 500) % 2 == 0;
                String fullText = prompt + inputBuffer + (showCursor ? "\u2588" : "");
                graphics.drawString(AdvancedHologramScreen.this.font, Component.literal(fullText), getX() + 3,
                        getY() + height - lineHeight - 2, isFocused() ? 0xFF66D9FF : 0xFFD0D0D0, false);
            }
            graphics.disableScissor();
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }

    private final class LuaScriptEditor extends AbstractWidget {
        private static final int TEXT_COLOR = 0xFFD7E3F0;
        private static final int BORDER_COLOR = 0xFF66D9FF;
        private static final int MAX_LENGTH = 32767;

        private final Font font;
        private final int lineHeight;

        private String text = "";
        private int cursorIndex;
        private int scrollLines;
        private int scrollChars;

        private LuaScriptEditor(int x, int y, int width, int height, Font font) {
            super(x, y, width, height, Component.empty());
            this.font = font;
            this.lineHeight = font.lineHeight + 1;
        }

        public void setText(String newText) {
            this.text = newText == null ? "" : newText;
            this.cursorIndex = Math.min(this.cursorIndex, this.text.length());
            clampScroll();
        }

        public String getText() {
            return text;
        }

        private String[] lines() {
            return text.split("\n", -1);
        }

        private int[] cursorLineCol(String[] lines) {
            int line = 0;
            int col = 0;
            for (int i = 0; i < cursorIndex; i++) {
                if (text.charAt(i) == '\n') {
                    line++;
                    col = 0;
                } else {
                    col++;
                }
            }
            return new int[]{Math.min(line, Math.max(0, lines.length - 1)), col};
        }

        private int lineStartIndex(int lineIndex, String[] lines) {
            int idx = 0;
            for (int i = 0; i < lineIndex && i < lines.length; i++) {
                idx += lines[i].length() + 1;
            }
            return idx;
        }

        private void insertAtCursor(String insertion) {
            if (insertion == null || insertion.isEmpty()) {
                return;
            }
            int remaining = MAX_LENGTH - text.length();
            if (remaining <= 0) {
                return;
            }
            if (insertion.length() > remaining) {
                insertion = insertion.substring(0, remaining);
            }
            text = text.substring(0, cursorIndex) + insertion + text.substring(cursorIndex);
            cursorIndex += insertion.length();
        }

        private void deleteAtCursor() {
            if (cursorIndex >= text.length()) {
                return;
            }
            text = text.substring(0, cursorIndex) + text.substring(cursorIndex + 1);
        }

        private void backspace() {
            if (cursorIndex <= 0) {
                return;
            }
            text = text.substring(0, cursorIndex - 1) + text.substring(cursorIndex);
            cursorIndex--;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !this.isMouseOver(mouseX, mouseY)) {
                return false;
            }
            this.setFocused(true);
            String[] lines = lines();
            int lineIndex = scrollLines + (int) Math.floor((mouseY - (getY() + 2)) / lineHeight);
            lineIndex = Math.max(0, Math.min(lineIndex, lines.length - 1));
            String line = lines[lineIndex];
            int start = Math.min(scrollChars, line.length());
            int relX = (int) mouseX - (getX() + 3 - font.width(line.substring(0, start)));
            int col = 0;
            int cum = 0;
            for (; col < line.length(); col++) {
                int charWidth = font.width(String.valueOf(line.charAt(col)));
                if (cum + charWidth / 2.0F > relX) {
                    break;
                }
                cum += charWidth;
            }
            cursorIndex = lineStartIndex(lineIndex, lines) + Math.max(start, Math.min(col, line.length()));
            clampScroll();
            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
                moveCursorHorizontal(keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
                moveCursorVertical(keyCode == GLFW.GLFW_KEY_UP ? -1 : 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_HOME) {
                moveCursorHome();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_END) {
                moveCursorEnd();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                backspace();
                clampScroll();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                deleteAtCursor();
                clampScroll();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                insertAtCursor("\n");
                clampScroll();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_C && net.minecraft.client.gui.screens.Screen.hasControlDown()) {
                copyCurrentLine();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_V && net.minecraft.client.gui.screens.Screen.hasControlDown()) {
                insertAtCursor(AdvancedHologramScreen.this.minecraft.keyboardHandler.getClipboard());
                clampScroll();
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            if (!Character.isISOControl(codePoint)) {
                insertAtCursor(String.valueOf(codePoint));
                clampScroll();
                return true;
            }
            return super.charTyped(codePoint, modifiers);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (!this.isMouseOver(mouseX, mouseY)) {
                return false;
            }
            scrollLines += verticalAmount > 0 ? -1 : (verticalAmount < 0 ? 1 : 0);
            clampScroll();
            return true;
        }

        private void moveCursorHorizontal(int dir) {
            if (dir < 0) {
                cursorIndex = Math.max(0, cursorIndex - 1);
            } else {
                cursorIndex = Math.min(text.length(), cursorIndex + 1);
            }
            clampScroll();
        }

        private void moveCursorVertical(int dir) {
            String[] lines = lines();
            int[] lc = cursorLineCol(lines);
            int target = lc[0] + dir;
            if (target < 0 || target >= lines.length) {
                return;
            }
            cursorIndex = lineStartIndex(target, lines) + Math.min(lc[1], lines[target].length());
            clampScroll();
        }

        private void moveCursorHome() {
            String[] lines = lines();
            cursorIndex = lineStartIndex(cursorLineCol(lines)[0], lines);
            clampScroll();
        }

        private void moveCursorEnd() {
            String[] lines = lines();
            int line = cursorLineCol(lines)[0];
            cursorIndex = lineStartIndex(line, lines) + lines[line].length();
            clampScroll();
        }

        private void copyCurrentLine() {
            String[] lines = lines();
            int line = cursorLineCol(lines)[0];
            AdvancedHologramScreen.this.minecraft.keyboardHandler.setClipboard(lines[line]);
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        private void clampScroll() {
            String[] lines = lines();
            int visible = Math.max(1, (height - 4) / lineHeight);
            int maxScroll = Math.max(0, lines.length - visible);
            scrollLines = Math.max(0, Math.min(scrollLines, maxScroll));

            int[] lc = cursorLineCol(lines);
            if (lc[0] < scrollLines) {
                scrollLines = lc[0];
            }
            if (lc[0] > scrollLines + visible - 1) {
                scrollLines = lc[0] - visible + 1;
            }

            String line = lines[lc[0]];
            int col = Math.min(lc[1], line.length());
            int editorWidth = Math.max(1, width - 6);
            if (scrollChars > col) {
                scrollChars = Math.max(0, col);
            }
            while (scrollChars < col
                    && font.width(line.substring(scrollChars, col)) > editorWidth) {
                scrollChars++;
            }
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF10141C);
            graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, 0xFF1B222D);

            int borderColor = isHoveredOrFocused() ? BORDER_COLOR : 0xFF3A4854;
            graphics.fill(getX(), getY(), getX() + width, getY() + 1, borderColor);
            graphics.fill(getX(), getY(), getX() + 1, getY() + height, borderColor);
            graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, borderColor);
            graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, borderColor);

            int editorLeft = getX() + 3;
            int editorTop = getY() + 2;
            int editorWidth = width - 6;
            int editorHeight = height - 4;

            clampScroll();
            String[] lines = lines();
            int[] lc = cursorLineCol(lines);

            graphics.enableScissor(editorLeft, editorTop, editorLeft + editorWidth, editorTop + editorHeight);
            for (int i = scrollLines; i < lines.length; i++) {
                int yPos = editorTop + (i - scrollLines) * lineHeight;
                if (yPos >= editorTop + editorHeight) {
                    break;
                }
                String line = lines[i];
                int start = Math.min(scrollChars, line.length());
                graphics.drawString(font, line.substring(start), editorLeft, yPos, TEXT_COLOR, false);
            }

            if (isFocused()) {
                String line = lines[lc[0]];
                int col = Math.min(lc[1], line.length());
                int start = Math.min(scrollChars, line.length());
                int xPos = editorLeft + font.width(line.substring(Math.min(start, col), Math.max(start, col)));
                int yPos = editorTop + (lc[0] - scrollLines) * lineHeight;
                graphics.fill(xPos, yPos, xPos + 1, yPos + font.lineHeight, 0xFFD7E3F0);
            }
            graphics.disableScissor();
        }
    }
}
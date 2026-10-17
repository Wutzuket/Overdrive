package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Basis fuer alle JEI-Kategorien der Fluid-Maschinen (Beschleuniger, AD, ELENA,
 * Reaktorkuehlung, Kuehlturm, Annihilationsreaktor).
 *
 * Layout: oben ein helles Panel mit den Kennzahlen des Rezepts, darunter eine
 * zentrierte Reihe der Fluid-Slots mit Menge. Das Panel ist hell mit dunklem
 * Text, weil der JEI-Hintergrund je nach Ressourcenpaket hell oder dunkel sein kann.
 */
public abstract class FluidRecipeCategory<T extends Recipe<RecipeInput>> implements IRecipeCategory<T> {

    public static final int WIDTH = 200;
    public static final int HEIGHT = 134;

    public static final int SLOT_SIZE = 18;
    public static final int SLOT_Y = 90;
    private static final int SLOT_GAP = 8;
    protected static final int IN_OUT_GAP = 26;
    private static final int LINE_HEIGHT = 10;

    /** X-Position einer einzelnen, mittig platzierten Fluid-Ausgabe. */
    protected static int centeredOutputX() {
        return (WIDTH - SLOT_SIZE) / 2;
    }

    protected static final int PANEL_X = 4;
    protected static final int PANEL_Y = 4;
    protected static final int PANEL_W = WIDTH - 8;
protected static final int PANEL_MIN_H = 44;
protected static final int PANEL_PAD_TOP = 8;
protected static final int PANEL_PAD_BOTTOM = 10;

    protected static final int COL_PANEL_BORDER = 0xFF6A6A72;
    protected static final int COL_PANEL = 0xFFF2F2F5;
    protected static final int COL_LABEL = 0xFF3C3C42;
    protected static final int COL_VALUE = 0xFF1E1E24;
    protected static final int COL_CYAN = 0xFF00506A;
    protected static final int COL_GREEN = 0xFF14501F;

    /** Eine Zeile im Kennzahlen-Panel. */
    public record InfoLine(Component text, int color) {
        public static InfoLine label(Component text) {
            return new InfoLine(text, COL_LABEL);
        }

        public static InfoLine value(Component text) {
            return new InfoLine(text, COL_VALUE);
        }

        public static InfoLine energy(Component text) {
            return new InfoLine(text, COL_CYAN);
        }
    }

    /** Fluid-Seiten eines Rezepts; leere Stapel werden nicht angezeigt. */
    public record FluidIO(List<FluidStack> inputs, @Nullable FluidStack output) {
        public FluidIO {
            inputs = List.copyOf(inputs);
        }

        public static FluidIO of(FluidStack input, @Nullable FluidStack output) {
            return new FluidIO(List.of(input), output);
        }
    }

    private record PlacedSlot(int x, FluidStack fluid, boolean input, int color) {
    }

    /** Eine geumbrochene Textzeile im Kennzahlen-Panel. */
    private record DrawLine(FormattedCharSequence text, int color) {
    }

    private final RecipeType<T> type;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;

    protected FluidRecipeCategory(IGuiHelper guiHelper, RecipeType<T> type, String titleKey, ItemLike iconItem) {
        this.type = type;
        this.title = Component.translatable(titleKey);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(iconItem);
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    @SuppressWarnings("removal")
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

    /** Fluid-Seiten dieses Rezepts. */
    protected abstract FluidIO getFluids(T recipe);

    /** Kennzahlen-Zeilen fuer das Panel. */
    protected abstract List<InfoLine> getInfoLines(T recipe);

    /** Bloecke, die als Katalysator dienen (rein fuer die Rezept-Suche in JEI). */
    protected ItemLike[] getCatalysts() {
        return new ItemLike[0];
    }

    @Override
    public final void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull T recipe, @NotNull IFocusGroup focuses) {
        FluidIO io = getFluids(recipe);
        for (PlacedSlot slot : layout(io)) {
            builder.addSlot(slot.input() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, slot.x(), SLOT_Y)
                    .addFluidStack(slot.fluid().getFluid(), slot.fluid().getAmount(), DataComponentPatch.EMPTY);
        }
        addExtraSlots(builder, recipe);
        for (ItemLike catalyst : getCatalysts()) {
            builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemLike(catalyst);
        }
    }

    /** Zusaetzliche Item-Slots (z.B. ein Mesh), die nicht ueber Fluids laufen. */
    protected void addExtraSlots(@NotNull IRecipeLayoutBuilder builder, @NotNull T recipe) {
    }

    /** X des linken Item-Eingangs fuer das Input-Label, -1 = keiner. */
    protected int extraInputSlotX() {
        return -1;
    }

    @Override
    public void draw(@NotNull T recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // Zeilen umbrechen, damit lange Texte nicht aus dem Panel laufen.
        // Achtung: FormattedCharSequence.toString() liefert den Lambda-Namen,
        // deshalb die Segmente unveraendert weitergeben.
        int maxWidth = PANEL_W - 16;
        List<DrawLine> lines = new ArrayList<>();
        for (InfoLine info : getInfoLines(recipe)) {
            for (FormattedCharSequence part : font.split(info.text(), maxWidth)) {
                lines.add(new DrawLine(part, info.color()));
            }
        }

        // Panel sitzt direkt um den Text, mit deutlichem Abstand unter der letzten Zeile.
        int panelH = Math.max(PANEL_MIN_H, PANEL_PAD_TOP + lines.size() * LINE_HEIGHT + PANEL_PAD_BOTTOM);
        drawPanel(graphics, PANEL_X, PANEL_Y, PANEL_W, panelH);
        int y = PANEL_Y + PANEL_PAD_TOP;
        for (DrawLine line : lines) {
            graphics.drawString(font, line.text(), PANEL_X + 8, y, line.color(), false);
            y += LINE_HEIGHT;
        }

        FluidIO io = getFluids(recipe);
        List<PlacedSlot> slots = layout(io);
        int firstInput = -1;
        int outputX = -1;
        for (PlacedSlot slot : slots) {
            if (slot.input()) {
                if (firstInput < 0) {
                    firstInput = slot.x();
                }
            } else {
                outputX = slot.x();
            }
        }
        if (firstInput < 0) {
            firstInput = extraInputSlotX();
        }
        if (firstInput >= 0) {
            graphics.drawString(font, Component.translatable("jei.overdrive.fluid.input"), firstInput, SLOT_Y - 11,
                    COL_LABEL, false);
        }
        if (outputX >= 0) {
            graphics.drawString(font, Component.translatable("jei.overdrive.fluid.output"), outputX, SLOT_Y - 11,
                    COL_LABEL, false);
        }
        for (PlacedSlot slot : slots) {
            graphics.drawString(font, Component.literal(ODFormat.formatFluid(slot.fluid().getAmount())),
                    slot.x(), SLOT_Y + SLOT_SIZE + 3, slot.color(), false);
        }
    }

    /** Zentriert die Fluid-Slots: Eingaben links, Ausgabe mit grossem Abstand rechts. */
    private static List<PlacedSlot> layout(FluidIO io) {
        List<FluidStack> inputs = new ArrayList<>();
        for (FluidStack stack : io.inputs()) {
            if (stack != null && !stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        FluidStack output = io.output() != null && !io.output().isEmpty() ? io.output() : null;

        int count = inputs.size() + (output != null ? 1 : 0);
        if (count == 0) {
            return List.of();
        }

        // Slot-Abstand muss zur breitesten Mengenangabe passen, sonst laufen die
        // Texte unter den Slots ineinander (z.B. zwei 10-mB-Eingaben im Reaktor).
        Font font = Minecraft.getInstance().font;
        int pitch = SLOT_SIZE + SLOT_GAP;
        for (FluidStack stack : inputs) {
            pitch = Math.max(pitch, font.width(ODFormat.formatFluid(stack.getAmount())) + 6);
        }
        if (output != null) {
            pitch = Math.max(pitch, font.width(ODFormat.formatFluid(output.getAmount())) + 6);
        }

        int lastInput = inputs.isEmpty() ? 0 : (inputs.size() - 1) * pitch;
        int outGap = Math.max(IN_OUT_GAP, pitch);
        int rowWidth = lastInput + SLOT_SIZE + (output != null ? outGap : 0);
        int startX = (WIDTH - rowWidth) / 2;

        List<PlacedSlot> placed = new ArrayList<>(count);
        for (int i = 0; i < inputs.size(); i++) {
            placed.add(new PlacedSlot(startX + i * pitch, inputs.get(i), true, COL_CYAN));
        }
        if (output != null) {
            int outX = inputs.isEmpty() ? (WIDTH - SLOT_SIZE) / 2 : startX + lastInput + outGap;
            placed.add(new PlacedSlot(outX, output, false, COL_GREEN));
        }
        return placed;
    }

    /** Zeile "Wert" mit neutraler Farbe. */
    protected static InfoLine value(String key, Object... args) {
        return InfoLine.value(Component.translatable(key, args));
    }

    /** Zeile fuer Energie-/Kostenangaben. */
    protected static InfoLine energy(String key, Object... args) {
        return InfoLine.energy(Component.translatable(key, args));
    }

    protected static InfoLine line(Component text, int color) {
        return new InfoLine(text, color);
    }

    /** Helle Box mit dunklem Rand als sichere Leseflaeche fuer die Textzeilen. */
    protected static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COL_PANEL_BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, COL_PANEL);
    }
}
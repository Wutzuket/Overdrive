package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.CryoDistillerRecipe;
import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI-Widget des Cryo Distillers: 3D-Render der Maschine ueber dem vom Rezept
 * geforderten Katalysator-Block plus Text zu Dauer, Strom und Fluiden.
 *
 * Damit die Schrift unabhaengig vom JEI-Hintergrund (Standard hellgrau, Resource Packs
 * koennen ihn aendern) lesbar bleibt, liegen die Textzeilen auf hellen Panels
 * und werden dunkel und ohne Schatten gezeichnet.
 */
@SuppressWarnings("removal")
public class CryoDistillerWidget implements IRecipeWidget, IJeiGuiEventListener {
    public static final int WIDTH = CryoDistillerCategory.WIDTH;
    public static final int HEIGHT = CryoDistillerCategory.HEIGHT;

    public static final int INPUT_SLOT_X = 96;
    public static final int INPUT_SLOT_Y = 70;
    public static final int OUTPUT_SLOT_X = 146;
    public static final int OUTPUT_SLOT_Y = 70;

    private static final float STRUCTURE_CENTER_X = 50.0F;
    private static final float STRUCTURE_CENTER_Y = 58.0F;
    private static final float STRUCTURE_SCALE = 24.0F;
    /** Katalysator sitzt bei y=-1, Maschine bei y=0 -> Blockmitten bei -0.5 und +0.5. */
    private static final float CATALYST_BLOCK_Y = -1.0F;

    private static final int PANEL_X = 88;
    private static final int PANEL_Y = 4;
    private static final int PANEL_W = 108;
    private static final int PANEL_H = 100;
    private static final int CAPTION_X = 4;
    private static final int CAPTION_Y = 108;
    private static final int CAPTION_W = 192;
    private static final int CAPTION_H = 38;

    private static final int COL_PANEL_BORDER = 0xFF6A6A72;
    private static final int COL_PANEL = 0xFFF2F2F5;
    private static final int COL_LABEL = 0xFF3C3C42;
    private static final int COL_VALUE = 0xFF1E1E24;
    private static final int COL_CYAN = 0xFF00506A;
    private static final int COL_GREEN = 0xFF14501F;
    private static final int COL_RED = 0xFF7A1616;
    private static final int COL_RULE = 0xFFA8A8B0;

    private final CryoDistillerRecipe recipe;
    private final List<jei_block_render_helper.RenderBlock> blocks;
    /** null = kein Katalysator noetig, Blocks.AIR = ungueltige Block-ID */
    private final @Nullable Block catalystBlock;

    public CryoDistillerWidget(CryoDistillerRecipe recipe) {
        this.recipe = recipe;
        this.catalystBlock = recipe.catalystBlockId() == null
                ? null
                : BuiltInRegistries.BLOCK.get(recipe.catalystBlockId());
        List<jei_block_render_helper.RenderBlock> list = new ArrayList<>();
        if (catalystBlock != null && catalystBlock != Blocks.AIR) {
            list.add(new jei_block_render_helper.RenderBlock(catalystBlock.defaultBlockState(), 0.0F, CATALYST_BLOCK_Y, 0.0F));
            list.add(new jei_block_render_helper.RenderBlock(ODBlocks.CRYO_DISTILLER.get().defaultBlockState(), 0.0F, CATALYST_BLOCK_Y + 1.0F, 0.0F));
        } else {
            list.add(new jei_block_render_helper.RenderBlock(ODBlocks.CRYO_DISTILLER.get().defaultBlockState(), 0.0F, 0.0F, 0.0F));
        }
        this.blocks = List.copyOf(list);
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
    public void drawWidget(GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        drawPanel(g, PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
        drawPanel(g, CAPTION_X, CAPTION_Y, CAPTION_W, CAPTION_H);

        // ---- linke Spalte: 3D-Struktur ----
        draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.structure"), 8, 8, COL_LABEL);
        jei_block_render_helper.renderBlocks(g, blocks, STRUCTURE_CENTER_X, STRUCTURE_CENTER_Y, STRUCTURE_SCALE);

        // ---- untere Leiste: Aufbau + Katalysator-Hinweis ----
        draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.layout"), CAPTION_X + 4, CAPTION_Y + 4, COL_LABEL);
        if (catalystBlock == null) {
            draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.no_catalyst"), CAPTION_X + 4, CAPTION_Y + 16, COL_LABEL);
        } else if (catalystBlock == Blocks.AIR) {
            draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.catalyst_invalid",
                    String.valueOf(recipe.catalystBlockId())), CAPTION_X + 4, CAPTION_Y + 16, COL_RED);
        } else {
            draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.catalyst_below",
                    catalystBlock.getName().getString()), CAPTION_X + 4, CAPTION_Y + 16, COL_LABEL);
        }

        // ---- rechte Spalte: Kennzahlen ----
        int tx = PANEL_X + 8;
        draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.time_label"), tx, PANEL_Y + 6, COL_LABEL);
        draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.time",
                recipe.processTime(), String.format(java.util.Locale.ROOT, "%.1f", recipe.processTime() / 20.0)),
                tx, PANEL_Y + 16, COL_VALUE);
        draw(g, font, Component.literal(ODFormat.formatEnergyPerTick(recipe.energyPerTick())), tx, PANEL_Y + 30, COL_CYAN);
        draw(g, font, Component.literal(ODFormat.formatEnergy(recipe.totalEnergy())), tx, PANEL_Y + 40, COL_VALUE);

        g.fill(tx, PANEL_Y + 50, PANEL_X + PANEL_W - 8, PANEL_Y + 51, COL_RULE);

        // ---- Fluid-Slots + Mengen ----
        draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.input"), INPUT_SLOT_X, INPUT_SLOT_Y - 10, COL_LABEL);
        draw(g, font, Component.translatable("jei.overdrive.cryo_distiller.output"), OUTPUT_SLOT_X, OUTPUT_SLOT_Y - 10, COL_LABEL);

        draw(g, font, Component.literal(ODFormat.formatFluid(recipe.inputFluid().getAmount())), INPUT_SLOT_X, INPUT_SLOT_Y + 22, COL_CYAN);
        draw(g, font, Component.literal(ODFormat.formatFluid(recipe.outputFluid().getAmount())), OUTPUT_SLOT_X, OUTPUT_SLOT_Y + 22, COL_GREEN);

        // ---- Produktionsrate des Rezepts (z.B. 5 mB/t) ----
        draw(g, font, Component.translatable("jei.overdrive.fluid.production",
                        ODFormat.formatFluidPerTick(recipe.outputFluid().getAmount() / (double) Math.max(1, recipe.processTime()))),
                CAPTION_X + 4, CAPTION_Y + 28, COL_CYAN);
    }

    /** Helle Box mit dunklem Rand als sichere Leseflaeche fuer die Textzeilen. */
    private static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COL_PANEL_BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, COL_PANEL);
    }

    /** Ohne Schatten - sonst verschwimmt die dünne Minecraft-Pixelfont auf hellem Grund. */
    private static void draw(GuiGraphics g, Font font, Component text, int x, int y, int color) {
        g.drawString(font, text, x, y, color, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        ItemStack hovered = getHoveredBlock(mouseX, mouseY);
        if (hovered != null && !hovered.isEmpty()) {
            tooltip.addAll(Screen.getTooltipFromItem(Minecraft.getInstance(), hovered));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ItemStack hovered = getHoveredBlock(mouseX, mouseY);
        if (hovered == null || hovered.isEmpty()) return false;
        IJeiRuntime runtime = OverdriveJeiPlugin.getRuntime();
        if (runtime != null) {
            var focus = runtime.getJeiHelpers().getFocusFactory()
                    .createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, hovered);
            runtime.getRecipesGui().show(focus);
        }
        return true;
    }

    private @Nullable ItemStack getHoveredBlock(double mouseX, double mouseY) {
        float hitRadius = STRUCTURE_SCALE * 0.4F;
        boolean hasCatalyst = catalystBlock != null && catalystBlock != Blocks.AIR;

        if (hasCatalyst) {
            float[] catalyst = jei_block_render_helper.projectVertical(CATALYST_BLOCK_Y + 0.5F,
                    STRUCTURE_CENTER_X, STRUCTURE_CENTER_Y, STRUCTURE_SCALE);
            if (dist(mouseX, mouseY, catalyst[0], catalyst[1]) < hitRadius * hitRadius) {
                return new ItemStack(catalystBlock.asItem());
            }
        }

        float machineLocalY = hasCatalyst ? CATALYST_BLOCK_Y + 1.5F : 0.5F;
        float[] machine = jei_block_render_helper.projectVertical(machineLocalY,
                STRUCTURE_CENTER_X, STRUCTURE_CENTER_Y, STRUCTURE_SCALE);
        if (dist(mouseX, mouseY, machine[0], machine[1]) < hitRadius * hitRadius) {
            return new ItemStack(ODBlocks.CRYO_DISTILLER.get().asItem());
        }
        return null;
    }

    private static double dist(double mx, double my, float sx, float sy) {
        double dx = mx - sx;
        double dy = my - sy;
        return dx * dx + dy * dy;
    }
}
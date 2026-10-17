package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.distiller.CryoDistillerEntity;
import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.network.ToggleHologramPayload;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CryoDistillerScreen extends AbstractContainerScreen<CryoDistillerMenu> {
    private static final int WIDTH = 208;
    private static final int HEIGHT = 168;

    private static final int BAR_W = 14;
    private static final int BAR_H = 44;
    private static final int BAR_Y = 16;
    private static final int ENERGY_X = 8;
    private static final int INPUT_X = 26;
    private static final int OUTPUT_X = 186;
    private static final int SCREEN_X = 46;
    private static final int SCREEN_Y = 16;
    private static final int SCREEN_W = 136;
    private static final int SCREEN_H = 56;
    private static final int PROGRESS_Y = 78;

    private ButtonHelper hologramBtn;
    private ButtonHelper tabStatus, tabInfo;
    private int currentTab = 0;

    public CryoDistillerScreen(CryoDistillerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.titleLabelY = 4;
        this.inventoryLabelY = HEIGHT - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;
        hologramBtn = new ButtonHelper(x + WIDTH - 20, y + 4, 16, 16, Component.literal("H"), b -> {
            CryoDistillerEntity be = menu.getBlockEntity();
            if (be != null) PacketDistributor.sendToServer(new ToggleHologramPayload(be.getBlockPos()));
        }, GuiStyle.COL_CYAN);
        addRenderableWidget(hologramBtn);
        tabStatus = new ButtonHelper(x + 6, y + HEIGHT - 20, 46, 14, Component.literal("Status"), b -> {
            currentTab = 0;
            updateTabs();
        }, GuiStyle.COL_CYAN);
        tabInfo = new ButtonHelper(x + 54, y + HEIGHT - 20, 46, 14, Component.literal("Info"), b -> {
            currentTab = 1;
            updateTabs();
        }, GuiStyle.COL_CYAN);
        addRenderableWidget(tabStatus);
        addRenderableWidget(tabInfo);
        updateTabs();
    }

    private void updateTabs() {
        if (tabStatus != null) tabStatus.setMessage(Component.literal((currentTab == 0 ? "[" : "") + "Status" + (currentTab == 0 ? "]" : "")));
        if (tabInfo != null) tabInfo.setMessage(Component.literal((currentTab == 1 ? "[" : "") + "Info" + (currentTab == 1 ? "]" : "")));
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        GuiStyle.panel(g, x, y, WIDTH, HEIGHT);

        CryoDistillerEntity be = menu.getBlockEntity();
        if (be != null) {
            double energyPct = be.getEnergyStored() / (double) be.getMaxEnergyStored();
            GuiStyle.metricGauge(g, font, x + ENERGY_X, y + BAR_Y, BAR_W, BAR_H, (float) energyPct, GuiStyle.COL_GREEN, null);
            GuiStyle.fluidGauge(g, font, be.getInputTank().getFluid(), CryoDistillerEntity.TANK_CAPACITY,
                    x + INPUT_X, y + BAR_Y, BAR_W, BAR_H, null);
            GuiStyle.fluidGauge(g, font, be.getOutputTank().getFluid(), CryoDistillerEntity.TANK_CAPACITY,
                    x + OUTPUT_X, y + BAR_Y, BAR_W, BAR_H, null);
        } else {
            GuiStyle.metricGauge(g, font, x + ENERGY_X, y + BAR_Y, BAR_W, BAR_H, 0, GuiStyle.COL_GREEN, null);
            GuiStyle.inset(g, x + INPUT_X, y + BAR_Y, BAR_W, BAR_H);
            GuiStyle.inset(g, x + OUTPUT_X, y + BAR_Y, BAR_W, BAR_H);
        }

        GuiStyle.screenArea(g, x + SCREEN_X, y + SCREEN_Y, SCREEN_W, SCREEN_H);

        double prog = be != null ? Math.min(1.0, be.getProgress() / (double) be.getMaxProgress()) : 0.0;
        int progressColor = be != null && be.needsCatalyst() && !be.isCatalystPresent() ? GuiStyle.COL_RED : GuiStyle.COL_CYAN;
        GuiStyle.hBar(g, x + 6, y + PROGRESS_Y, WIDTH - 12, 8, prog, progressColor);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, title, 8, 4, GuiStyle.COL_TEXT, false);

        CryoDistillerEntity be = menu.getBlockEntity();
        if (be == null) return;

        int sx = SCREEN_X + 4;
        int sy = SCREEN_Y + 4;

        boolean catalystMissing = be.needsCatalyst() && !be.isCatalystPresent();
        String status;
        if (catalystMissing) {
            status = "§cCatalyst missing";
        } else if (be.isActive()) {
            status = "§aRunning";
        } else {
            status = "§eIdle";
        }
        g.drawString(font, "Status: " + status, sx, sy, GuiStyle.COL_TEXT, false);

        ItemStack catalyst = be.getCatalystStack();
        String catalystLine;
        if (!be.needsCatalyst()) {
            catalystLine = "Catalyst: none required";
        } else if (catalyst.isEmpty()) {
            catalystLine = "§cCatalyst: unknown block";
        } else {
            catalystLine = (be.isCatalystPresent() ? "§aCatalyst: " : "§cCatalyst: ") + catalyst.getHoverName().getString();
        }
        g.drawString(font, catalystLine, sx, sy + 10, GuiStyle.COL_TEXT_DIM, false);

        g.drawString(font, "Progress: " + be.getProgress() + " / " + be.getMaxProgress() + " t",
                sx, sy + 20, GuiStyle.COL_TEXT_DIM, false);
        g.drawString(font, "Rate: " + ODFormat.formatEnergyPerTick(be.getEnergyPerTick()),
                sx, sy + 30, be.getEnergyPerTick() > 0 ? GuiStyle.COL_CYAN : GuiStyle.COL_TEXT_DIM, false);
        g.drawString(font, "Time: " + be.getMaxProgress() + " t (" + String.format(java.util.Locale.ROOT, "%.1f", be.getMaxProgress() / 20.0) + " s)",
                sx, sy + 40, GuiStyle.COL_TEXT_DIM, false);

        if (currentTab == 0) {
            FluidStack in = be.getInputTank().getFluid();
            g.drawString(font, "In: " + (in.isEmpty() ? "Empty" : in.getHoverName().getString()),
                    8, 90, GuiStyle.COL_CYAN, false);
            g.drawString(font, "     " + ODFormat.formatFluidPair(be.getInputTank().getFluidAmount(), CryoDistillerEntity.TANK_CAPACITY),
                    8, 100, GuiStyle.COL_TEXT, false);

            FluidStack out = be.getOutputTank().getFluid();
            g.drawString(font, "Out: " + (out.isEmpty() ? "Empty" : out.getHoverName().getString()),
                    108, 90, be.getOutputTank().getFluidAmount() > 0 ? GuiStyle.COL_GREEN : GuiStyle.COL_TEXT_DIM, false);
            g.drawString(font, "      " + ODFormat.formatFluidPair(be.getOutputTank().getFluidAmount(), CryoDistillerEntity.TANK_CAPACITY),
                    108, 100, GuiStyle.COL_TEXT_DIM, false);

            g.drawString(font, "Energy: " + ODFormat.formatEnergyPair(be.getEnergyStored(), be.getMaxEnergyStored()),
                    8, 114, GuiStyle.COL_GREEN, false);
        } else {
            int iy = 90;
            g.drawString(font, "Input: " + ODFormat.formatFluid(be.getInputTank().getFluidAmount()),
                    8, iy, GuiStyle.COL_TEXT_DIM, false);
            g.drawString(font, "Output: " + ODFormat.formatFluid(be.getOutputTank().getFluidAmount()),
                    8, iy + 9, GuiStyle.COL_TEXT_DIM, false);
            g.drawString(font, "Total/op: " + ODFormat.formatEnergy((long) be.getEnergyPerTick() * be.getMaxProgress()),
                    8, iy + 18, GuiStyle.COL_TEXT_DIM, false);
            g.drawString(font, "Tank cap: " + ODFormat.formatFluid(CryoDistillerEntity.TANK_CAPACITY) + " each",
                    8, iy + 27, GuiStyle.COL_TEXT_DIM, false);
            g.drawString(font, "Throughput: " + ODFormat.formatFluidPerTick(
                            be.getInputTank().getFluidAmount() > 0
                                    ? be.getInputTank().getFluidAmount() / (double) Math.max(1, be.getMaxProgress())
                                    : 0.0) + " avg",
                    8, iy + 36, GuiStyle.COL_TEXT_DIM, false);
            g.drawString(font, "Energy input only (no extract)", 8, iy + 45, GuiStyle.COL_TEXT_DIM, false);
        }
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
        CryoDistillerEntity be = menu.getBlockEntity();
        if (be == null) return;

        if (mx >= x + ENERGY_X && mx <= x + ENERGY_X + BAR_W && my >= y + BAR_Y && my <= y + BAR_Y + BAR_H) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal("Energy"));
            lines.add(Component.literal(ODFormat.formatEnergyPair(be.getEnergyStored(), be.getMaxEnergyStored())));
            lines.add(Component.literal("Usage: " + ODFormat.formatEnergyPerTick(be.getEnergyPerTick())));
            g.renderTooltip(font, lines, java.util.Optional.empty(), mx, my);
            return;
        }
        if (mx >= x + INPUT_X && mx <= x + INPUT_X + BAR_W && my >= y + BAR_Y && my <= y + BAR_Y + BAR_H) {
            List<Component> lines = new ArrayList<>();
            FluidStack in = be.getInputTank().getFluid();
            lines.add(Component.literal(in.isEmpty() ? "Input: Empty"
                    : "Input: " + in.getHoverName().getString()));
            lines.add(Component.literal(ODFormat.formatFluidPair(be.getInputTank().getFluidAmount(), CryoDistillerEntity.TANK_CAPACITY)));
            g.renderTooltip(font, lines, java.util.Optional.empty(), mx, my);
            return;
        }
        if (mx >= x + OUTPUT_X && mx <= x + OUTPUT_X + BAR_W && my >= y + BAR_Y && my <= y + BAR_Y + BAR_H) {
            List<Component> lines = new ArrayList<>();
            FluidStack out = be.getOutputTank().getFluid();
            lines.add(Component.literal(out.isEmpty() ? "Output: Empty"
                    : "Output: " + out.getHoverName().getString()));
            lines.add(Component.literal(ODFormat.formatFluidPair(be.getOutputTank().getFluidAmount(), CryoDistillerEntity.TANK_CAPACITY)));
            g.renderTooltip(font, lines, java.util.Optional.empty(), mx, my);
            return;
        }
        super.renderTooltip(g, mx, my);
    }
}
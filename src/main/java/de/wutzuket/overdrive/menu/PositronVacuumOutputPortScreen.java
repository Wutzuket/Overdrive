package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.integration.jei.scanner_jei_plugin;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PositronVacuumOutputPortScreen extends AbstractContainerScreen<PositronVacuumOutputPortMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    private static final int FLUID_BAR_X = 8;
    private static final int FLUID_BAR_Y = 20;
    private static final int FLUID_BAR_W = 130;
    private static final int FLUID_BAR_H = 16;

    private static final int SLOT_X = 148;
    private static final int SLOT_Y = 35;

    private double lastMouseX;
    private double lastMouseY;

    public PositronVacuumOutputPortScreen(PositronVacuumOutputPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = 72;
    }

    private FluidStack getFluidStack() {
        Fluid fluid = menu.getFluid();
        int amount = menu.getFluidAmount();
        if (fluid == Fluids.EMPTY || amount <= 0) return FluidStack.EMPTY;
        return new FluidStack(fluid, amount);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 1. Hintergrund-Textur vom Dispenser zeichnen
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 2. Grauer Bereich: Deckt die nicht benutzten 3x3 Dispenser-Slots vollständig ab (von y + 16 bis y + 80)
        guiGraphics.fill(x + 6, y + 16, x + 142, y + 80, 0xFFC6C6C6);

        // 3. Fluid-Bar Rahmen
        int fx = x + FLUID_BAR_X, fy = y + FLUID_BAR_Y, fw = FLUID_BAR_W, fh = FLUID_BAR_H;
        guiGraphics.fill(fx, fy, fx + fw, fy + fh, 0xFF2D2D2D);
        guiGraphics.fill(fx + 1, fy + 1, fx + fw, fy + fh, 0xFF707070);
        guiGraphics.fill(fx + 1, fy + 1, fx + fw - 1, fy + fh - 1, 0xFF141414);

        // 4. Fluid-Füllstand
        FluidStack fluidStack = getFluidStack();
        if (!fluidStack.isEmpty()) {
            int amount = fluidStack.getAmount();
            int capacity = menu.getFluidCapacity();
            int fillW = capacity > 0 ? amount * (fw - 2) / capacity : 0;
            if (fillW > 0) {
                fillW = Math.min(fillW, fw - 2);
                int color = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
                guiGraphics.fill(fx + 1, fy + 1, fx + 1 + fillW, fy + fh - 1, color);
                guiGraphics.fill(fx + 1, fy + 1, fx + 1 + fillW, fy + 2, 0x66FFFFFF);
            }
        }

        // 5. Einzelner Slot-Hintergrund oben rechts
        guiGraphics.blit(CONTAINER_BACKGROUND, x + SLOT_X - 1, y + SLOT_Y - 1, 61, 17, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        int amount = menu.getFluidAmount();
        guiGraphics.drawString(this.font, amount + " / " + menu.getFluidCapacity() + " mB", 8, 44, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_R || keyCode == GLFW.GLFW_KEY_U) && menu.getFluidAmount() > 0) {
            int x = (this.width - WIDTH) / 2;
            int y = (this.height - HEIGHT) / 2;

            if (lastMouseX >= x + FLUID_BAR_X && lastMouseX <= x + FLUID_BAR_X + FLUID_BAR_W &&
                    lastMouseY >= y + FLUID_BAR_Y && lastMouseY <= y + FLUID_BAR_Y + FLUID_BAR_H) {
                openJeiForFluid(keyCode == GLFW.GLFW_KEY_U);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void openJeiForFluid(boolean showUses) {
        FluidStack fluidStack = getFluidStack();
        IJeiRuntime jeiRuntime = scanner_jei_plugin.getRuntime();
        if (jeiRuntime != null && !fluidStack.isEmpty()) {
            IIngredientManager manager = jeiRuntime.getIngredientManager();
            manager.getIngredientTypeChecked(FluidStack.class).ifPresent(fluidType -> {
                jeiRuntime.getRecipesGui().show(
                        jeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(showUses ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, fluidType, fluidStack.copyWithAmount(1))
                );
            });
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        if (mouseX >= x + FLUID_BAR_X && mouseX <= x + FLUID_BAR_X + FLUID_BAR_W &&
                mouseY >= y + FLUID_BAR_Y && mouseY <= y + FLUID_BAR_Y + FLUID_BAR_H) {
            List<Component> lines = new ArrayList<>();
            FluidStack fluidStack = getFluidStack();
            if (!fluidStack.isEmpty()) {
                lines.add(fluidStack.getHoverName());
            } else {
                lines.add(Component.translatable("container.overdrive.positron_vacuum_output_port.empty"));
            }
            lines.add(Component.literal(menu.getFluidAmount() + " / " + menu.getFluidCapacity() + " mB"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
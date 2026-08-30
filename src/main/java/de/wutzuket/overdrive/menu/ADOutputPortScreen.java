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
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ADOutputPortScreen extends AbstractContainerScreen<ADOutputPortMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int FLUID_BAR_X = 18;
    private static final int FLUID_BAR_Y = 17;
    private static final int FLUID_BAR_W = 18;
    private static final int FLUID_BAR_H = 54;
    private static final int SLOT_X = 134;
    private static final int SLOT_Y = 35;

    public ADOutputPortScreen(ADOutputPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private FluidStack getFluidStack() {
        FluidStack tankFluid = menu.getBlockEntity().getFluidTank().getFluid();
        if (tankFluid.isEmpty()) return FluidStack.EMPTY;
        return tankFluid.copyWithAmount(menu.getFluidAmount());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.fill(x + 6, y + 16, x + 170, y + 72, 0xFFC6C6C6);

        int bx = x + FLUID_BAR_X;
        int by = y + FLUID_BAR_Y;

        guiGraphics.fill(bx, by, bx + FLUID_BAR_W, by + FLUID_BAR_H, 0xFF2D2D2D);
        guiGraphics.fill(bx + 1, by + 1, bx + FLUID_BAR_W, by + FLUID_BAR_H, 0xFF707070);
        guiGraphics.fill(bx + 1, by + 1, bx + FLUID_BAR_W - 1, by + FLUID_BAR_H - 1, 0xFF141414);

        FluidStack fluidStack = getFluidStack();
        int fluidAmount = fluidStack.getAmount();
        int fluidCapacity = menu.getFluidCapacity();

        int maxInnerH = FLUID_BAR_H - 2;
        int fillHeight = fluidCapacity > 0 ? (int) ((long) fluidAmount * maxInnerH / fluidCapacity) : 0;

        if (fillHeight > 0) {
            int innerX = bx + 1;
            int innerY = by + 1;
            int innerW = FLUID_BAR_W - 2;
            int fluidTop = innerY + maxInnerH - fillHeight;
            int color = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
            guiGraphics.fill(innerX, fluidTop, innerX + innerW, innerY + maxInnerH, color);
            guiGraphics.fill(innerX, fluidTop, innerX + innerW, fluidTop + 1, 0x66FFFFFF);
        }

        guiGraphics.blit(CONTAINER_BACKGROUND, x + SLOT_X - 1, y + SLOT_Y - 1, 61, 17, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private double lastMouseX;
    private double lastMouseY;

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
        FluidStack tankFluid = menu.getBlockEntity().getFluidTank().getFluid();
        IJeiRuntime jeiRuntime = scanner_jei_plugin.getRuntime();
        if (jeiRuntime != null && !tankFluid.isEmpty()) {
            IIngredientManager manager = jeiRuntime.getIngredientManager();
            manager.getIngredientTypeChecked(FluidStack.class).ifPresent(fluidType -> {
                jeiRuntime.getRecipesGui().show(
                        jeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(showUses ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, fluidType, tankFluid.copyWithAmount(1))
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
            if (menu.getFluidAmount() > 0) {
                lines.add(getFluidStack().getHoverName());
            } else {
                lines.add(Component.translatable("container.overdrive.ad_output_port.empty"));
            }
            lines.add(Component.literal(menu.getFluidAmount() + " / " + menu.getFluidCapacity() + " mB"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}

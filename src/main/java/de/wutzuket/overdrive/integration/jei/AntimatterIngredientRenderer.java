package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.antimatter.AntimatterStack;
import de.wutzuket.overdrive.config.ODConfig;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Draws the antimatter like a fluid: a small tank gauge in the color of the antimatter and the
 * amount of the content. Hovering over it shows the type and the amount in mB.
 */
public class AntimatterIngredientRenderer implements IIngredientRenderer<AntimatterStack> {

    public static final AntimatterIngredientRenderer INSTANCE = new AntimatterIngredientRenderer();

    private static final int SIZE = 16;
    private static final int BORDER = 0xFF101014;
    private static final int EMPTY = 0xFF23232A;

    @Override
    public void render(GuiGraphics graphics, AntimatterStack stack) {
        int x = 0;
        int y = 0;
        graphics.fill(x, y, x + SIZE, y + SIZE, BORDER);
        graphics.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, EMPTY);

        if (stack == null || stack.type() == null || stack.amount() <= 0) {
            return;
        }

        int capacity = Math.max(1, ODConfig.ANTIMATTER_TANK_CAPACITY.get());
        int inner = SIZE - 2;
        int fill = Math.max(1, Math.min(inner, (int) ((long) stack.amount() * inner / capacity)));
        int color = stack.type().color();
        int top = y + SIZE - 1 - fill;
        graphics.fill(x + 1, top, x + SIZE - 1, y + SIZE - 1, color);
        graphics.fill(x + 1, top, x + SIZE - 1, top + 1, 0x40FFFFFF);
    }

    @Override
    public List<Component> getTooltip(AntimatterStack stack, TooltipFlag flag) {
        if (stack == null || stack.type() == null) {
            return List.of(Component.translatable("jei.overdrive.antimatter.type.empty"));
        }
        return List.of(
                stack.type().displayName(),
                Component.literal(de.wutzuket.overdrive.util.ODFormat.formatFluid(stack.amount())));
    }

    @Override
    public int getWidth() {
        return SIZE;
    }

    @Override
    public int getHeight() {
        return SIZE;
    }
}

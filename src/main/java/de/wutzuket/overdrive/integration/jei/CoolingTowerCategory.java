package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.CoolingTowerRecipe;
import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/** Kuehlturm: heisses Fluid (Dampf) -> abgekuehlt (Wasser). */
public class CoolingTowerCategory extends FluidRecipeCategory<CoolingTowerRecipe> {
    public static final RecipeType<CoolingTowerRecipe> TYPE =
            RecipeType.create(OverdriveJeiPlugin.MODID, "cooling_tower", CoolingTowerRecipe.class);

    public CoolingTowerCategory(IGuiHelper guiHelper) {
        super(guiHelper, TYPE, "jei.overdrive.cooling_tower.title", ODBlocks.COOLING_TOWER_CONTROLLER.get());
    }

    @Override
    protected FluidIO getFluids(CoolingTowerRecipe recipe) {
        return FluidIO.of(recipe.inputFluid(), recipe.outputFluid());
    }

    @Override
    protected List<InfoLine> getInfoLines(CoolingTowerRecipe recipe) {
        List<InfoLine> lines = new ArrayList<>();
        lines.add(value("jei.overdrive.fluid.time", recipe.processTime(),
                String.format(java.util.Locale.ROOT, "%.2f", recipe.processTime() / 20.0)));
        lines.add(value("jei.overdrive.fluid.rate",
                ODFormat.formatFluidPerTick((double) recipe.outputFluid().getAmount() / Math.max(1, recipe.processTime()))));
        return lines;
    }

    @Override
    protected ItemLike[] getCatalysts() {
        return new ItemLike[]{ODBlocks.COOLING_TOWER_CONTROLLER.get(), ODBlocks.COOLING_TOWER_INPUT_PORT.get()};
    }
}
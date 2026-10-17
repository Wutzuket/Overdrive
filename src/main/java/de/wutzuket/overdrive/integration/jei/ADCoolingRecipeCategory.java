package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.AntiprotonCoolingRecipe;
import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/** AD-Ring: Antiprotonen -> abgebremste Antiprotonen. */
public class ADCoolingRecipeCategory extends FluidRecipeCategory<AntiprotonCoolingRecipe> {
    public static final RecipeType<AntiprotonCoolingRecipe> TYPE =
            RecipeType.create(OverdriveJeiPlugin.MODID, "ad_cooling", AntiprotonCoolingRecipe.class);

    public ADCoolingRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, TYPE, "jei.overdrive.ad.title", ODBlocks.AD_CONTROLLER.get());
    }

    @Override
    protected FluidIO getFluids(AntiprotonCoolingRecipe recipe) {
        return FluidIO.of(recipe.inputFluid(), recipe.outputFluid());
    }

    @Override
    protected List<InfoLine> getInfoLines(AntiprotonCoolingRecipe recipe) {
        List<InfoLine> lines = new ArrayList<>();
        lines.add(energy("jei.overdrive.fluid.energy", ODFormat.formatEnergy(recipe.energyCost())));
        lines.add(value("jei.overdrive.fluid.time", recipe.processTime(),
                String.format(java.util.Locale.ROOT, "%.2f", recipe.processTime() / 20.0)));
        if (!recipe.outputCell().isEmpty()) {
            lines.add(value("jei.overdrive.fluid.cell_output", recipe.outputCell().getHoverName().getString()));
        }
        return lines;
    }

    @Override
    protected ItemLike[] getCatalysts() {
        return new ItemLike[]{ODBlocks.AD_CONTROLLER.get()};
    }
}
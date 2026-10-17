package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.AntiprotonCoolingRecipe;
import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/** ELENA-Ring: abgebremste Antiprotonen -> gekuehlte Antiprotonen. */
public class ELENACoolingRecipeCategory extends FluidRecipeCategory<AntiprotonCoolingRecipe> {
    public static final RecipeType<AntiprotonCoolingRecipe> TYPE =
            RecipeType.create(OverdriveJeiPlugin.MODID, "elena_cooling", AntiprotonCoolingRecipe.class);

    public ELENACoolingRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, TYPE, "jei.overdrive.elena.title", ODBlocks.ELENA_CONTROLLER.get());
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
        return new ItemLike[]{ODBlocks.ELENA_CONTROLLER.get()};
    }
}
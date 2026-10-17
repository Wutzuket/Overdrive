package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.ReactorCoolingRecipe;
import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/**
 * Reaktorkuehlung des Annihilationsreaktors: Kuehlmittel -> heisses Kuehlmittel
 * (z.B. Wasser -> Dampf bzw. Cryofluid -> ueberhitztes Cryofluid).
 */
public class ReactorCoolingRecipeCategory extends FluidRecipeCategory<ReactorCoolingRecipe> {
    public static final RecipeType<ReactorCoolingRecipe> TYPE =
            RecipeType.create(OverdriveJeiPlugin.MODID, "reactor_cooling", ReactorCoolingRecipe.class);

    public ReactorCoolingRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, TYPE, "jei.overdrive.reactor_cooling.title", ODBlocks.COOLANT_INPUT_PORT.get());
    }

    @Override
    protected FluidIO getFluids(ReactorCoolingRecipe recipe) {
        return FluidIO.of(recipe.input(), recipe.output());
    }

    @Override
    protected List<InfoLine> getInfoLines(ReactorCoolingRecipe recipe) {
        List<InfoLine> lines = new ArrayList<>();
        lines.add(value("jei.overdrive.reactor_cooling.boiling", recipe.boilingTemp()));
        lines.add(value("jei.overdrive.reactor_cooling.usage", ODFormat.formatFluid(recipe.mbPer100K())));
        lines.add(value("jei.overdrive.reactor_cooling.cooling",
                String.format(java.util.Locale.ROOT, "%.2f", recipe.coolingPerMb())));
        lines.add(value("jei.overdrive.reactor_cooling.steam", ODFormat.formatFluid(recipe.steamPerMb())));
        lines.add(value("jei.overdrive.reactor_cooling.max_rate",
                ODFormat.formatFluidPerTick(recipe.maxMbPerTick())));
        return lines;
    }

    @Override
    protected ItemLike[] getCatalysts() {
        return new ItemLike[]{ODBlocks.COOLANT_INPUT_PORT.get(), ODBlocks.ANNIHILATION_CONTROLLER.get()};
    }
}
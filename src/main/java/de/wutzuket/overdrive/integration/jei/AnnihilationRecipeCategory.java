package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.AnnihilationRecipe;
import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Annihilationsreaktor: Antiprotonen + Positronen -> Energie.
 * Die Kuehlmittel-Seite laeuft ueber die eigene Reaktorkuehlung-Kategorie.
 */
public class AnnihilationRecipeCategory extends FluidRecipeCategory<AnnihilationRecipe> {
    public static final RecipeType<AnnihilationRecipe> TYPE =
            RecipeType.create(OverdriveJeiPlugin.MODID, "annihilation_generator", AnnihilationRecipe.class);

    public AnnihilationRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, TYPE, "jei.overdrive.annihilation.title", ODBlocks.ANNIHILATION_CONTROLLER.get());
    }

    @Override
    protected FluidIO getFluids(AnnihilationRecipe recipe) {
        List<FluidStack> inputs = new ArrayList<>();
        // Die Antiprotonen und Positronen werden ueber das Antimatter-System bewegt.
        return new FluidIO(inputs, recipe.outputFluid().isEmpty() ? null : recipe.outputFluid());
    }

    @Override
    protected AntimatterIO getAntimatters(AnnihilationRecipe recipe) {
        return new AntimatterIO(List.of(recipe.inputAntiproton(), recipe.inputPositron()), null);
    }

    @Override
    protected List<InfoLine> getInfoLines(AnnihilationRecipe recipe) {
        List<InfoLine> lines = new ArrayList<>();
        lines.add(energy("jei.overdrive.annihilation.energy", ODFormat.formatEnergyPerTick(recipe.energyPerTick())));
        lines.add(value("jei.overdrive.annihilation.temp", recipe.temperatureThreshold()));
        lines.add(value("jei.overdrive.fluid.time", recipe.processTime(),
                String.format(java.util.Locale.ROOT, "%.2f", recipe.processTime() / 20.0)));
        antimatter(recipe.inputAntiproton()).ifPresent(lines::add);
        antimatter(recipe.inputPositron()).ifPresent(lines::add);
        return lines;
    }

    @Override
    protected ItemLike[] getCatalysts() {
        return new ItemLike[]{ODBlocks.ANNIHILATION_CONTROLLER.get(), ODBlocks.CELL_DOCKING_PORT.get()};
    }
}
package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.CollisionRecipe;
import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/** Kollision im Teilchenbeschleuniger: Iridium aus dem Target -> Antiprotonen. */
public class AcceleratorRecipeCategory extends FluidRecipeCategory<CollisionRecipe> {
    public static final RecipeType<CollisionRecipe> TYPE = RecipeType.create(
            OverdriveJeiPlugin.MODID, "accelerator_collision", CollisionRecipe.class);

    public AcceleratorRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper, TYPE, "jei.overdrive.accelerator_collision.title", ODBlocks.PARTICLE_ACCELERATOR_CONTROLLER.get());
    }

    @Override
    protected FluidIO getFluids(CollisionRecipe recipe) {
        return new FluidIO(List.of(recipe.inputFluid()), recipe.outputFluid());
    }

    @Override
    protected List<InfoLine> getInfoLines(CollisionRecipe recipe) {
        List<InfoLine> lines = new ArrayList<>();
        lines.add(energy("jei.overdrive.fluid.energy", ODFormat.formatEnergy(recipe.energyCost())));
        lines.add(value("jei.overdrive.fluid.time", recipe.processTime(),
                String.format(java.util.Locale.ROOT, "%.2f", recipe.processTime() / 20.0)));
        lines.add(line(Component.translatable("jei.overdrive.accelerator_collision.input"), COL_LABEL));
        lines.add(line(Component.translatable("jei.overdrive.accelerator_collision.rate_hint"), COL_LABEL));
        return lines;
    }

    @Override
    protected ItemLike[] getCatalysts() {
        return new ItemLike[]{ODBlocks.PARTICLE_ACCELERATOR_CONTROLLER.get(), ODBlocks.IRIDIUM_TARGET.get()};
    }
}
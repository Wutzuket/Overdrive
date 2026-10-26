package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.PositronExtractionRecipe;
import de.wutzuket.overdrive.util.ODFormat;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Elektronenkanone: Tungstennetz -> Positronen (Block Elektronenkanone + Tungsten-Target). */
public class PositronExtractionCategory extends FluidRecipeCategory<PositronExtractionRecipe> {
    public static final RecipeType<PositronExtractionRecipe> TYPE =
            RecipeType.create(OverdriveJeiPlugin.MODID, "positron_extraction", PositronExtractionRecipe.class);

    public PositronExtractionCategory(IGuiHelper guiHelper) {
        super(guiHelper, TYPE, "jei.overdrive.positron_extraction.title", ODBlocks.ELECTRON_GUN.get());
    }

    @Override
    protected FluidIO getFluids(PositronExtractionRecipe recipe) {
        return new FluidIO(List.of(), null);
    }

    @Override
    protected AntimatterIO getAntimatters(PositronExtractionRecipe recipe) {
        // Die Positronen werden ueber das Antimatter-System ausgegeben und muessen
        // deshalb als eigener Antimatter-Slot angezeigt werden.
        return AntimatterIO.of(null, recipe.outputAntimatter());
    }

    @Override
    protected void addExtraSlots(@NotNull IRecipeLayoutBuilder builder, @NotNull PositronExtractionRecipe recipe) {
        builder.addSlot(RecipeIngredientRole.INPUT, extraInputSlotX(), SLOT_Y)
                .addItemStack(new ItemStack(ODItems.TUNGSTEN_MESH.get()));
        if (!recipe.outputCell().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, centeredOutputX() + IN_OUT_GAP, SLOT_Y)
                    .addItemStack(recipe.outputCell());
        }
    }

    @Override
    protected int extraInputSlotX() {
        return centeredOutputX() - IN_OUT_GAP - SLOT_SIZE;
    }

    @Override
    protected List<InfoLine> getInfoLines(PositronExtractionRecipe recipe) {
        List<InfoLine> lines = new ArrayList<>();
        lines.add(energy("jei.overdrive.fluid.energy", ODFormat.formatEnergy(recipe.energyCost())));
        lines.add(value("jei.overdrive.fluid.time", recipe.processTime(),
                String.format(java.util.Locale.ROOT, "%.2f", recipe.processTime() / 20.0)));
        lines.add(value("jei.overdrive.fluid.mesh", recipe.meshDurabilityCost()));
        if (!recipe.outputAntimatter().isEmpty()) {
            antimatter(recipe.outputAntimatter()).ifPresent(lines::add);
            lines.add(production(recipe.outputAntimatter().amount() / (double) Math.max(1, recipe.processTime())));
        }
        return lines;
    }

    @Override
    protected ItemLike[] getCatalysts() {
        return new ItemLike[]{ODBlocks.ELECTRON_GUN.get(), ODBlocks.TUNGSTEN_TARGET.get(),
                ODBlocks.POSITRON_VACUUM_OUTPUT_PORT.get()};
    }
}
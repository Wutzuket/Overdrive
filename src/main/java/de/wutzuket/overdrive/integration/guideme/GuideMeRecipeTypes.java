package de.wutzuket.overdrive.integration.guideme;

import de.wutzuket.overdrive.antimatter.AntimatterStack;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe;
import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.fluid.ODItemsFluid;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.AnnihilationRecipe;
import de.wutzuket.overdrive.recipe.AntiprotonCoolingRecipe;
import de.wutzuket.overdrive.recipe.CircuitPressRecipe;
import de.wutzuket.overdrive.recipe.CollisionRecipe;
import de.wutzuket.overdrive.recipe.CoolingTowerRecipe;
import de.wutzuket.overdrive.recipe.CryoDistillerRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import de.wutzuket.overdrive.recipe.PositronExtractionRecipe;
import de.wutzuket.overdrive.recipe.ReactorCoolingRecipe;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.block.LytBlock;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytSlotGrid;
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.scene.LytItemImage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Meldet die Overdrive-Rezepttypen an GuideME weiter, damit das Handbuch die
 * Rezepte der Maschinen und des Scanners anzeigen kann.
 *
 * <p>GuideME kann nur die Vanilla-Rezepttypen von sich aus darstellen. Die
 * eigenen Rezepttypen der Mod werden ueber die
 * {@link RecipeTypeMappingSupplier}-Schnittstelle registriert und deshalb in
 * allen GuideBuechern - auch in denen anderer Mods - angezeigt.
 *
 * <p>Die Registrierung erfolgt ueber die Service-Datei
 * {@code META-INF/services/guideme.compiler.tags.RecipeTypeMappingSupplier}.
 */
public class GuideMeRecipeTypes implements RecipeTypeMappingSupplier {

    @Override
    public void collect(RecipeTypeMappings mappings) {
        mappings.add(ODRecipeTypes.CIRCUIT_PRESS.get(), GuideMeRecipeTypes::circuitPress);
        mappings.add(ODRecipeTypes.COLLISION.get(), GuideMeRecipeTypes::collision);
        mappings.add(ODRecipeTypes.AD_RECIPE.get(), GuideMeRecipeTypes::antiprotonCooling);
        mappings.add(ODRecipeTypes.ELENA_RECIPE.get(), GuideMeRecipeTypes::antiprotonCooling);
        mappings.add(ODRecipeTypes.POSITRON_EXTRACTION.get(), GuideMeRecipeTypes::positronExtraction);
        mappings.add(ODRecipeTypes.ANNIHILATION.get(), GuideMeRecipeTypes::annihilation);
        mappings.add(ODRecipeTypes.COOLING_TOWER.get(), GuideMeRecipeTypes::coolingTower);
        mappings.add(ODRecipeTypes.REACTOR_COOLING.get(), GuideMeRecipeTypes::reactorCooling);
        mappings.add(ODRecipeTypes.CRYO_DISTILLER.get(), GuideMeRecipeTypes::cryoDistiller);
        mappings.add(ODRecipeTypes.SCANNER.get(), GuideMeRecipeTypes::scanner);
    }

    // ------------------------------------------------------------------
    // Scanner
    // ------------------------------------------------------------------

    /**
     * Zeigt das 3x3x3-Muster des Scanners als Ebenen aus Slots an.
     */
    private static LytBlock scanner(RecipeHolder<scanner_recipe> holder) {
        var recipe = holder.value();

        var layers = new LytVBox();
        for (int layer = 0; layer < recipe.layerCount(); layer++) {
            var grid = new LytSlotGrid(3, 3);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    var matcher = recipe.getMatcher(layer, row, col);
                    if (matcher == null) {
                        continue;
                    }
                    var stacks = matcher.displayStacks();
                    if (!stacks.isEmpty()) {
                        grid.setItem(col, row, stacks.getFirst());
                    }
                }
            }
            layers.append(grid);
        }

        var result = new LytVBox();
        result.append(LytStandardRecipeBox.builder()
                .title(Component.translatable("jei.overdrive.scanner.title").getString())
                .icon(ODBlocks.SCANNER.get())
                .input(LytSlotGrid.row(List.of(), false))
                .output(recipe.getResultStack())
                .build(holder));
        result.append(layers);
        return result;
    }

    // ------------------------------------------------------------------
    // Maschinen
    // ------------------------------------------------------------------

    private static LytBlock circuitPress(RecipeHolder<CircuitPressRecipe> holder) {
        var recipe = holder.value();
        var grid = new LytSlotGrid(Math.max(1, recipe.ingredients().size()), 1);
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            grid.setIngredient(i, 0, recipe.ingredients().get(i));
        }
        return LytStandardRecipeBox.builder()
                .title(Component.translatable("jei.overdrive.circuit_press.title").getString())
                .icon(ODBlocks.CIRCUIT_PRESS.get())
                .input(grid)
                .output(recipe.output())
                .build(holder);
    }

    private static LytBlock collision(RecipeHolder<CollisionRecipe> holder) {
        var recipe = holder.value();
        return box("jei.overdrive.accelerator_collision.title", ODBlocks.PARTICLE_ACCELERATOR_CONTROLLER.get(),
                fluid(recipe.inputFluid()), antimatter(recipe.outputAntimatter()), holder);
    }

    private static LytBlock antiprotonCooling(RecipeHolder<AntiprotonCoolingRecipe> holder) {
        var recipe = holder.value();
        boolean ad = recipe.getType() == ODRecipeTypes.AD_RECIPE.get();
        return box(ad ? "jei.overdrive.ad.title" : "jei.overdrive.elena.title",
                ad ? ODBlocks.AD_CONTROLLER.get() : ODBlocks.ELENA_CONTROLLER.get(),
                antimatter(recipe.inputAntimatter()), antimatter(recipe.outputAntimatter()), holder);
    }

    private static LytBlock positronExtraction(RecipeHolder<PositronExtractionRecipe> holder) {
        var recipe = holder.value();
        var inputs = new LytVBox();
        inputs.append(itemImage(ODItems.TUNGSTEN_MESH.get()));
        inputs.append(itemImage(ODItems.TUNGSTEN_TARGET.get()));
        return box("jei.overdrive.positron_extraction.title", ODBlocks.ELECTRON_GUN.get(),
                inputs, antimatter(recipe.outputAntimatter()), holder);
    }

    private static LytBlock annihilation(RecipeHolder<AnnihilationRecipe> holder) {
        var recipe = holder.value();
        var inputs = new LytVBox();
        inputs.append(antimatter(recipe.inputAntiproton()));
        inputs.append(antimatter(recipe.inputPositron()));
        return box("jei.overdrive.annihilation.title", ODBlocks.ANNIHILATION_CONTROLLER.get(),
                inputs, LytParagraph.of(""), holder);
    }

    private static LytBlock coolingTower(RecipeHolder<CoolingTowerRecipe> holder) {
        var recipe = holder.value();
        return box("jei.overdrive.cooling_tower.title", ODBlocks.COOLING_TOWER_CONTROLLER.get(),
                fluid(recipe.inputFluid()), fluid(recipe.outputFluid()), holder);
    }

    private static LytBlock reactorCooling(RecipeHolder<ReactorCoolingRecipe> holder) {
        var recipe = holder.value();
        return box("jei.overdrive.reactor_cooling.title", ODBlocks.COOLANT_INPUT_PORT.get(),
                fluid(recipe.input()), fluid(recipe.output()), holder);
    }

    private static LytBlock cryoDistiller(RecipeHolder<CryoDistillerRecipe> holder) {
        var recipe = holder.value();
        return box("jei.overdrive.cryo_distiller.title", ODBlocks.CRYO_DISTILLER.get(),
                fluid(recipe.inputFluid()), fluid(recipe.outputFluid()), holder);
    }

    // ------------------------------------------------------------------
    // Hilfen
    // ------------------------------------------------------------------

    private static LytBlock box(String titleKey, Block icon, LytBlock input, LytBlock output,
                                RecipeHolder<? extends Recipe<RecipeInput>> holder) {
        return LytStandardRecipeBox.builder()
                .title(Component.translatable(titleKey).getString())
                .icon(icon)
                .addLeft(input)
                .addRight(output)
                .build(holder);
    }

    private static LytItemImage itemImage(Item item) {
        var image = new LytItemImage();
        image.setItem(new ItemStack(item));
        return image;
    }

    private static LytBlock fluid(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return LytParagraph.of("-");
        }
        var bucket = bucketOf(stack.getFluid());
        if (bucket == null) {
            return LytParagraph.of(stack.getDisplayName().getString());
        }
        return itemImage(bucket);
    }

    /**
     * @return der Eimer des Fluids oder {@code null}, wenn es keinen gibt
     */
    private static Item bucketOf(net.minecraft.world.level.material.Fluid fluid) {
        if (fluid == ODFluids.CRYOFLUID_SOURCE.get()) {
            return ODItemsFluid.CRYOFLUID_BUCKET.get();
        }
        if (fluid == ODFluids.STEAM_SOURCE.get()) {
            return ODItemsFluid.STEAM_BUCKET.get();
        }
        if (fluid == ODFluids.LIQUID_IRIDIUM_SOURCE.get()) {
            return ODItemsFluid.LIQUID_IRIDIUM_BUCKET.get();
        }
        if (fluid == ODFluids.SUPERHEATED_CRYOFLUID_SOURCE.get()) {
            return ODItemsFluid.SUPERHEATED_CRYOFLUID_BUCKET.get();
        }
        return null;
    }

    private static LytBlock antimatter(AntimatterStack stack) {
        if (stack == null || stack.isEmpty()) {
            return LytParagraph.of("-");
        }
        // Die Zelle des passenden Typs zeigt die Menge in ihrem Tooltip.
        var cell = ODItems.getCellFor(stack.type());
        if (cell == null) {
            return LytParagraph.of(stack.type().displayName().getString());
        }
        return itemImage(cell);
    }
}

package de.wutzuket.overdrive.block.scanner.recipe;

import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Zugriff auf die Scanner-Rezepte. Die Rezepte liegen als normale
 * Vanilla-Rezepte mit dem Typ {@code overdrive:scanner} in den Datenpacks und
 * werden dadurch nur passend zum Rezepttyp geladen.
 */
public final class scanner_recipe_manager {
    private static final Direction[] HORIZONTAL_FACINGS = new Direction[] {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    };

    private scanner_recipe_manager() {
    }

    /**
     * Alle Scanner-Rezepte der Welt.
     */
    public static @NotNull List<scanner_recipe> getRecipes(@NotNull Level level) {
        List<scanner_recipe> recipes = new ArrayList<>();
        for (RecipeHolder<scanner_recipe> holder : getRecipeHolders(level)) {
            recipes.add(holder.value());
        }
        return recipes;
    }

    /**
     * Alle Scanner-Rezepte der Welt als Holder.
     */
    public static @NotNull List<RecipeHolder<scanner_recipe>> getRecipeHolders(@NotNull Level level) {
        return level.getRecipeManager().getAllRecipesFor(ODRecipeTypes.SCANNER.get());
    }

    /**
     * Ein Scanner-Rezept anhand seiner ID. Liefert {@code null}, wenn es kein
     * Scanner-Rezept mit dieser ID gibt.
     */
    public static @Nullable scanner_recipe getRecipe(@NotNull Level level, @NotNull ResourceLocation id) {
        RecipeHolder<?> holder = level.getRecipeManager().byKey(id).orElse(null);
        if (holder == null || !(holder.value() instanceof scanner_recipe recipe)) {
            return null;
        }
        return recipe;
    }

    public static @Nullable scanner_match findMatch(@NotNull Level level, @NotNull BlockPos scannerPos,
                                                    @NotNull BlockState scannerState) {
        for (RecipeHolder<scanner_recipe> holder : getRecipeHolders(level)) {
            scanner_recipe recipe = holder.value();
            for (Direction facing : HORIZONTAL_FACINGS) {
                if (recipe.matches(level, scannerPos, scannerState, facing, false)) {
                    return new scanner_match(holder.id(), recipe, facing, false);
                }
                if (recipe.acceptMirrored() && recipe.matches(level, scannerPos, scannerState, facing, true)) {
                    return new scanner_match(holder.id(), recipe, facing, true);
                }
            }
        }
        return null;
    }
}

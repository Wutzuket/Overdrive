package de.wutzuket.overdrive.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import de.wutzuket.overdrive.config.ODConfig;

import java.util.List;

public final class CircuitPressRecipe implements Recipe<RecipeInput> {
    private final List<Ingredient> ingredients;
    private final ItemStack output;
    private final int energyPerTick;
    private final int processTime;

    public CircuitPressRecipe(List<Ingredient> ingredients, ItemStack output, int energyPerTick, int processTime) {
        if (ingredients.isEmpty() || ingredients.size() > 4) {
            throw new IllegalArgumentException("Circuit Press recipes must have 1 to 4 ingredients");
        }
        this.ingredients = List.copyOf(ingredients);
        this.output = output.copy();
        this.energyPerTick = Math.max(0, energyPerTick);
        this.processTime = Math.max(1, processTime);
    }

    public List<Ingredient> ingredients() { return ingredients; }
    public ItemStack output() { return output; }
    public int energyPerTick() {
        return energyPerTick > 0 ? energyPerTick : ODConfig.CIRCUIT_PRESS_ENERGY_PER_TICK.get();
    }
    public int processTime() {
        return processTime > 0 ? processTime : ODConfig.CIRCUIT_PRESS_PROCESS_TIME.get();
    }
    public long totalEnergy() { return (long) energyPerTick() * processTime(); }

    @Override public boolean matches(RecipeInput input, Level level) { return false; }
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) { return output.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return output.copy(); }
    @Override public RecipeSerializer<?> getSerializer() { return ODRecipeSerializers.CIRCUIT_PRESS_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ODRecipeTypes.CIRCUIT_PRESS.get(); }
}

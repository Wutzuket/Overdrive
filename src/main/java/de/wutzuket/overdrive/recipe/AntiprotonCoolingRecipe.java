package de.wutzuket.overdrive.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class AntiprotonCoolingRecipe implements Recipe<RecipeInput> {
    private final RecipeType<?> recipeType;
    private final RecipeSerializer<?> serializer;
    private final FluidStack inputFluid;
    private final int energyCost;
    private final int processTime;
    private final FluidStack outputFluid;
    private final ItemStack outputCell;

    public AntiprotonCoolingRecipe(RecipeType<?> recipeType, RecipeSerializer<?> serializer, FluidStack inputFluid, int energyCost, int processTime, FluidStack outputFluid, ItemStack outputCell) {
        this.recipeType = recipeType;
        this.serializer = serializer;
        this.inputFluid = inputFluid;
        this.energyCost = energyCost;
        this.processTime = processTime;
        this.outputFluid = outputFluid;
        this.outputCell = outputCell;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() { return serializer; }

    @Override
    public RecipeType<?> getType() { return recipeType; }

    public FluidStack inputFluid() { return inputFluid; }
    public int energyCost() { return energyCost; }
    public int processTime() { return processTime; }
    public FluidStack outputFluid() { return outputFluid; }
    public ItemStack outputCell() { return outputCell; }
}

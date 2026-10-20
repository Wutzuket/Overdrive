package de.wutzuket.overdrive.recipe;

import de.wutzuket.overdrive.antimatter.AntimatterStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class AntiprotonCoolingRecipe implements Recipe<RecipeInput> {
    private final RecipeType<?> recipeType;
    private final RecipeSerializer<?> serializer;
    private final AntimatterStack inputAntimatter;
    private final int energyCost;
    private final int processTime;
    private final AntimatterStack outputAntimatter;
    private final ItemStack outputCell;

    public AntiprotonCoolingRecipe(RecipeType<?> recipeType, RecipeSerializer<?> serializer, AntimatterStack inputAntimatter, int energyCost, int processTime, AntimatterStack outputAntimatter, ItemStack outputCell) {
        this.recipeType = recipeType;
        this.serializer = serializer;
        this.inputAntimatter = inputAntimatter;
        this.energyCost = energyCost;
        this.processTime = processTime;
        this.outputAntimatter = outputAntimatter;
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

    public AntimatterStack inputAntimatter() { return inputAntimatter; }
    public int energyCost() { return energyCost; }
    public int processTime() { return processTime; }
    public AntimatterStack outputAntimatter() { return outputAntimatter; }
    public ItemStack outputCell() { return outputCell; }
}

package de.wutzuket.overdrive.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class CoolingTowerRecipe implements Recipe<RecipeInput> {
    private final FluidStack inputFluid;
    private final FluidStack outputFluid;
    private final int processTime;
    public CoolingTowerRecipe(FluidStack in, FluidStack out, int time){ this.inputFluid=in; this.outputFluid=out; this.processTime=time; }
    @Override public boolean matches(RecipeInput input, Level level){ return true; }
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider r){ return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w,int h){ return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider r){ return ItemStack.EMPTY; }
    @Override public RecipeSerializer<?> getSerializer(){ return ODRecipeSerializers.COOLING_TOWER_SERIALIZER.get(); }
    @Override public RecipeType<?> getType(){ return ODRecipeTypes.COOLING_TOWER.get(); }
    public FluidStack inputFluid(){ return inputFluid; }
    public FluidStack outputFluid(){ return outputFluid; }
    public int processTime(){ return processTime; }
}

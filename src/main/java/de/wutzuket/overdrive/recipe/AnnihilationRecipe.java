package de.wutzuket.overdrive.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class AnnihilationRecipe implements Recipe<RecipeInput> {
    private final FluidStack inputAntiproton;
    private final FluidStack inputPositron;
    private final FluidStack inputCoolant;
    private final FluidStack outputFluid;
    private final int energyPerTick;
    private final int temperatureThreshold;
    private final int processTime;

    public AnnihilationRecipe(FluidStack inputAntiproton, FluidStack inputPositron, FluidStack inputCoolant, FluidStack outputFluid, int energyPerTick, int temperatureThreshold, int processTime){
        this.inputAntiproton=inputAntiproton; this.inputPositron=inputPositron; this.inputCoolant=inputCoolant; this.outputFluid=outputFluid; this.energyPerTick=energyPerTick; this.temperatureThreshold=temperatureThreshold; this.processTime=processTime;
    }
    @Override public boolean matches(RecipeInput input, Level level){ return true; }
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider r){ return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w,int h){ return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider r){ return ItemStack.EMPTY; }
    @Override public RecipeSerializer<?> getSerializer(){ return ODRecipeSerializers.ANNIHILATION_SERIALIZER.get(); }
    @Override public RecipeType<?> getType(){ return ODRecipeTypes.ANNIHILATION.get(); }

    public FluidStack inputAntiproton(){ return inputAntiproton; }
    public FluidStack inputPositron(){ return inputPositron; }
    public FluidStack inputCoolant(){ return inputCoolant; }
    public FluidStack outputFluid(){ return outputFluid; }
    public int energyPerTick(){ return energyPerTick; }
    public int temperatureThreshold(){ return temperatureThreshold; }
    public int processTime(){ return processTime; }
}

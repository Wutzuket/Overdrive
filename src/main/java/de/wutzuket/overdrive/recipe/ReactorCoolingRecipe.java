package de.wutzuket.overdrive.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class ReactorCoolingRecipe implements Recipe<RecipeInput> {
    private final FluidStack input;
    private final FluidStack output;
    private final int mbPer100K; // wie viele mB pro 100K über Boiling verbraucht
    private final double coolingPerMb; // wie viel C/K pro mB pro Tick abgeführt (Komma erlaubt)
    private final int boilingTemp; // ab wann kühlt es, z.B. 100
    private final int steamPerMb; // wie viel mB Steam pro mB Coolant
    private final int maxMbPerTick; // Verdampfungs-Limit: max. mB Coolant pro Tick (darüber kühlt es nicht mehr genug)

    public ReactorCoolingRecipe(FluidStack input, FluidStack output, int mbPer100K, double coolingPerMb, int boilingTemp, int steamPerMb, int maxMbPerTick){
        this.input=input; this.output=output; this.mbPer100K=mbPer100K; this.coolingPerMb=coolingPerMb; this.boilingTemp=boilingTemp; this.steamPerMb=steamPerMb; this.maxMbPerTick=maxMbPerTick;
    }
    @Override public boolean matches(RecipeInput in, Level lvl){ return true; }
    @Override public ItemStack assemble(RecipeInput in, HolderLookup.Provider r){ return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w,int h){ return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider r){ return ItemStack.EMPTY; }
    @Override public RecipeSerializer<?> getSerializer(){ return ODRecipeSerializers.REACTOR_COOLING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType(){ return ODRecipeTypes.REACTOR_COOLING.get(); }
    public FluidStack input(){ return input; }
    public FluidStack output(){ return output; }
    public int mbPer100K(){ return mbPer100K; }
    public double coolingPerMb(){ return coolingPerMb; }
    public int boilingTemp(){ return boilingTemp; }
    public int steamPerMb(){ return steamPerMb; }
    public int maxMbPerTick(){ return maxMbPerTick; }
}

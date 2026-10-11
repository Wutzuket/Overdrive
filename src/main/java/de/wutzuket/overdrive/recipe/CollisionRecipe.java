package de.wutzuket.overdrive.recipe;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class CollisionRecipe implements Recipe<RecipeInput> {
    // z.B. 1000 mB Liquid Iridium + 250 MFE = 20.000 mB Antiprotonen (1 Op, 1 Tick).
    // Pro 1 mB Antiproton: 0,05 mB Iridium + 20.000 FE. Skaliert linear mit der GUI-FE/t-Rate.
    private final FluidStack inputFluid;
    private final int energyCost;
    private final int processTime;
    private final FluidStack outputFluid;

    public CollisionRecipe(FluidStack inputFluid, int energyCost, int processTime, FluidStack outputFluid) {
        this.inputFluid = inputFluid;
        this.energyCost = energyCost;
        this.processTime = processTime;
        this.outputFluid = outputFluid;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return true;
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
    public RecipeSerializer<?> getSerializer() {
        return ODRecipeSerializers.COLLISION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ODRecipeTypes.COLLISION.get();
    }

    public FluidStack inputFluid() {
        return inputFluid;
    }

    public int energyCost() {
        return energyCost;
    }

    public int processTime() {
        return processTime;
    }

    public FluidStack outputFluid() {
        return outputFluid;
    }
}

package de.wutzuket.overdrive.recipe;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class CollisionRecipe implements Recipe<RecipeInput> {
    private final Ingredient inputTarget;
    private final int energyCost;
    private final int processTime;
    private final FluidStack outputFluid;

    public CollisionRecipe(Ingredient inputTarget, int energyCost, int processTime, FluidStack outputFluid) {
        this.inputTarget = inputTarget;
        this.energyCost = energyCost;
        this.processTime = processTime;
        this.outputFluid = outputFluid;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return inputTarget.test(input.getItem(0));
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

    public Ingredient inputTarget() {
        return inputTarget;
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

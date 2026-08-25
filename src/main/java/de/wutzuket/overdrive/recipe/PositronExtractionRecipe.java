package de.wutzuket.overdrive.recipe;

import de.wutzuket.overdrive.index.ODItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class PositronExtractionRecipe implements Recipe<RecipeInput> {
    private final int meshDurabilityCost;
    private final int energyCost;
    private final int processTime;
    private final List<ResultEntry> resultEntries;
    private final FluidStack outputFluid;
    private final ItemStack outputCell;

    public PositronExtractionRecipe(int meshDurabilityCost, int energyCost, int processTime, FluidStack outputFluid, ItemStack outputCell) {
        this.meshDurabilityCost = meshDurabilityCost;
        this.energyCost = energyCost;
        this.processTime = processTime;
        this.outputFluid = outputFluid;
        this.outputCell = outputCell;
        this.resultEntries = buildResultEntries(outputFluid, outputCell);
    }

    public PositronExtractionRecipe(int meshDurabilityCost, int energyCost, int processTime, List<ResultEntry> resultEntries) {
        this.meshDurabilityCost = meshDurabilityCost;
        this.energyCost = energyCost;
        this.processTime = processTime;
        this.resultEntries = resultEntries;
        this.outputFluid = extractFluid(resultEntries);
        this.outputCell = extractItem(resultEntries);
    }

    private static FluidStack extractFluid(List<ResultEntry> entries) {
        for (ResultEntry entry : entries) {
            if (entry.amount() > 0) {
                var holder = BuiltInRegistries.FLUID.getHolder(entry.id());
                if (holder.isPresent()) {
                    return new FluidStack(holder.get(), entry.amount());
                }
            }
        }
        return FluidStack.EMPTY;
    }

    private static ItemStack extractItem(List<ResultEntry> entries) {
        for (ResultEntry entry : entries) {
            if (entry.count() > 0) {
                var holder = BuiltInRegistries.ITEM.getHolder(entry.id());
                if (holder.isPresent()) {
                    return new ItemStack(holder.get(), entry.count());
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<ResultEntry> buildResultEntries(FluidStack fluid, ItemStack item) {
        List<ResultEntry> list = new ArrayList<>();
        if (!fluid.isEmpty()) {
            list.add(new ResultEntry(fluid.getFluid().builtInRegistryHolder().key().location(), fluid.getAmount(), 0));
        }
        if (!item.isEmpty()) {
            list.add(new ResultEntry(item.getItem().builtInRegistryHolder().key().location(), 0, item.getCount()));
        }
        return list;
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
    public RecipeSerializer<?> getSerializer() {
        return ODRecipeSerializers.POSITRON_EXTRACTION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ODRecipeTypes.POSITRON_EXTRACTION.get();
    }

    public int meshDurabilityCost() { return meshDurabilityCost; }
    public int energyCost() { return energyCost; }
    public int processTime() { return processTime; }
    public List<ResultEntry> resultEntries() { return resultEntries; }
    public FluidStack outputFluid() { return outputFluid; }
    public ItemStack outputCell() { return outputCell; }

    public record ResultEntry(ResourceLocation id, int amount, int count) {}
}

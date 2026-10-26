package de.wutzuket.overdrive.recipe;

import de.wutzuket.overdrive.index.ODItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.AntimatterStack;
import de.wutzuket.overdrive.antimatter.ODAntimatters;

import java.util.ArrayList;
import java.util.List;

public class PositronExtractionRecipe implements Recipe<RecipeInput> {
    private final int meshDurabilityCost;
    private final int energyCost;
    private final int processTime;
    /** Maximal einstellbare FE/t-Rate; 0 = kein Rezept-Limit. */
    private final int maxEnergyRate;
    private final List<ResultEntry> resultEntries;
    private final AntimatterStack outputAntimatter;
    private final ItemStack outputCell;

    public PositronExtractionRecipe(int meshDurabilityCost, int energyCost, int processTime, int maxEnergyRate, AntimatterStack outputAntimatter, ItemStack outputCell) {
        this.meshDurabilityCost = meshDurabilityCost;
        this.energyCost = energyCost;
        this.processTime = processTime;
        this.maxEnergyRate = maxEnergyRate;
        this.outputAntimatter = outputAntimatter;
        this.outputCell = outputCell;
        this.resultEntries = buildResultEntries(outputAntimatter, outputCell);
    }

    public PositronExtractionRecipe(int meshDurabilityCost, int energyCost, int processTime, int maxEnergyRate, List<ResultEntry> resultEntries) {
        this.meshDurabilityCost = meshDurabilityCost;
        this.energyCost = energyCost;
        this.processTime = processTime;
        this.maxEnergyRate = maxEnergyRate;
        this.resultEntries = resultEntries;
        this.outputAntimatter = extractAntimatter(resultEntries);
        this.outputCell = extractItem(resultEntries);
    }

    private static AntimatterStack extractAntimatter(List<ResultEntry> entries) {
        for (ResultEntry entry : entries) {
            if (entry.amount() > 0) {
                Antimatter type = ODAntimatters.get(entry.id());
                if (type != null) {
                    return new AntimatterStack(type, entry.amount());
                }
            }
        }
        return new AntimatterStack(null, 0);
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

    private static List<ResultEntry> buildResultEntries(AntimatterStack fluid, ItemStack item) {
        List<ResultEntry> list = new ArrayList<>();
        if (!fluid.isEmpty() && fluid.type() != null) {
            list.add(new ResultEntry(fluid.type().id(), fluid.amount(), 0));
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
    public int maxEnergyRate() { return maxEnergyRate; }
    public List<ResultEntry> resultEntries() { return resultEntries; }
    public AntimatterStack outputAntimatter() { return outputAntimatter; }
    public ItemStack outputCell() { return outputCell; }

    public record ResultEntry(ResourceLocation id, int amount, int count) {}
}

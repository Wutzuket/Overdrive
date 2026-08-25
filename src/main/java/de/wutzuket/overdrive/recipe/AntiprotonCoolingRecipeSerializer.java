package de.wutzuket.overdrive.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AntiprotonCoolingRecipeSerializer implements RecipeSerializer<AntiprotonCoolingRecipe> {
    private final Supplier<RecipeType<AntiprotonCoolingRecipe>> recipeTypeSupplier;
    private final MapCodec<AntiprotonCoolingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, AntiprotonCoolingRecipe> streamCodec;

    public AntiprotonCoolingRecipeSerializer(Supplier<RecipeType<AntiprotonCoolingRecipe>> recipeTypeSupplier) {
        this.recipeTypeSupplier = recipeTypeSupplier;
        this.codec = createCodec(recipeTypeSupplier, this);
        this.streamCodec = StreamCodec.of(
                this::toNetwork,
                this::fromNetwork
        );
    }

    private static MapCodec<AntiprotonCoolingRecipe> createCodec(Supplier<RecipeType<AntiprotonCoolingRecipe>> typeSupplier, RecipeSerializer<AntiprotonCoolingRecipe> serializer) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        FluidStack.CODEC.fieldOf("input_fluid").forGetter(AntiprotonCoolingRecipe::inputFluid),
                        Codec.INT.fieldOf("energy_cost").forGetter(AntiprotonCoolingRecipe::energyCost),
                        Codec.INT.fieldOf("process_time").forGetter(AntiprotonCoolingRecipe::processTime),
                        Codec.list(Codec.either(FluidStack.CODEC, ItemStack.CODEC)).fieldOf("results").forGetter(r -> {
                            List<Either<FluidStack, ItemStack>> list = new ArrayList<>();
                            if (!r.outputFluid().isEmpty()) list.add(Either.left(r.outputFluid()));
                            if (!r.outputCell().isEmpty()) list.add(Either.right(r.outputCell()));
                            return list;
                        })
                ).apply(instance, (inputFluid, energyCost, processTime, results) -> {
                    FluidStack outputFluid = FluidStack.EMPTY;
                    ItemStack outputCell = ItemStack.EMPTY;
                    for (var either : results) {
                        var fs = either.left().orElse(null);
                        var is = either.right().orElse(null);
                        if (fs != null && outputFluid.isEmpty()) outputFluid = fs;
                        if (is != null && outputCell.isEmpty()) outputCell = is;
                    }
                    return new AntiprotonCoolingRecipe(typeSupplier.get(), serializer, inputFluid, energyCost, processTime, outputFluid, outputCell);
                })
        );
    }

    @Override
    public MapCodec<AntiprotonCoolingRecipe> codec() { return codec; }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AntiprotonCoolingRecipe> streamCodec() { return streamCodec; }

    private AntiprotonCoolingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        FluidStack inputFluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        int energyCost = buffer.readInt();
        int processTime = buffer.readInt();
        FluidStack outputFluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        ItemStack outputCell = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        return new AntiprotonCoolingRecipe(recipeTypeSupplier.get(), this, inputFluid, energyCost, processTime, outputFluid, outputCell);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, AntiprotonCoolingRecipe recipe) {
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.inputFluid());
        buffer.writeInt(recipe.energyCost());
        buffer.writeInt(recipe.processTime());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.outputFluid());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.outputCell());
    }
}

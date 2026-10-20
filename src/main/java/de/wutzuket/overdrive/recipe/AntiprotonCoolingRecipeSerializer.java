package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.wutzuket.overdrive.antimatter.AntimatterStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

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
                        AntimatterStack.MAP_CODEC.fieldOf("input_antimatter").forGetter(AntiprotonCoolingRecipe::inputAntimatter),
                        Codec.INT.fieldOf("energy_cost").forGetter(AntiprotonCoolingRecipe::energyCost),
                        Codec.INT.fieldOf("process_time").forGetter(AntiprotonCoolingRecipe::processTime),
                        AntimatterStack.MAP_CODEC.fieldOf("output_antimatter").forGetter(AntiprotonCoolingRecipe::outputAntimatter),
                        ItemStack.CODEC.optionalFieldOf("output_cell", ItemStack.EMPTY).forGetter(AntiprotonCoolingRecipe::outputCell)
                ).apply(instance, (inputAntimatter, energyCost, processTime, outputAntimatter, outputCell) ->
                        new AntiprotonCoolingRecipe(typeSupplier.get(), serializer, inputAntimatter, energyCost, processTime, outputAntimatter, outputCell))
        );
    }

    @Override
    public MapCodec<AntiprotonCoolingRecipe> codec() { return codec; }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AntiprotonCoolingRecipe> streamCodec() { return streamCodec; }

    private AntiprotonCoolingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        AntimatterStack inputAntimatter = AntimatterStack.STREAM_CODEC.decode(buffer);
        int energyCost = buffer.readInt();
        int processTime = buffer.readInt();
        AntimatterStack outputAntimatter = AntimatterStack.STREAM_CODEC.decode(buffer);
        ItemStack outputCell = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        return new AntiprotonCoolingRecipe(recipeTypeSupplier.get(), this, inputAntimatter, energyCost, processTime, outputAntimatter, outputCell);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, AntiprotonCoolingRecipe recipe) {
        AntimatterStack.STREAM_CODEC.encode(buffer, recipe.inputAntimatter());
        buffer.writeInt(recipe.energyCost());
        buffer.writeInt(recipe.processTime());
        AntimatterStack.STREAM_CODEC.encode(buffer, recipe.outputAntimatter());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.outputCell());
    }
}

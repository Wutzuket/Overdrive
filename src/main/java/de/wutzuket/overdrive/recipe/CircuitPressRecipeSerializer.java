package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public final class CircuitPressRecipeSerializer implements RecipeSerializer<CircuitPressRecipe> {
    private static final MapCodec<CircuitPressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(CircuitPressRecipe::ingredients),
            ItemStack.CODEC.fieldOf("output").forGetter(CircuitPressRecipe::output),
            Codec.INT.optionalFieldOf("energy_per_tick", 0).forGetter(CircuitPressRecipe::energyPerTick),
            Codec.INT.optionalFieldOf("process_time", 0).forGetter(CircuitPressRecipe::processTime)
    ).apply(instance, CircuitPressRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CircuitPressRecipe> STREAM = StreamCodec.of(
            CircuitPressRecipeSerializer::toNetwork, CircuitPressRecipeSerializer::fromNetwork);

    private static CircuitPressRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Ingredient> ingredients = java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> Ingredient.CONTENTS_STREAM_CODEC.decode(buf)).toList();
        return new CircuitPressRecipe(ingredients, ItemStack.STREAM_CODEC.decode(buf), buf.readVarInt(), buf.readVarInt());
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, CircuitPressRecipe recipe) {
        buf.writeVarInt(recipe.ingredients().size());
        recipe.ingredients().forEach(i -> Ingredient.CONTENTS_STREAM_CODEC.encode(buf, i));
        ItemStack.STREAM_CODEC.encode(buf, recipe.output());
        buf.writeVarInt(recipe.energyPerTick());
        buf.writeVarInt(recipe.processTime());
    }

    @Override public MapCodec<CircuitPressRecipe> codec() { return CODEC; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, CircuitPressRecipe> streamCodec() { return STREAM; }
}

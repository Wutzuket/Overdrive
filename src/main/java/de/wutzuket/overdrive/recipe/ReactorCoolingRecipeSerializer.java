package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class ReactorCoolingRecipeSerializer implements RecipeSerializer<ReactorCoolingRecipe> {
    private static final MapCodec<ReactorCoolingRecipe> CODEC = RecordCodecBuilder.mapCodec(i-> i.group(
            FluidStack.CODEC.fieldOf("input").forGetter(ReactorCoolingRecipe::input),
            FluidStack.CODEC.fieldOf("output").forGetter(ReactorCoolingRecipe::output),
            Codec.INT.fieldOf("mb_per_100k").forGetter(ReactorCoolingRecipe::mbPer100K),
            Codec.DOUBLE.fieldOf("cooling_per_mb").forGetter(ReactorCoolingRecipe::coolingPerMb),
            Codec.INT.fieldOf("boiling_temp").forGetter(ReactorCoolingRecipe::boilingTemp),
            Codec.INT.fieldOf("steam_per_mb").forGetter(ReactorCoolingRecipe::steamPerMb),
            Codec.INT.optionalFieldOf("max_mb_per_tick", Integer.MAX_VALUE).forGetter(ReactorCoolingRecipe::maxMbPerTick)
    ).apply(i, ReactorCoolingRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, ReactorCoolingRecipe> STREAM = StreamCodec.of(
            (buf, r)->{
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.input());
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.output());
                buf.writeInt(r.mbPer100K()); buf.writeDouble(r.coolingPerMb()); buf.writeInt(r.boilingTemp()); buf.writeInt(r.steamPerMb()); buf.writeInt(r.maxMbPerTick());
            },
            buf-> new ReactorCoolingRecipe(
                    FluidStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    FluidStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    buf.readInt(), buf.readDouble(), buf.readInt(), buf.readInt(), buf.readInt()
            )
    );
    @Override public MapCodec<ReactorCoolingRecipe> codec(){ return CODEC; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ReactorCoolingRecipe> streamCodec(){ return STREAM; }
}

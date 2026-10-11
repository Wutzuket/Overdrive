package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class CoolingTowerRecipeSerializer implements RecipeSerializer<CoolingTowerRecipe> {
    private static final MapCodec<CoolingTowerRecipe> CODEC=RecordCodecBuilder.mapCodec(inst-> inst.group(
            FluidStack.CODEC.fieldOf("input_fluid").forGetter(CoolingTowerRecipe::inputFluid),
            FluidStack.CODEC.fieldOf("output_fluid").forGetter(CoolingTowerRecipe::outputFluid),
            Codec.INT.fieldOf("process_time").forGetter(CoolingTowerRecipe::processTime)
    ).apply(inst, CoolingTowerRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, CoolingTowerRecipe> STREAM=StreamCodec.of(CoolingTowerRecipeSerializer::toNetwork, CoolingTowerRecipeSerializer::fromNetwork);
    @Override public MapCodec<CoolingTowerRecipe> codec(){ return CODEC; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, CoolingTowerRecipe> streamCodec(){ return STREAM; }
    private static CoolingTowerRecipe fromNetwork(RegistryFriendlyByteBuf buf){
        FluidStack in=FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        FluidStack out=FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        int t=buf.readInt();
        return new CoolingTowerRecipe(in,out,t);
    }
    private static void toNetwork(RegistryFriendlyByteBuf buf, CoolingTowerRecipe r){
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.inputFluid());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.outputFluid());
        buf.writeInt(r.processTime());
    }
}

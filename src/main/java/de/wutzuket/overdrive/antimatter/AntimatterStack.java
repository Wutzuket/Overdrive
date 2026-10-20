package de.wutzuket.overdrive.antimatter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * A fixed amount of one type of antimatter. It replaces the fluid stacks of the machines, so the
 * recipes work with the antimatter types instead of the old fluids.
 *
 * @param type   the type of antimatter
 * @param amount the amount of antimatter units (1 unit = 1 mB)
 */
public record AntimatterStack(Antimatter type, int amount) {

    /** Der leere Antimatter-Stapel (ohne Typ). */
    public static final AntimatterStack EMPTY = new AntimatterStack(null, 0);

    /** Der Codec wird von JEI fuer die Ingredient-Registrierung gebraucht. */
    public static final Codec<AntimatterStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Antimatter.CODEC.fieldOf("type").forGetter(AntimatterStack::type),
            Codec.INT.fieldOf("amount").forGetter(AntimatterStack::amount)
    ).apply(instance, AntimatterStack::new));

    /** Das MapCodec wird fuer die Rezept-Serialisierung benoetigt. */
    public static final MapCodec<AntimatterStack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Antimatter.CODEC.fieldOf("type").forGetter(AntimatterStack::type),
            Codec.INT.fieldOf("amount").forGetter(AntimatterStack::amount)
    ).apply(instance, AntimatterStack::new));

    public static final StreamCodec<ByteBuf, AntimatterStack> STREAM_CODEC = StreamCodec.composite(
            Antimatter.STREAM_CODEC, AntimatterStack::type,
            ByteBufCodecs.VAR_INT, AntimatterStack::amount,
            AntimatterStack::new);

    public AntimatterStack {
        amount = Math.max(0, amount);
    }

    public boolean isEmpty() {
        return type == null || amount <= 0;
    }

    public AntimatterStack withAmount(int newAmount) {
        if (isEmpty() || newAmount <= 0) {
            return EMPTY;
        }
        return new AntimatterStack(type, newAmount);
    }

    /** @return eine Kopie dieses Stapels. */
    public AntimatterStack copy() {
        return isEmpty() ? EMPTY : new AntimatterStack(type, amount);
    }

    /** @return true, wenn beide Stapel vom gleichen Typ sind (die Menge wird ignoriert). */
    public boolean isSameType(@Nullable AntimatterStack other) {
        return other != null && type != null && type.equals(other.type());
    }
}

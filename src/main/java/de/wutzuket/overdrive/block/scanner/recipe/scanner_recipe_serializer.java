package de.wutzuket.overdrive.block.scanner.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serialisiert die Scanner-Rezepte aus den Datapacks.
 */
public class scanner_recipe_serializer implements RecipeSerializer<scanner_recipe> {
    /**
     * Das Pattern besteht aus drei Layern mit je drei Reihen aus je drei Zeichen.
     */
    private static final Codec<String[][][]> PATTERN_CODEC = Codec.STRING.listOf().listOf().flatXmap(
            rows -> {
                if (rows.size() != 3) {
                    return DataResult.error(() -> "Scanner pattern must contain exactly 3 layers");
                }

                String[][][] pattern = new String[3][3][3];
                for (int layer = 0; layer < 3; layer++) {
                    List<String> layerRows = rows.get(layer);
                    if (layerRows.size() != 3) {
                        int layerIndex = layer;
                        return DataResult.error(() -> "Scanner pattern layer " + layerIndex + " must contain exactly 3 rows");
                    }

                    for (int row = 0; row < 3; row++) {
                        String rowString = layerRows.get(row);
                        if (rowString.length() != 3) {
                            int layerIndex = layer;
                            int rowIndex = row;
                            return DataResult.error(() -> "Scanner pattern row " + layerIndex + ":" + rowIndex
                                    + " must be exactly 3 characters long");
                        }

                        for (int col = 0; col < 3; col++) {
                            pattern[layer][row][col] = String.valueOf(rowString.charAt(col));
                        }
                    }
                }
                return DataResult.success(pattern);
            },
            pattern -> {
                List<List<String>> rows = new ArrayList<>(3);
                for (String[][] layer : pattern) {
                    List<String> layerRows = new ArrayList<>(3);
                    for (String[] row : layer) {
                        layerRows.add(String.join("", row));
                    }
                    rows.add(layerRows);
                }
                return DataResult.success(rows);
            });

    private static final MapCodec<scanner_recipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            scanner_result.CODEC.fieldOf("result").forGetter(scanner_recipe::result),
            Codec.INT.optionalFieldOf("energy", 0).forGetter(scanner_recipe::energy),
            Codec.BOOL.optionalFieldOf("accept_mirrored", true).forGetter(scanner_recipe::acceptMirrored),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(scanner_recipe::showNotification),
            Codec.unboundedMap(Codec.STRING, scanner_block_matcher.CODEC).fieldOf("key").forGetter(scanner_recipe::key),
            PATTERN_CODEC.fieldOf("pattern").forGetter(scanner_recipe::pattern)
    ).apply(inst, scanner_recipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, scanner_recipe> STREAM_CODEC = StreamCodec.of(
            scanner_recipe_serializer::toNetwork, scanner_recipe_serializer::fromNetwork);

    @Override
    public MapCodec<scanner_recipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, scanner_recipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, scanner_recipe recipe) {
        ResourceLocation.STREAM_CODEC.encode(buf, recipe.resultId());
        buf.writeVarInt(recipe.resultCount());
        buf.writeVarInt(recipe.energy());
        buf.writeBoolean(recipe.acceptMirrored());
        buf.writeBoolean(recipe.showNotification());

        Map<String, scanner_block_matcher> key = recipe.key();
        buf.writeVarInt(key.size());
        for (Map.Entry<String, scanner_block_matcher> entry : key.entrySet()) {
            ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
            scanner_block_matcher.STREAM_CODEC.encode(buf, entry.getValue());
        }

        for (int layer = 0; layer < 3; layer++) {
            for (int row = 0; row < 3; row++) {
                StringBuilder builder = new StringBuilder(3);
                for (int col = 0; col < 3; col++) {
                    builder.append(recipe.symbolAt(layer, row, col));
                }
                ByteBufCodecs.STRING_UTF8.encode(buf, builder.toString());
            }
        }
    }

    private static scanner_recipe fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation resultId = ResourceLocation.STREAM_CODEC.decode(buf);
        int resultCount = buf.readVarInt();
        int energy = buf.readVarInt();
        boolean acceptMirrored = buf.readBoolean();
        boolean showNotification = buf.readBoolean();

        int keySize = buf.readVarInt();
        Map<String, scanner_block_matcher> key = new java.util.TreeMap<>();
        for (int i = 0; i < keySize; i++) {
            String symbol = ByteBufCodecs.STRING_UTF8.decode(buf);
            key.put(symbol, scanner_block_matcher.STREAM_CODEC.decode(buf));
        }

        String[][][] pattern = new String[3][3][3];
        for (int layer = 0; layer < 3; layer++) {
            for (int row = 0; row < 3; row++) {
                String rowString = ByteBufCodecs.STRING_UTF8.decode(buf);
                for (int col = 0; col < 3; col++) {
                    pattern[layer][row][col] = String.valueOf(rowString.charAt(col));
                }
            }
        }

        return new scanner_recipe(new scanner_result(resultId, resultCount), energy, acceptMirrored, showNotification, key, pattern);
    }
}

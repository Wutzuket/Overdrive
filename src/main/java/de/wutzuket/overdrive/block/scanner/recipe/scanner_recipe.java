package de.wutzuket.overdrive.block.scanner.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class scanner_recipe {
    private final ResourceLocation id;
    private final ResourceLocation resultId;
    private final int resultCount;
    private final int energy;
    private final boolean acceptMirrored;
    private final Map<String, scanner_block_matcher> key;
    private final String[][][] pattern;

    public scanner_recipe(ResourceLocation id, ResourceLocation resultId, int resultCount, int energy, boolean acceptMirrored,
                          Map<String, scanner_block_matcher> key, String[][][] pattern) {
        this.id = id;
        this.resultId = resultId;
        this.resultCount = resultCount;
        this.energy = energy;
        this.acceptMirrored = acceptMirrored;
        this.key = key;
        this.pattern = pattern;
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceLocation resultId() {
        return resultId;
    }

    public int resultCount() {
        return resultCount;
    }

    public int energy() {
        return energy;
    }

    public boolean acceptMirrored() {
        return acceptMirrored;
    }

    public int layerCount() {
        return pattern.length;
    }

    public Map<String, scanner_block_matcher> key() {
        return key;
    }

    public String[][][] pattern() {
        return pattern;
    }

    public String symbolAt(int layer, int row, int col) {
        return pattern[layer][row][col];
    }

    public scanner_block_matcher getMatcher(int layer, int row, int col) {
        String symbol = symbolAt(layer, row, col);
        scanner_block_matcher matcher = key.get(symbol);
        if (matcher == null) {
            throw new IllegalStateException("Unknown scanner recipe symbol '" + symbol + "' in recipe " + id);
        }
        return matcher;
    }

    public ItemStack getResultStack() {
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(resultId).orElseThrow(() ->
                new IllegalStateException("Unknown scanner result item: " + resultId)), resultCount);
    }

    public List<ItemStack> getPreviewIngredientStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (scanner_block_matcher matcher : key.values()) {
            for (ItemStack stack : matcher.displayStacks()) {
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    public List<ItemStack> getMaterialCosts() {
        Map<Item, Integer> counts = new LinkedHashMap<>();
        for (int layer = 0; layer < pattern.length; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (!isActiveCell(layer, row, col)) {
                        continue;
                    }

                    List<ItemStack> stacks = getMatcher(layer, row, col).displayStacks();
                    if (stacks.isEmpty()) {
                        continue;
                    }

                    ItemStack displayStack = stacks.getFirst();
                    counts.merge(displayStack.getItem(), 1, Integer::sum);
                }
            }
        }

        List<ItemStack> costs = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
            costs.add(new ItemStack(entry.getKey(), entry.getValue()));
        }
        return costs;
    }

    public boolean isActiveCell(int layer, int row, int col) {
        String symbol = pattern[layer][row][col];
        return !symbol.isBlank() && !symbol.equals("*");
    }

    public BlockPos getCenter(@NotNull BlockPos scannerPos, @NotNull BlockState scannerState) {
        Direction scannerFacing = scannerState.getValue(BlockStateProperties.FACING);
        return scannerPos.relative(scannerFacing, 3).above(1);
    }

    public BlockPos getCellPos(@NotNull BlockPos scannerPos, @NotNull BlockState scannerState, int layer, int row, int col, boolean mirrored) {
        return getCellPos(scannerPos, scannerState, layer, row, col, mirrored, scannerState.getValue(BlockStateProperties.FACING));
    }

    public BlockPos getCellPos(@NotNull BlockPos scannerPos, @NotNull BlockState scannerState, int layer, int row, int col, boolean mirrored, @NotNull Direction patternFacing) {
        Vec3i patternForward = patternFacing.getNormal();
        Vec3i patternRight = mirrored ? patternFacing.getCounterClockWise().getNormal() : patternFacing.getClockWise().getNormal();
        BlockPos center = getCenter(scannerPos, scannerState);

        return center.offset(
                patternRight.getX() * (col - 1) + patternForward.getX() * (row - 1),
                layer - 1,
                patternRight.getZ() * (col - 1) + patternForward.getZ() * (row - 1)
        );
    }

    public static scanner_recipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        String type = GsonHelper.getAsString(json, "type", "overdrive:scanner");
        if (!type.equals("overdrive:scanner")) {
            throw new IllegalArgumentException("Unsupported scanner recipe type: " + type);
        }

        JsonObject resultObject = GsonHelper.getAsJsonObject(json, "result");
        ResourceLocation resultId = ResourceLocation.parse(GsonHelper.getAsString(resultObject, "id"));
        int resultCount = GsonHelper.getAsInt(resultObject, "count", 1);
        int energy = GsonHelper.getAsInt(json, "energy", 0);
        boolean acceptMirrored = GsonHelper.getAsBoolean(json, "accept_mirrored", true);
        Map<String, scanner_block_matcher> key = parseKey(GsonHelper.getAsJsonObject(json, "key"));
        String[][][] pattern = parsePattern(GsonHelper.getAsJsonArray(json, "pattern"));
        return new scanner_recipe(id, resultId, resultCount, energy, acceptMirrored, key, pattern);
    }

    private static Map<String, scanner_block_matcher> parseKey(JsonObject keyJson) {
        Map<String, scanner_block_matcher> key = new HashMap<>();
        for (String symbol : keyJson.keySet()) {
            key.put(symbol, scanner_block_matcher.parse(keyJson.get(symbol)));
        }
        return key;
    }

    private static String[][][] parsePattern(JsonArray patternJson) {
        if (patternJson.size() != 3) {
            throw new IllegalArgumentException("Scanner pattern must contain exactly 3 layers");
        }

        String[][][] pattern = new String[3][3][3];
        for (int layer = 0; layer < 3; layer++) {
            JsonArray rows = patternJson.get(layer).getAsJsonArray();
            if (rows.size() != 3) {
                throw new IllegalArgumentException("Scanner pattern layer " + layer + " must contain exactly 3 rows");
            }

            for (int row = 0; row < 3; row++) {
                String rowString = GsonHelper.convertToString(rows.get(row), "pattern[" + layer + "][" + row + "]");
                if (rowString.length() != 3) {
                    throw new IllegalArgumentException("Scanner pattern row " + layer + ":" + row + " must be exactly 3 characters long");
                }

                for (int col = 0; col < 3; col++) {
                    pattern[layer][row][col] = String.valueOf(rowString.charAt(col));
                }
            }
        }
        return pattern;
    }

    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState) {
        return matches(level, scannerPos, scannerState, scannerState.getValue(BlockStateProperties.FACING));
    }

    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, @NotNull Direction facing) {
        return matches(level, scannerPos, scannerState, facing, false)
                || (acceptMirrored && matches(level, scannerPos, scannerState, facing, true));
    }

    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, @NotNull Direction facing, boolean mirrored) {
        return matchesFacing(level, scannerPos, scannerState, facing, mirrored);
    }

    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, boolean mirrored) {
        return matchesFacing(level, scannerPos, scannerState, scannerState.getValue(BlockStateProperties.FACING), mirrored);
    }

    private boolean matchesFacing(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, @NotNull Direction facing, boolean mirrored) {
        for (int layer = 0; layer < 3; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (!isActiveCell(layer, row, col)) {
                        continue;
                    }

                    String symbol = pattern[layer][row][col];
                    scanner_block_matcher matcher = key.get(symbol);
                    if (matcher == null) {
                        throw new IllegalStateException("Unknown scanner recipe symbol '" + symbol + "' in recipe " + id);
                    }

                    BlockPos target = getCellPos(scannerPos, scannerState, layer, row, col, mirrored, facing);

                    if (!matcher.matches(level.getBlockState(target))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void consume(@NotNull ServerLevel level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, @NotNull Direction facing, boolean mirrored) {
        ItemStack output = getResultStack();
        Vec3 dropPos = Vec3.atCenterOf(getCenter(scannerPos, scannerState));
        level.addFreshEntity(new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, output));
    }

    public void consume(@NotNull ServerLevel level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, boolean mirrored) {
        consume(level, scannerPos, scannerState, scannerState.getValue(BlockStateProperties.FACING), mirrored);
    }
}

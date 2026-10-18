package de.wutzuket.overdrive.block.scanner.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface scanner_block_matcher {
    boolean matches(@NotNull BlockState state);

    default @NotNull List<ItemStack> displayStacks() {
        return List.of();
    }

    default @NotNull BlockState displayState() {
        List<ItemStack> stacks = displayStacks();
        if (stacks.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }

        if (stacks.getFirst().getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }

        return Blocks.AIR.defaultBlockState();
    }

    static scanner_block_matcher parse(@NotNull JsonElement element) {
        if (element.isJsonPrimitive()) {
            return parse(element.getAsString());
        }

        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Scanner key entry must be a string or object");
        }

        JsonObject object = element.getAsJsonObject();
        if (object.has("tag")) {
            return parse("#" + object.get("tag").getAsString());
        }
        if (object.has("block")) {
            return parse(object.get("block").getAsString());
        }
        throw new IllegalArgumentException("Scanner key entry object must contain minecraft block or tag");
    }

    static scanner_block_matcher parse(@NotNull String rawValue) {
        String value = rawValue.trim();
        if (value.isEmpty() || value.equals("*")) {
            return new scanner_block_matcher() {
                @Override
                public boolean matches(@NotNull BlockState state) {
                    return true;
                }
            };
        }

        if (value.startsWith("#")) {
            ResourceLocation tagLocation = ResourceLocation.parse(value.substring(1));
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagLocation);
            return new scanner_block_matcher() {
                private List<ItemStack> displayStacks;

                @Override
                public boolean matches(@NotNull BlockState state) {
                    return state.is(tagKey);
                }

                @Override
                public @NotNull List<ItemStack> displayStacks() {
                    if (displayStacks == null || displayStacks.isEmpty()) {
                        displayStacks = BuiltInRegistries.BLOCK.getTag(tagKey)
                                .map(tag -> tag.stream()
                                        .map(Holder::value)
                                        .map(Block::asItem)
                                        .map(ItemStack::new)
                                        .filter(stack -> !stack.isEmpty())
                                        .toList())
                                .orElse(List.of());
                    }
                    return displayStacks;
                }
            };
        }

        ResourceLocation blockId = ResourceLocation.parse(value);
        Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + value);
        }

        ItemStack displayStack = new ItemStack(block.asItem());
        return new scanner_block_matcher() {
            @Override
            public boolean matches(@NotNull BlockState state) {
                return state.is(block);
            }

            @Override
            public @NotNull List<ItemStack> displayStacks() {
                return displayStack.isEmpty() ? List.of() : List.of(displayStack);
            }
        };
    }
}

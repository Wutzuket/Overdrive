package de.wutzuket.overdrive.block.scanner.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public interface scanner_block_matcher {
    Logger LOGGER = LogUtils.getLogger();

    boolean matches(@NotNull BlockState state);

    /**
     * Rohwert des Matchers, wie er in der Rezeptdatei steht. Ein Block wird ohne
     * Praefix geschrieben ({@code minecraft:stone}), ein Tag mit fuehrender Raute
     * ({@code #minecraft:logs}) und der Platzhalter mit {@code *}.
     */
    @NotNull String rawValue();

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

    /**
     * Objektform eines Matchers: {@code {"block": "minecraft:stone"}}.
     */
    Codec<scanner_ref> BLOCK_REF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("block").forGetter(scanner_ref::id)
    ).apply(inst, id -> new scanner_ref(id, false)));

    /**
     * Objektform eines Matchers: {@code {"tag": "c:storage_blocks/aluminum"}}.
     */
    Codec<scanner_ref> TAG_REF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("tag").forGetter(scanner_ref::id)
    ).apply(inst, id -> new scanner_ref(id, true)));

    /**
     * Rohform eines Matchers: {@code "minecraft:stone"}, {@code "#minecraft:logs"} oder {@code "*"}.
     */
    Codec<scanner_block_matcher> RAW_CODEC = Codec.STRING.comapFlatMap(
            raw -> {
                try {
                    return DataResult.success(parse(raw));
                } catch (RuntimeException e) {
                    return DataResult.error(() -> "Invalid scanner matcher '" + raw + "': " + e.getMessage());
                }
            },
            scanner_block_matcher::rawValue);

    /**
     * Objektform eines Matchers, geschrieben als Block oder Tag.
     */
    Codec<scanner_block_matcher> OBJECT_CODEC = Codec.either(BLOCK_REF_CODEC, TAG_REF_CODEC).flatXmap(
            either -> {
                scanner_ref ref = either.left().isPresent() ? either.left().get() : either.right().get();
                return DataResult.success(ref.tag() ? tagMatcher(ref.id()) : blockMatcher(ref.id()));
            },
            scanner_block_matcher::encodeObject);

    /**
     * Codec eines Matchers fuer die Rezeptdateien. Die Objektform wird bevorzugt,
     * die Rohform dient als Alternative.
     */
    Codec<scanner_block_matcher> CODEC = Codec.withAlternative(OBJECT_CODEC, RAW_CODEC);

    StreamCodec<RegistryFriendlyByteBuf, scanner_block_matcher> STREAM_CODEC = StreamCodec.of(
            scanner_block_matcher::writeNetwork, scanner_block_matcher::readNetwork);

    static @NotNull scanner_block_matcher parse(@NotNull String rawValue) {
        String value = rawValue.trim();
        if (value.isEmpty() || value.equals("*")) {
            return new any_matcher();
        }

        if (value.startsWith("#")) {
            ResourceLocation tagLocation = ResourceLocation.parse(value.substring(1));
            return tagMatcher(tagLocation);
        }

        return blockMatcher(ResourceLocation.parse(value));
    }

    static @NotNull scanner_block_matcher tagMatcher(@NotNull ResourceLocation tagLocation) {
        TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagLocation);
        return new tag_matcher("#" + tagLocation, tagKey);
    }

    static @NotNull scanner_block_matcher blockMatcher(@NotNull ResourceLocation blockId) {
        return new block_matcher(blockId.toString(), blockId);
    }

    static @NotNull DataResult<Either<scanner_ref, scanner_ref>> encodeObject(@NotNull scanner_block_matcher matcher) {
        if (matcher instanceof block_matcher block) {
            return DataResult.success(Either.left(new scanner_ref(block.blockId(), false)));
        }
        if (matcher instanceof tag_matcher tag) {
            return DataResult.success(Either.right(new scanner_ref(tag.tagKey().location(), true)));
        }
        return DataResult.error(() -> "The wildcard matcher cannot be written as an object");
    }

    static void writeNetwork(@NotNull RegistryFriendlyByteBuf buf, @NotNull scanner_block_matcher matcher) {
        if (matcher instanceof block_matcher block) {
            buf.writeByte(1);
            ResourceLocation.STREAM_CODEC.encode(buf, block.blockId());
        } else if (matcher instanceof tag_matcher tag) {
            buf.writeByte(2);
            ResourceLocation.STREAM_CODEC.encode(buf, tag.tagKey().location());
        } else {
            buf.writeByte(0);
        }
    }

    static @NotNull scanner_block_matcher readNetwork(@NotNull RegistryFriendlyByteBuf buf) {
        int kind = buf.readByte();
        if (kind == 1) {
            return blockMatcher(ResourceLocation.STREAM_CODEC.decode(buf));
        }
        if (kind == 2) {
            return tagMatcher(ResourceLocation.STREAM_CODEC.decode(buf));
        }
        return new any_matcher();
    }

    /**
     * Referenz auf einen Block oder einen Tag, wie sie in der Rezeptdatei steht.
     */
    record scanner_ref(@NotNull ResourceLocation id, boolean tag) {
    }

    record any_matcher() implements scanner_block_matcher {
        @Override
        public boolean matches(@NotNull BlockState state) {
            return true;
        }

        @Override
        public @NotNull String rawValue() {
            return "*";
        }
    }

    static final class tag_matcher implements scanner_block_matcher {
        private final String rawValue;
        private final TagKey<Block> tagKey;
        private List<ItemStack> displayStacks;

        tag_matcher(@NotNull String rawValue, @NotNull TagKey<Block> tagKey) {
            this.rawValue = rawValue;
            this.tagKey = tagKey;
        }

        public @NotNull TagKey<Block> tagKey() {
            return tagKey;
        }

        @Override
        public boolean matches(@NotNull BlockState state) {
            return state.is(tagKey);
        }

        @Override
        public @NotNull String rawValue() {
            return rawValue;
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
    }

    static final class block_matcher implements scanner_block_matcher {
        private final String rawValue;
        private final ResourceLocation blockId;
        private Block block;
        private boolean resolved;

        block_matcher(@NotNull String rawValue, @NotNull ResourceLocation blockId) {
            this.rawValue = rawValue;
            this.blockId = blockId;
        }

        public @NotNull ResourceLocation blockId() {
            return blockId;
        }

        private @Nullable Block block() {
            if (!resolved) {
                resolved = true;
                if (BuiltInRegistries.BLOCK.containsKey(blockId)) {
                    block = BuiltInRegistries.BLOCK.get(blockId);
                } else {
                    LOGGER.warn("Unknown block id '{}' in scanner recipe", blockId);
                }
            }
            return block;
        }

        @Override
        public boolean matches(@NotNull BlockState state) {
            Block target = block();
            return target != null && state.is(target);
        }

        @Override
        public @NotNull String rawValue() {
            return rawValue;
        }

        @Override
        public @NotNull List<ItemStack> displayStacks() {
            Block target = block();
            if (target == null) {
                return List.of();
            }
            ItemStack displayStack = new ItemStack(target.asItem());
            return displayStack.isEmpty() ? List.of() : List.of(displayStack);
        }
    }
}

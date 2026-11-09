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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Ein Eintrag in der {@code key}-Sektion eines Scanner-Rezepts. Ein Matcher
 * beschreibt entweder einen Block, einen Block-Tag, ein Item oder einen
 * Item-Tag. Items werden im Aufbau als gedroppte Items platziert oder in das
 * Scanner-GUI eingelegt.
 */
public interface scanner_block_matcher {
    Logger LOGGER = LogUtils.getLogger();

    /**
     * Ob dieser Matcher ein Item statt eines Blocks beschreibt.
     */
    boolean isItemMatcher();

    boolean matchesBlock(@NotNull BlockState state);

    boolean matchesItem(@NotNull ItemStack stack);

    /**
     * Rohwert des Matchers, wie er in der Rezeptdatei stehen kann. Ein Block
     * wird ohne Praefix geschrieben ({@code minecraft:stone}), ein Block-Tag mit
     * fuehrender Raute ({@code #minecraft:logs}), ein Item mit
     * {@code item:overdrive:basic_circuit}, ein Item-Tag mit
     * {@code item_tag:c:circuits/basic} und der Platzhalter mit {@code *}.
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
     * Objektform des Matchers fuer die Rezeptdatei. Der Platzhalter kann nicht
     * als Objekt geschrieben werden und nutzt die Rohform.
     */
    default @NotNull Optional<scanner_ref> toRef() {
        return Optional.empty();
    }

    enum Kind {
        BLOCK,
        BLOCK_TAG,
        ITEM,
        ITEM_TAG
    }

    /**
     * Referenz auf einen Block, ein Item oder den jeweiligen Tag.
     */
    record scanner_ref(@NotNull ResourceLocation id, @NotNull Kind kind) {
    }

    /**
     * Objektform eines Matchers: {@code {"block": "minecraft:stone"}}.
     */
    Codec<scanner_ref> BLOCK_REF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("block").forGetter(scanner_ref::id)
    ).apply(inst, id -> new scanner_ref(id, Kind.BLOCK)));

    /**
     * Objektform eines Matchers: {@code {"tag": "c:storage_blocks/aluminum"}}.
     */
    Codec<scanner_ref> BLOCK_TAG_REF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("tag").forGetter(scanner_ref::id)
    ).apply(inst, id -> new scanner_ref(id, Kind.BLOCK_TAG)));

    /**
     * Objektform eines Matchers: {@code {"item": "overdrive:basic_circuit"}}.
     */
    Codec<scanner_ref> ITEM_REF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(scanner_ref::id)
    ).apply(inst, id -> new scanner_ref(id, Kind.ITEM)));

    /**
     * Objektform eines Matchers: {@code {"item_tag": "c:circuits/basic"}}.
     */
    Codec<scanner_ref> ITEM_TAG_REF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("item_tag").forGetter(scanner_ref::id)
    ).apply(inst, id -> new scanner_ref(id, Kind.ITEM_TAG)));

    Codec<Either<Either<scanner_ref, scanner_ref>, Either<scanner_ref, scanner_ref>>> REF_EITHER_CODEC =
            Codec.either(
                    Codec.either(BLOCK_REF_CODEC, BLOCK_TAG_REF_CODEC),
                    Codec.either(ITEM_REF_CODEC, ITEM_TAG_REF_CODEC));

    /**
     * Kombiniert Block-, Block-Tag-, Item- und Item-Tag-Referenzen.
     */
    Codec<scanner_ref> REF_CODEC = REF_EITHER_CODEC.xmap(
            either -> {
                Either<scanner_ref, scanner_ref> inner = either.left().isPresent()
                        ? either.left().get()
                        : either.right().get();
                return inner.left().isPresent() ? inner.left().get() : inner.right().get();
            },
            ref -> switch (ref.kind()) {
                case BLOCK -> Either.left(Either.left(ref));
                case BLOCK_TAG -> Either.left(Either.right(ref));
                case ITEM -> Either.right(Either.left(ref));
                case ITEM_TAG -> Either.right(Either.right(ref));
            });

    /**
     * Objektform eines Matchers. Genau eines der Felder {@code block},
     * {@code tag}, {@code item} oder {@code item_tag} wird gesetzt.
     */
    Codec<scanner_block_matcher> OBJECT_CODEC = REF_CODEC.flatXmap(
            ref -> DataResult.success(fromRef(ref)),
            matcher -> matcher.toRef()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(
                            () -> "The wildcard matcher cannot be written as an object")));

    /**
     * Rohform-Codec. Er wird erst nach {@link #OBJECT_CODEC} ausprobiert und
     * deckt die Kurzschreibweise fuer Bloecke und Block-Tags ab.
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
     * Rohform eines Matchers. Die Objektform wird bevorzugt, die Rohform dient
     * als Alternative.
     */
    Codec<scanner_block_matcher> CODEC = Codec.withAlternative(OBJECT_CODEC, RAW_CODEC);

    StreamCodec<RegistryFriendlyByteBuf, scanner_block_matcher> STREAM_CODEC = StreamCodec.of(
            scanner_block_matcher::writeNetwork, scanner_block_matcher::readNetwork);

    static @NotNull scanner_block_matcher parse(@NotNull String rawValue) {
        String value = rawValue.trim();
        if (value.isEmpty() || value.equals("*")) {
            return new any_matcher();
        }

        if (value.startsWith("item_tag:")) {
            return itemTagMatcher(ResourceLocation.parse(value.substring("item_tag:".length())));
        }

        if (value.startsWith("item:")) {
            return itemMatcher(ResourceLocation.parse(value.substring("item:".length())));
        }

        if (value.startsWith("#")) {
            return tagMatcher(ResourceLocation.parse(value.substring(1)));
        }

        return blockMatcher(ResourceLocation.parse(value));
    }

    static @NotNull scanner_block_matcher fromRef(@NotNull scanner_ref ref) {
        return switch (ref.kind()) {
            case BLOCK -> blockMatcher(ref.id());
            case BLOCK_TAG -> tagMatcher(ref.id());
            case ITEM -> itemMatcher(ref.id());
            case ITEM_TAG -> itemTagMatcher(ref.id());
        };
    }

    static @NotNull scanner_block_matcher tagMatcher(@NotNull ResourceLocation tagLocation) {
        TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagLocation);
        return new tag_matcher("#" + tagLocation, tagKey);
    }

    static @NotNull scanner_block_matcher blockMatcher(@NotNull ResourceLocation blockId) {
        return new block_matcher(blockId.toString(), blockId);
    }

    static @NotNull scanner_block_matcher itemMatcher(@NotNull ResourceLocation itemId) {
        return new item_matcher("item:" + itemId, itemId);
    }

    static @NotNull scanner_block_matcher itemTagMatcher(@NotNull ResourceLocation tagLocation) {
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLocation);
        return new item_tag_matcher("item_tag:" + tagLocation, tagKey);
    }

    static void writeNetwork(@NotNull RegistryFriendlyByteBuf buf, @NotNull scanner_block_matcher matcher) {
        if (matcher instanceof block_matcher block) {
            buf.writeByte(1);
            ResourceLocation.STREAM_CODEC.encode(buf, block.blockId());
        } else if (matcher instanceof tag_matcher tag) {
            buf.writeByte(2);
            ResourceLocation.STREAM_CODEC.encode(buf, tag.tagKey().location());
        } else if (matcher instanceof item_matcher item) {
            buf.writeByte(3);
            ResourceLocation.STREAM_CODEC.encode(buf, item.itemId());
        } else if (matcher instanceof item_tag_matcher itemTag) {
            buf.writeByte(4);
            ResourceLocation.STREAM_CODEC.encode(buf, itemTag.tagKey().location());
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
        if (kind == 3) {
            return itemMatcher(ResourceLocation.STREAM_CODEC.decode(buf));
        }
        if (kind == 4) {
            return itemTagMatcher(ResourceLocation.STREAM_CODEC.decode(buf));
        }
        return new any_matcher();
    }

    record any_matcher() implements scanner_block_matcher {
        @Override
        public boolean isItemMatcher() {
            return false;
        }

        @Override
        public boolean matchesBlock(@NotNull BlockState state) {
            return true;
        }

        @Override
        public boolean matchesItem(@NotNull ItemStack stack) {
            return false;
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
        public boolean isItemMatcher() {
            return false;
        }

        @Override
        public boolean matchesBlock(@NotNull BlockState state) {
            return state.is(tagKey);
        }

        @Override
        public boolean matchesItem(@NotNull ItemStack stack) {
            return false;
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

        @Override
        public @NotNull Optional<scanner_ref> toRef() {
            return Optional.of(new scanner_ref(tagKey.location(), Kind.BLOCK_TAG));
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
        public boolean isItemMatcher() {
            return false;
        }

        @Override
        public boolean matchesBlock(@NotNull BlockState state) {
            Block target = block();
            return target != null && state.is(target);
        }

        @Override
        public boolean matchesItem(@NotNull ItemStack stack) {
            return false;
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

        @Override
        public @NotNull Optional<scanner_ref> toRef() {
            return Optional.of(new scanner_ref(blockId, Kind.BLOCK));
        }
    }

    static final class item_matcher implements scanner_block_matcher {
        private final String rawValue;
        private final ResourceLocation itemId;
        private Item item;
        private boolean resolved;

        item_matcher(@NotNull String rawValue, @NotNull ResourceLocation itemId) {
            this.rawValue = rawValue;
            this.itemId = itemId;
        }

        public @NotNull ResourceLocation itemId() {
            return itemId;
        }

        private @Nullable Item item() {
            if (!resolved) {
                resolved = true;
                if (BuiltInRegistries.ITEM.containsKey(itemId)) {
                    item = BuiltInRegistries.ITEM.get(itemId);
                } else {
                    LOGGER.warn("Unknown item id '{}' in scanner recipe", itemId);
                }
            }
            return item;
        }

        @Override
        public boolean isItemMatcher() {
            return true;
        }

        @Override
        public boolean matchesBlock(@NotNull BlockState state) {
            return false;
        }

        @Override
        public boolean matchesItem(@NotNull ItemStack stack) {
            Item target = item();
            return target != null && !stack.isEmpty() && stack.is(target);
        }

        @Override
        public @NotNull String rawValue() {
            return rawValue;
        }

        @Override
        public @NotNull List<ItemStack> displayStacks() {
            Item target = item();
            return target == null ? List.of() : List.of(new ItemStack(target));
        }

        @Override
        public @NotNull Optional<scanner_ref> toRef() {
            return Optional.of(new scanner_ref(itemId, Kind.ITEM));
        }
    }

    static final class item_tag_matcher implements scanner_block_matcher {
        private final String rawValue;
        private final TagKey<Item> tagKey;
        private List<ItemStack> displayStacks;

        item_tag_matcher(@NotNull String rawValue, @NotNull TagKey<Item> tagKey) {
            this.rawValue = rawValue;
            this.tagKey = tagKey;
        }

        public @NotNull TagKey<Item> tagKey() {
            return tagKey;
        }

        @Override
        public boolean isItemMatcher() {
            return true;
        }

        @Override
        public boolean matchesBlock(@NotNull BlockState state) {
            return false;
        }

        @Override
        public boolean matchesItem(@NotNull ItemStack stack) {
            return !stack.isEmpty() && stack.is(tagKey);
        }

        @Override
        public @NotNull String rawValue() {
            return rawValue;
        }

        @Override
        public @NotNull List<ItemStack> displayStacks() {
            if (displayStacks == null || displayStacks.isEmpty()) {
                displayStacks = BuiltInRegistries.ITEM.getTag(tagKey)
                        .map(tag -> tag.stream()
                                .map(Holder::value)
                                .map(ItemStack::new)
                                .filter(stack -> !stack.isEmpty())
                                .toList())
                        .orElse(List.of());
            }
            return displayStacks;
        }

        @Override
        public @NotNull Optional<scanner_ref> toRef() {
            return Optional.of(new scanner_ref(tagKey.location(), Kind.ITEM_TAG));
        }
    }
}

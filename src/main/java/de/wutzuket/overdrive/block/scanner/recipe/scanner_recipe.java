package de.wutzuket.overdrive.block.scanner.recipe;

import de.wutzuket.overdrive.recipe.ODRecipeSerializers;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ein Scanner-Rezept. Der Rezepttyp lautet {@code overdrive:scanner}, die Dateien
 * liegen unter {@code data/overdrive/recipe/scanner/}.
 */
public final class scanner_recipe implements Recipe<RecipeInput> {
    private final scanner_result result;
    private final int energy;
    private final boolean acceptMirrored;
    private final boolean showNotification;
    private final Map<String, scanner_block_matcher> key;
    private final String[][][] pattern;

    public scanner_recipe(scanner_result result, int energy, boolean acceptMirrored, boolean showNotification,
                          Map<String, scanner_block_matcher> key, String[][][] pattern) {
        this.result = result;
        this.energy = energy;
        this.acceptMirrored = acceptMirrored;
        this.showNotification = showNotification;
        this.key = new TreeMap<>(key);
        this.pattern = pattern;
    }

    public scanner_result result() {
        return result;
    }

    public ResourceLocation resultId() {
        return result.id();
    }

    public int resultCount() {
        return result.count();
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
            throw new IllegalStateException("Unknown scanner recipe symbol '" + symbol + "'");
        }
        return matcher;
    }

    public ItemStack getResultStack() {
        return getResultStack(null);
    }

    public ItemStack getResultStack(@Nullable HolderLookup.Provider registries) {
        Item item = resolveResultItem(registries);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, result.count());
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

    /**
     * Die Item-Zutaten dieses Rezepts, jeweils mit der Anzahl der Zellen, die
     * diesen Matcher verwenden. Diese Items werden im Aufbau platziert oder in
     * das Scanner-GUI eingelegt.
     */
    public List<ItemStack> getPreviewItemStacks() {
        Map<Item, Integer> counts = new LinkedHashMap<>();
        for (int layer = 0; layer < pattern.length; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (!isActiveCell(layer, row, col)) {
                        continue;
                    }
                    scanner_block_matcher matcher = getMatcher(layer, row, col);
                    if (!matcher.isItemMatcher()) {
                        continue;
                    }
                    List<ItemStack> stacks = matcher.displayStacks();
                    if (!stacks.isEmpty()) {
                        counts.merge(stacks.getFirst().getItem(), 1, Integer::sum);
                    }
                }
            }
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
            stacks.add(new ItemStack(entry.getKey(), Math.min(entry.getValue(), 64)));
        }
        return stacks;
    }

    public boolean hasItemRequirements() {
        for (int layer = 0; layer < pattern.length; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (isActiveCell(layer, row, col) && getMatcher(layer, row, col).isItemMatcher()) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    @Override
    public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
        return true;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput input, @NotNull HolderLookup.Provider registries) {
        return getResultStack(registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return getResultStack(registries);
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ODRecipeSerializers.SCANNER_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ODRecipeTypes.SCANNER.get();
    }

    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState) {
        return matches(level, scannerPos, scannerState, scannerState.getValue(BlockStateProperties.FACING), false, null, null);
    }

    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, @NotNull Direction facing) {
        return matches(level, scannerPos, scannerState, facing, false, null, null)
                || (acceptMirrored && matches(level, scannerPos, scannerState, facing, true, null, null));
    }

    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, @NotNull Direction facing, boolean mirrored) {
        return matches(level, scannerPos, scannerState, facing, mirrored, null, null);
    }

    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, boolean mirrored) {
        return matches(level, scannerPos, scannerState, scannerState.getValue(BlockStateProperties.FACING), mirrored, null, null);
    }

    /**
     * Prueft den Aufbau inklusive der Item-Zutaten. Items koennen als
     * gedroppte Items in den Zellen des Aufbaus liegen oder im Scanner-Inventar
     * bereitgestellt werden.
     */
    public boolean matches(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState,
                           @NotNull Direction facing, boolean mirrored,
                           @Nullable ItemStackHandler inventory, @Nullable List<ItemEntity> nearbyItems) {
        return matchesFacing(level, scannerPos, scannerState, facing, mirrored, inventory, nearbyItems);
    }

    private boolean matchesFacing(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState,
                                  @NotNull Direction facing, boolean mirrored,
                                  @Nullable ItemStackHandler inventory, @Nullable List<ItemEntity> nearbyItems) {
        List<ItemRequirement> itemRequirements = new ArrayList<>();

        for (int layer = 0; layer < 3; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (!isActiveCell(layer, row, col)) {
                        continue;
                    }

                    scanner_block_matcher matcher = getMatcher(layer, row, col);
                    BlockPos target = getCellPos(scannerPos, scannerState, layer, row, col, mirrored, facing);
                    BlockState state = level.getBlockState(target);

                    if (matcher.isItemMatcher()) {
                        // Item-Zellen muessen leer sein; das Item liegt auf dem
                        // Block darunter oder kommt aus dem Scanner-Inventar.
                        if (!state.isAir()) {
                            return false;
                        }
                        itemRequirements.add(new ItemRequirement(target, matcher));
                    } else if (!matcher.matchesBlock(state)) {
                        return false;
                    }
                }
            }
        }

        return resolveItemUses(level, itemRequirements, inventory, nearbyItems) != null;
    }

    private List<ItemRequirement> collectItemRequirements(@NotNull BlockPos scannerPos, @NotNull BlockState scannerState,
                                                          @NotNull Direction facing, boolean mirrored) {
        List<ItemRequirement> requirements = new ArrayList<>();
        for (int layer = 0; layer < 3; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (!isActiveCell(layer, row, col)) {
                        continue;
                    }
                    scanner_block_matcher matcher = getMatcher(layer, row, col);
                    if (!matcher.isItemMatcher()) {
                        continue;
                    }
                    requirements.add(new ItemRequirement(
                            getCellPos(scannerPos, scannerState, layer, row, col, mirrored, facing), matcher));
                }
            }
        }
        return requirements;
    }

    private @Nullable List<ItemUse> resolveItemUses(@NotNull Level level, @NotNull List<ItemRequirement> requirements,
                                                    @Nullable ItemStackHandler inventory,
                                                    @Nullable List<ItemEntity> nearbyItems) {
        if (requirements.isEmpty()) {
            return List.of();
        }

        Map<ItemEntity, Integer> usedEntities = new HashMap<>();
        int[] usedSlots = new int[inventory == null ? 0 : inventory.getSlots()];
        List<ItemUse> uses = new ArrayList<>();

        for (ItemRequirement requirement : requirements) {
            ItemEntity entity = findItemEntity(level, requirement, nearbyItems, usedEntities);
            if (entity != null) {
                usedEntities.merge(entity, 1, Integer::sum);
                uses.add(new ItemUse(entity, -1));
                continue;
            }

            int slot = findInventorySlot(inventory, requirement.matcher(), usedSlots);
            if (slot < 0) {
                return null;
            }
            usedSlots[slot]++;
            uses.add(new ItemUse(null, slot));
        }

        return uses;
    }

    private @Nullable ItemEntity findItemEntity(@NotNull Level level, @NotNull ItemRequirement requirement,
                                                @Nullable List<ItemEntity> nearbyItems,
                                                @NotNull Map<ItemEntity, Integer> usedEntities) {
        AABB cell = new AABB(requirement.pos());

        if (nearbyItems != null) {
            for (ItemEntity entity : nearbyItems) {
                if (!entity.isAlive() || !entity.getBoundingBox().intersects(cell)) {
                    continue;
                }
                if (usedEntities.getOrDefault(entity, 0) >= entity.getItem().getCount()) {
                    continue;
                }
                if (requirement.matcher().matchesItem(entity.getItem())) {
                    return entity;
                }
            }
            return null;
        }

        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, cell)) {
            if (usedEntities.getOrDefault(entity, 0) >= entity.getItem().getCount()) {
                continue;
            }
            if (requirement.matcher().matchesItem(entity.getItem())) {
                return entity;
            }
        }
        return null;
    }

    private int findInventorySlot(@Nullable ItemStackHandler inventory, @NotNull scanner_block_matcher matcher,
                                  int[] usedSlots) {
        if (inventory == null) {
            return -1;
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (usedSlots[slot] >= stack.getCount()) {
                continue;
            }
            if (matcher.matchesItem(stack)) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Entfernt die Item-Zutaten des Aufbaus. Wird vor dem Zerstoeren der
     * Struktur aufgerufen, damit gedroppte Items nicht verloren gehen.
     */
    public void consumeItems(@NotNull ServerLevel level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState,
                             @NotNull Direction facing, boolean mirrored, @Nullable ItemStackHandler inventory) {
        List<ItemRequirement> requirements = collectItemRequirements(scannerPos, scannerState, facing, mirrored);
        if (requirements.isEmpty()) {
            return;
        }

        List<ItemUse> uses = resolveItemUses(level, requirements, inventory, null);
        if (uses == null) {
            return;
        }

        for (ItemUse use : uses) {
            if (use.entity() != null) {
                ItemStack stack = use.entity().getItem();
                if (stack.isEmpty()) {
                    continue;
                }
                stack.shrink(1);
                if (stack.isEmpty()) {
                    use.entity().discard();
                }
            } else if (inventory != null && use.inventorySlot() >= 0) {
                inventory.extractItem(use.inventorySlot(), 1, false);
            }
        }
    }

    private record ItemRequirement(@NotNull BlockPos pos, @NotNull scanner_block_matcher matcher) {
    }

    private record ItemUse(@Nullable ItemEntity entity, int inventorySlot) {
    }

    public void consume(@NotNull ServerLevel level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, @NotNull Direction facing, boolean mirrored) {
        ItemStack output = getResultStack();
        if (output.isEmpty()) {
            return;
        }
        Vec3 dropPos = Vec3.atCenterOf(getCenter(scannerPos, scannerState));
        level.addFreshEntity(new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, output));
    }

    public void consume(@NotNull ServerLevel level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState, boolean mirrored) {
        consume(level, scannerPos, scannerState, scannerState.getValue(BlockStateProperties.FACING), mirrored);
    }

    private @Nullable Item resolveResultItem(@Nullable HolderLookup.Provider registries) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, result.id());
        if (registries != null) {
            HolderLookup.RegistryLookup<Item> lookup = registries.lookup(Registries.ITEM).orElse(null);
            if (lookup != null) {
                return lookup.get(itemKey).map(Holder::value).orElse(null);
            }
        }
        if (!BuiltInRegistries.ITEM.containsKey(result.id())) {
            return null;
        }
        return BuiltInRegistries.ITEM.get(result.id());
    }
}

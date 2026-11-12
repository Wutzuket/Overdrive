package de.wutzuket.overdrive.integration.guideme;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Prueft, ob alle Paletten-Eintraege der Strukturdateien einen gueltigen Blockzustand ergeben.
 * Ein ungueltiger Zustand wird von Vanilla stillschweigend zu Luft, der Block fehlt dann in
 * der Szene des Handbuchs.
 */
class StructureStateParseTest {

    private static final String STRUCTURE_PATH =
            "assets/overdrive/guides/overdrive/guide/structures/";

    @Test
    void allPaletteEntriesParseIntoABlockState() throws Exception {
        var lookup = BuiltInRegistries.BLOCK.asLookup();

        for (var name : List.of("vacuum_pump.snbt", "particle_accelerator.snbt", "ad_ring.snbt",
                "elena_ring.snbt", "annihilation_reactor.snbt", "cooling_tower.snbt",
                "electron_gun.snbt")) {
            String text;
            try (var in = getClass().getClassLoader().getResourceAsStream(STRUCTURE_PATH + name)) {
                text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            CompoundTag unpacked = NbtUtils.snbtToStructure(text);
            ListTag palette = unpacked.getList("palette", 10);

            for (int i = 0; i < palette.size(); i++) {
                CompoundTag entry = palette.getCompound(i);
                String stateName = entry.getString("Name");
                String props = entry.getCompound("Properties").toString();

                BlockState state = NbtUtils.readBlockState(lookup, entry);
                assertFalse(state.isAir(), name + ": der Blockzustand '" + stateName
                        + "' " + props + " ergibt Luft");
            }
        }
    }
}

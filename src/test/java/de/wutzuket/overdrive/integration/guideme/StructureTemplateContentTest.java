package de.wutzuket.overdrive.integration.guideme;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft, ob die Strukturdateien die erwarteten Bloecke an den erwarteten Positionen enthalten.
 * Der Controller wird von {@code <PortSlots />} in der Szene gesucht, deshalb muss er an der
 * Stelle stehen, die auch die Handbuchseite nennt.
 */
class StructureTemplateContentTest {

    private static final String STRUCTURE_PATH =
            "assets/overdrive/guides/overdrive/guide/structures/";

    @Test
    void structureContainsTheControllerAtTheExpectedPosition() throws Exception {
        check("ad_ring.snbt", "overdrive:ad_controller", new BlockPos(4, 1, 8));
        check("elena_ring.snbt", "overdrive:elena_controller", new BlockPos(2, 1, 4));
        check("annihilation_reactor.snbt", "overdrive:annihilation_controller", new BlockPos(12, 4, 0));
        check("vacuum_pump.snbt", "overdrive:vacuum_pump", new BlockPos(1, 1, 0));
        check("particle_accelerator.snbt", "overdrive:particle_accelerator_controller",
                new BlockPos(25, 1, 50));
    }

    private void check(String fileName, String blockId, BlockPos expected) throws Exception {
        String text;
        try (var in = getClass().getClassLoader().getResourceAsStream(STRUCTURE_PATH + fileName)) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        CompoundTag unpacked = NbtUtils.snbtToStructure(text);

        HolderLookup<Block> lookup = BuiltInRegistries.BLOCK.asLookup();
        var template = new StructureTemplate();
        template.load(lookup, unpacked);

        var palettesField = StructureTemplate.class.getDeclaredField("palettes");
        palettesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<StructureTemplate.Palette> palettes =
                (List<StructureTemplate.Palette>) palettesField.get(template);

        assertEquals(1, palettes.size(), fileName + ": es wird genau eine Palette erwartet");

        var found = palettes.getFirst().blocks().stream()
                .filter(info -> info.pos().equals(expected))
                .map(info -> BuiltInRegistries.BLOCK.getKey(info.state().getBlock()).toString())
                .toList();

        assertEquals(List.of(blockId), found,
                fileName + ": an " + expected + " muss " + blockId + " stehen");
        assertTrue(palettes.getFirst().blocks().size() > 0, fileName + ": die Struktur ist leer");
    }
}

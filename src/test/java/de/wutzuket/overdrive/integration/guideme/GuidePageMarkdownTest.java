package de.wutzuket.overdrive.integration.guideme;

import guideme.compiler.PageCompiler;
import guideme.compiler.ParsedGuidePage;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.libs.mdast.model.MdAstNode;
import guideme.libs.mdast.model.MdAstParent;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft die Markdown-Seiten des Handbuchs mit dem Parser von GuideME.
 *
 * <p>Ein Syntaxfehler in einer Seite fuehrt dazu, dass GuideME statt der Seite
 * eine Fehlerseite anzeigt. Der Test parst daher jede Seite und prueft, dass
 * der Frontmatter gefunden wurde und die Seite einen Titel enthaelt.
 *
 * <p>Zusaetzlich werden alle MDX-Elemente der Seite gegen die Tag-Namen
 * geprueft, die GuideME tatsaechlich anbietet. Ein unbekanntes Tag wie
 * {@code <strong>} fuehrt im Spiel zur Fehlermeldung
 * "Unhandled MDX element in flow context" und wird hier erkannt.
 */
class GuidePageMarkdownTest {

    private static final String GUIDE_ROOT = "assets/overdrive/guides/overdrive/guide/";

    /**
     * Alle Tags, die GuideME 21.1.22 anbietet (siehe
     * {@code guideme.internal.extensions.DefaultExtensions}).
     */
    private static final Set<String> KNOWN_TAGS = Set.of(
            // Block-Kontext
            "div", "a", "Color", "ItemLink", "FloatingImage", "br", "Recipe", "RecipeFor",
            "RecipesFor", "ItemGrid", "CategoryIndex", "BlockImage", "ItemImage", "Row", "Column",
            "GameScene", "SubPages", "CommandLink", "PlayerName", "KeyBind", "OptionalSection",
            // Szenen-Elemente
            "Entity", "Block", "ImportStructure", "IsometricCamera",
            // Annotationen
            "BlockAnnotation", "BoxAnnotation", "LineAnnotation", "DiamondAnnotation",
            "BlockAnnotationTemplate", "RemoveBlocks",
            // eigene Tags der Mod
            "PortSlots");

    @Test
    void allPagesParseWithoutErrors() throws IOException {
        var loader = getClass().getClassLoader();

        var pages = new ArrayList<String>();
        collectPages(loader, GUIDE_ROOT, "", pages);
        assertEquals(30, pages.size(), "Es werden 30 Handbuchseiten erwartet (15 deutsch + 15 englisch)");

        for (var path : pages) {
            var id = ResourceLocation.fromNamespaceAndPath("overdrive", path);

            ParsedGuidePage page;
            try (InputStream in = loader.getResourceAsStream(GUIDE_ROOT + path)) {
                assertNotNull(in, "Seite fehlt: " + path);
                page = PageCompiler.parse("overdrive", "en_us", id, in);
            }

            var frontmatter = page.getFrontmatter();
            assertNotNull(frontmatter, path + ": kein Frontmatter gefunden");
            assertNotNull(frontmatter.navigationEntry(), path + ": kein Navigationseintrag gefunden");
            assertFalse(frontmatter.navigationEntry().title().isBlank(),
                    path + ": leerer Navigationstitel");

            // Der Ast darf keine Fehlerseite enthalten.
            assertFalse(page.getAstRoot().children().stream()
                    .anyMatch(c -> c.toString().contains("PARSING ERROR")),
                    path + ": Parsing-Fehler");

            // Jedes MDX-Element muss ein Tag von GuideME sein. Sonst zeigt
            // GuideME im Spiel eine Fehlermeldung an.
            var unknown = new LinkedHashSet<String>();
            collectTags(page.getAstRoot(), unknown);
            assertTrue(unknown.isEmpty(),
                    path + ": unbekannte MDX-Tags " + unknown
                            + " - GuideME unterstuetzt nur " + KNOWN_TAGS);
        }
    }

    @Test
    void germanPagesExistForEveryPage() throws IOException {
        var loader = getClass().getClassLoader();

        var pages = new ArrayList<String>();
        collectPages(loader, GUIDE_ROOT, "", pages);

        for (var path : pages) {
            if (path.startsWith("_de_de/")) {
                continue;
            }
            var german = "_de_de/" + path;
            assertNotNull(loader.getResource(GUIDE_ROOT + german),
                    "deutsche Uebersetzung fehlt: " + german);
        }
    }

    /**
     * Sammelt die Namen aller MDX-Elemente, die GuideME nicht kennt.
     */
    private static void collectTags(MdAstNode node, Set<String> unknown) {
        if (node instanceof MdxJsxElementFields element) {
            var name = element.name();
            if (name != null && !name.isEmpty() && !KNOWN_TAGS.contains(name)) {
                unknown.add(name);
            }
        }
        if (node instanceof MdAstParent<?> parent) {
            for (var child : parent.children()) {
                if (child instanceof MdAstNode childNode) {
                    collectTags(childNode, unknown);
                }
            }
        }
    }

    /**
     * Sammelt alle Markdown-Dateien unterhalb des Guide-Ordners.
     *
     * <p>Der Classloader kann Verzeichnisse nicht auflisten. Der Ordner wird
     * deshalb aus der URL einer bekannten Seite abgeleitet.
     */
    private static void collectPages(ClassLoader loader, String root, String prefix, List<String> out)
            throws IOException {
        var index = loader.getResource(root + "index.md");
        assertNotNull(index, "Startseite fehlt: " + root + "index.md");

        Path dir;
        try {
            dir = Path.of(index.toURI()).getParent();
        } catch (java.net.URISyntaxException e) {
            throw new IOException(e);
        }
        if (!Files.isDirectory(dir)) {
            throw new IOException("Guide-Ordner nicht gefunden: " + dir);
        }

        try (var stream = Files.walk(dir)) {
            stream.filter(p -> p.toString().endsWith(".md")).forEach(p -> {
                var rel = dir.relativize(p).toString().replace('\\', '/');
                out.add(prefix + rel);
            });
        }
    }
}

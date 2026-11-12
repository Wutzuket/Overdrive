---
navigation:
  title: Hologramm-Blöcke
  icon: overdrive:hologram_block
  parent: index.md
item_ids:
  - overdrive:hologram_block
  - overdrive:advanced_hologram_block
categories:
  - Maschinen
---

# Hologramm-Blöcke

Beide Hologramm-Blöcke visualisieren Bauarten in der Welt. Sie sind der
einfachste Weg, einen Multiblock zu planen: Baue den Bauplan als Hologramm
und setze die echten Blöcke darauf.

## Hologramm Block

Der <ItemLink id="overdrive:hologram_block" /> zeigt ein JSON-Modell, das im
Ordner `holograms` deiner Welt liegt.

Rechtsklick auf den Block öffnet das GUI:

| Bedienelement | Wirkung |
|---|---|
| Dateiliste | Alle JSON-Modelle im Ordner `holograms` der Welt |
| Versatz X / Y / Z | Verschiebt das Hologramm relativ zum Block |
| Skalierung | Skaliert das Modell |
| Rotation X / Y / Z | Dreht das Modell |
| Spin | Dreht das Hologramm automatisch |
| Neue Datei | Legt ein Modell aus dem eingegebenen JSON an (bis 65.536 Zeichen) |

<BlockImage id="overdrive:hologram_block" scale="2" />

## Advanced Hologram Block

Der <ItemLink id="overdrive:advanced_hologram_block" /> führt **Lua**-Skripte
aus und kann durchscheinende Würfel, anklickbare Buttons und schwebenden Text
anzeigen.

| Bedienelement | Wirkung |
|---|---|
| Skriptliste | Alle `.lua`-Dateien im Ordner `holograms` der Welt |
| Neues Skript | Legt ein leeres Skript an |
| Bearbeiten | Öffnet den eingebauten Code-Editor |
| Speichern | Schreibt den Editorinhalt zurück in die Datei |
| Löschen | Löscht das gewählte Skript |
| Start / Stop | Startet bzw. stoppt das Skript und löscht die Hologramme |
| Konsole | Zeigt alles, was das Skript ausgibt, und nimmt Eingaben für `read()` an |

Die wichtigsten Befehle der Skript-API:

| Befehl | Zweck |
|---|---|
| `cube(...)` | Erzeugt einen durchscheinenden farbigen Würfel |
| `button(...)` | Erzeugt einen anklickbaren Würfel |
| `text(...)` | Erzeugt schwebenden Text |
| `transition(...)` | Animiert Objekte |
| `sleep(t)` | Pausiert das Skript |
| `print(...)` / `read(...)` | Konsolenausgabe und -eingabe |
| `is_powered` | Redstone-Power des Blocks |
| `getSignalStrength(dir)` | Redstone-Signal einer Seite |

Grenzen: 512 Objekte und Transitionen pro Skript, 100.000 Instruktionen pro
Schritt und eine Auswahlreichweite von 8 Blöcken.

<BlockImage id="overdrive:advanced_hologram_block" scale="2" />

## Rezepte

<RecipeFor id="overdrive:hologram_block" />
<RecipeFor id="overdrive:advanced_hologram_block" />

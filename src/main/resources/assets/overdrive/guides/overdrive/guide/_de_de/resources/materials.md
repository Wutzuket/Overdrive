---
navigation:
  title: Materialien und Fluide
  icon: overdrive:aluminum_ingot
  parent: index.md
item_ids:
  - overdrive:aluminum_ore
  - overdrive:deepslate_aluminum_ore
  - overdrive:raw_aluminum
  - overdrive:aluminum_ingot
  - overdrive:aluminum_nugget
  - overdrive:aluminum_block
  - overdrive:iridium_ore
  - overdrive:deepslate_iridium_ore
  - overdrive:raw_iridium
  - overdrive:iridium_ingot
  - overdrive:iridium_block
  - overdrive:tungsten_ore
  - overdrive:deepslate_tungsten_ore
  - overdrive:raw_tungsten
  - overdrive:tungsten_ingot
  - overdrive:tungsten_block
  - overdrive:simple_frame
  - overdrive:creative_energy_cube
  - overdrive:wrench
categories:
  - Ressourcen
---

# Materialien und Fluide

## Erze

| Erz | Werkzeug | Weltgenerierung |
|---|---|---|
| <ItemLink id="overdrive:aluminum_ore" />, <ItemLink id="overdrive:deepslate_aluminum_ore" /> | Stein | 14 Adern pro Chunk, Größe 9, Y 0 – 64, in jedem Biom |
| <ItemLink id="overdrive:iridium_ore" />, <ItemLink id="overdrive:deepslate_iridium_ore" /> | Eisen | keine – Iridium muss über ein Datenpaket oder Modpaket bereitgestellt werden |
| <ItemLink id="overdrive:tungsten_ore" />, <ItemLink id="overdrive:deepslate_tungsten_ore" /> | Diamant | 8 Adern pro Chunk, Größe 8, Y -16 – 48, in jedem Overworld-Biom |

Die Aluminium-Generierung lässt sich in der gemeinsamen Konfiguration abschalten.

## Schmelzen

Rohes Erz wird im Ofen oder Hochofen geschmolzen. Jedes Roherz braucht
**200 Ticks** und gibt 0,7 Erfahrung. Ein Barren ergibt neun Klumpen.

<RecipeFor id="overdrive:aluminum_ingot" />
<RecipeFor id="overdrive:iridium_ingot" />
<RecipeFor id="overdrive:tungsten_ingot" />

## Lagerblöcke

Neun Barren ergeben einen Block, ein Block wieder neun Barren.

<Row>
  <ItemImage id="overdrive:aluminum_block" />
  <ItemImage id="overdrive:iridium_block" />
  <ItemImage id="overdrive:tungsten_block" />
</Row>

## Fluide

| Fluid | Temperatur | Hinweis |
|---|---|---|
| <ItemLink id="overdrive:cryofluid_bucket" /> | – | Kühlmittel für den Reaktor, 50 K pro mB |
| <ItemLink id="overdrive:steam_bucket" /> | 500 K | Aufgeheiztes Kühlmittel |
| <ItemLink id="overdrive:superheated_cryofluid_bucket" /> | 600 K | Aufgeheiztes Kryofluid |
| <ItemLink id="overdrive:liquid_iridium_bucket" /> | 1200 K | Brennstoff des Teilchenbeschleunigers |

## Hilfsblöcke

Der <ItemLink id="overdrive:simple_frame" /> ist ein Füllblock, den die
Scanner-Rezepte für die noch fehlenden Zellen eines Musters verwenden.

Der <ItemLink id="overdrive:creative_energy_cube" /> ist eine unendliche
Energiequelle zum Testen. Er schiebt alle fünf Ticks die maximale Energie in
alle sechs Seiten und kann nicht zerstört werden.

Der <ItemLink id="overdrive:wrench" /> schaltet die Seiten der Vakuum- und
Antimatter-Rohre um und zerlegt Blöcke, die auf dem Tag `wrenchable` stehen.

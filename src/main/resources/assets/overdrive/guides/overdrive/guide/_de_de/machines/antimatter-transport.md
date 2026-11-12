---
navigation:
  title: Antimatter-Transport
  icon: overdrive:antimatter_tank
  parent: index.md
item_ids:
  - overdrive:antimatter_pipe_basic
  - overdrive:antimatter_pipe_advanced
  - overdrive:antimatter_pipe_elite
  - overdrive:antimatter_pipe_ultimate
  - overdrive:antimatter_tank
  - overdrive:empty_antimatter_cell
categories:
  - Maschinen
  - Antimatter
---

# Antimatter-Transport

Antimatter ist keine Minecraft-Flüssigkeit. Es hat ein eigenes Rohrnetz,
eigene Tanks und eigene Zellen. Eimer und Fluid-Handler sind nicht beteiligt.

## Die vier Antimatter-Arten

| Art | Zelle | Produziert von |
|---|---|---|
| Antiprotonen | <ItemLink id="overdrive:empty_antimatter_cell" /> | [Teilchenbeschleuniger](particle-accelerator.md) |
| Abgebremste Antiprotonen | <ItemLink id="overdrive:empty_antimatter_cell" /> | [AD-Ring](ad-ring.md) |
| Gekühlte Antiprotonen | <ItemLink id="overdrive:empty_antimatter_cell" /> | [ELENA-Ring](elena-ring.md) |
| Positronen | <ItemLink id="overdrive:empty_antimatter_cell" /> | [Elektronenkanone](electron-gun.md) |

Es gibt nur **ein** Zellen-Item. Der Inhalt steckt im Item, dieselbe Zelle wird
also je nach Inhalt als *Antiprotonen-Zelle*, *Abgebremste-Antiprotonen-Zelle*,
*Gekühlte-Antiprotonen-Zelle* oder *Positronen-Zelle* angezeigt.

## Rohre

Vier Stufen teilen sich denselben Block, unterscheiden sich aber in Farbe und
Transferrate:

| Rohr | Transfer pro Seite und Tick |
|---|---|
| <ItemLink id="overdrive:antimatter_pipe_basic" /> | 4.000 mB/t |
| <ItemLink id="overdrive:antimatter_pipe_advanced" /> | 8.000 mB/t |
| <ItemLink id="overdrive:antimatter_pipe_elite" /> | 16.000 mB/t |
| <ItemLink id="overdrive:antimatter_pipe_ultimate" /> | 32.000 mB/t |

Ein Rohr puffert das Vierfache seiner Transferrate. Mit dem
<ItemLink id="overdrive:wrench" /> lässt sich der Modus einer Seite umstellen:

| Modus | Verhalten |
|---|---|
| Normal | Lässt die Antimatter durch |
| Push | Schiebt in die Nachbarmaschine |
| Pull | Entnimmt aus der Nachbarmaschine |
| Disabled | Geschlossen, wird mit Metallkappe dargestellt |

## Tanks und Zellen

Der <ItemLink id="overdrive:antimatter_tank" /> lagert **eine** Antimatter-Art
mit 32.000 mB Kapazität. Die <ItemLink id="overdrive:empty_antimatter_cell" />
fasst 4.000 mB und übernimmt die Art des Tanks, aus der sie gefüllt wurde. Eine
gefüllte Zelle wird zurück in den Tank gelegt, um sie zu leeren.

Für jede Antimatter-Art gibt es einen kreativen Tank. Rechtsklick mit ihm auf
einen normalen Tank macht diesen zu einer unendlichen Quelle.

## Rezepte

<RecipeFor id="overdrive:antimatter_tank" />
<RecipeFor id="overdrive:empty_antimatter_cell" />
<RecipeFor id="overdrive:antimatter_pipe_basic" />

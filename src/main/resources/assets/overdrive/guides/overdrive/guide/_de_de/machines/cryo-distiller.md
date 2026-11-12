---
navigation:
  title: Kryodestillator
  icon: overdrive:cryo_distiller
  parent: index.md
item_ids:
  - overdrive:cryo_distiller
categories:
  - Maschinen
  - Kühlung
---

# Kryodestillator

Der Kryodestillator produziert **Kryofluid** aus Wasser. Kryofluid ist das
bevorzugte Kühlmittel des
[Annihilationsreaktors](annihilation-reactor.md), weil es pro Millibucket
viel mehr Wärme aufnimmt als Wasser.

## Bauart

Der Destillator ist ein einzelner Block. Er braucht einen **Katalysatorblock
direkt unter sich** – ohne diesen Block arbeitet die Maschine nicht. Das GUI
zeigt an, welcher Katalysator erwartet wird.

<BlockImage id="overdrive:cryo_distiller" scale="2" />

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Energiespeicher | 100.000.000 FE |
| Energie pro Tick | 10.000 FE |
| Ein-/Ausgangstank | je 32.000 mB |
| Prozesszeit | 20 Ticks (Blue Ice), 60 Ticks (Packed Ice) |

Das Standardrezept verwandelt 250 mB Wasser mit einem Katalysator aus Blue Ice
oder Packed Ice in 100 mB Kryofluid.

## Rezepte

<RecipeFor id="overdrive:cryo_distiller" />

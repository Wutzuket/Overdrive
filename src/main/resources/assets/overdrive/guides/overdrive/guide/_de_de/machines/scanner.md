---
navigation:
  title: Scanner
  icon: overdrive:scanner
  parent: index.md
item_ids:
  - overdrive:scanner
  - overdrive:scanner_panel
categories:
  - Maschinen
---

# Scanner

Der Scanner erkennt Multiblock-Bauteile und fertigt sie an. Viele der
 hochwertigen Maschinenteile von Overdrive lassen sich nur mit ihm herstellen.

## Bauart

Der Scanner sucht ein **3x3x3-Muster**. dessen Mittelpunkt liegt **drei
Blöcke vor dem Scanner und einen Block darüber**. Baue das Muster, das JEI für
das gewünschte Rezept anzeigt – gespiegelte Layouts sind ebenfalls erlaubt.

Starte den Scan mit einem **Redstone-Signal**. Der Scanner animiert, verbraucht
das Muster und wirft das Ergebnis aus.

<BlockImage id="overdrive:scanner" scale="2" />

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Energiespeicher | 100.000 FE |
| Energie pro Scan | 5.000 FE |
| Gegenstandsslots | 4 |

Manche Rezepte brauchen zusätzliche Gegenstände. Du kannst sie entweder in die
leeren Zellen des Musters legen – sie liegen dann auf dem Block darunter – oder
in die vier Gegenstandsslots des Scanner-GUIs, das sich durch Rechtsklick auf
den Scanner öffnet. Die Gegenstände werden beim Start des Scans verbraucht.
Was beim Abbau des Scanners noch in den Slots liegt, wird fallen gelassen.

## Scanner-Panel

Das <ItemLink id="overdrive:scanner_panel" /> ist die Stufenmarkierung des
Scanners. Es wird in der [Circuit Press](circuit-press.md) gepresst und wird von
Rezepten wie dem Kühlturm-Controller benötigt.

<RecipeFor id="overdrive:scanner_panel" />

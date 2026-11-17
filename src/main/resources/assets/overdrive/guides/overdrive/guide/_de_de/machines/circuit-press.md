---
navigation:
  title: Circuit Press
  icon: overdrive:circuit_press
  parent: index.md
item_ids:
  - overdrive:circuit_press
  - overdrive:circuit_board
  - overdrive:scanner_panel
categories:
  - Maschinen
---

# Circuit Press

Die Circuit Press ist die erste Maschine der Microcrafting-Kette. Sie presst das
<ItemLink id="overdrive:circuit_board" /> und das
<ItemLink id="overdrive:scanner_panel" />, die der
[Scanner](scanner.md) für die hochwertigen Bauteile braucht.

## Bauart

Die Presse ist ein einzelner Block mit **vier Eingangsslots** und **einem
Ausgangsslots**. Sie zieht ihre Energie direkt von benachbarten Energiespeichern,
du brauchst also kein Kabelnetz um sie herum.

<BlockImage id="overdrive:circuit_press" scale="2" />

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Energiespeicher | 10.000 FE |
| Energie pro Tick | 100 FE (Circuit Board), 300 FE (Scanner-Panels) |
| Prozesszeit | 100 Ticks (Circuit Board), 120 Ticks (Scanner-Panels) |
| Slotkapazität | 64 Gegenstände |

Beide Werte lassen sich in der gemeinsamen Konfiguration ändern.

## Rezepte

| Rezept | Ergebnis |
|---|---|
| Aluminiumbarren + Kupferbarren + Redstone | 1 x <ItemLink id="overdrive:circuit_board" /> |
| Circuit Board + Glasscheibe + Redstone | 2 x <ItemLink id="overdrive:scanner_panel" /> |

<RecipeFor id="overdrive:circuit_press" />

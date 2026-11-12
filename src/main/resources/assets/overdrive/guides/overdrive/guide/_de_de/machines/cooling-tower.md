---
navigation:
  title: Kühlturm
  icon: overdrive:cooling_tower_controller
  parent: index.md
item_ids:
  - overdrive:cooling_tower_controller
  - overdrive:cooling_tower_casing
  - overdrive:cooling_tower_input_port
  - overdrive:cooling_tower_output_port
categories:
  - Maschinen
  - Kühlung
---

# Kühlturm

Der Kühlturm kondensiert heiße Medien wieder. Er ist das Gegenstück zum
[Annihilationsreaktor](annihilation-reactor.md): Der Reaktor erhitzt das
Kühlmittel mit seiner Abwärme, der Turm kühlt es herunter.

## Bauart

Der Turm ist eine **5 x 5 x 9** große Schale mit hyperbolischer Taille. Der
Wandradius beträgt 2 Blöcke an der Basis und an der Krone und nur 1 Block in
den Lagen 3 bis 5.

* In den Lagen 3 bis 5 muss der äußere 5x5-Rand **leer** bleiben.
* Der Innenraum des Turms muss **leer** bleiben.
* Der Controller sitzt in der Wand der Basislage und blickt **vom Turm weg**.
  Das Turmzentrum liegt 2 Blöcke hinter dem Controller.
* Der **Eingangs-Port** sitzt an Zentrum + (2, 1, 0).
* Der **Ausgangs-Port** sitzt an Zentrum + (-2, 7, 0).

Die Portpositionen werden **nicht** mit dem Controller mitgedreht. Sie bleiben
immer auf der X-Achse, egal wohin der Controller blickt. Sie sind außerdem
**fest**: An diesen beiden Positionen wird nur der passende Port akzeptiert,
alle anderen Wandpositionen müssen Gehäuse sein.

<GameScene zoom="1.5" interactive={true}>
  <ImportStructure src="../structures/cooling_tower.snbt" />
  <BlockAnnotation x="2" y="0" z="4" color="#ffaa00">
    **Controller**<br />Sitzt in der Basiswand und blickt vom Turm weg.
  </BlockAnnotation>
  <BlockAnnotation x="4" y="1" z="2" color="#55ff55">
    **Kühlmittel-Eingang**<br />Führt heißes Kühlmittel in den Turm. Feste Position: hier kann kein anderer Port stehen.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="7" z="2" color="#55ff55">
    **Kühlmittel-Ausgang**<br />Sammelt das kondensierte Medium. Feste Position: hier kann kein anderer Port stehen.
  </BlockAnnotation>
</GameScene>

## Benötigte Bauteile

| Block | Anzahl |
|---|---|
| <ItemLink id="overdrive:cooling_tower_controller" /> | 1 |
| <ItemLink id="overdrive:cooling_tower_input_port" /> | 1, an Zentrum + (2, 1, 0) |
| <ItemLink id="overdrive:cooling_tower_output_port" /> | 1, an Zentrum + (-2, 7, 0) |
| <ItemLink id="overdrive:cooling_tower_casing" /> | der Rest (117) |

Im Kreativmodus kannst du die Schale mit Gehäuse und Ports füllen, indem du den
Controller mit dem respective Block in der Hand rechtsklickst.

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Energie | keine |
| Ein-/Ausgangstank | je 8.000 mB |
| Dampf zu Wasser | 1 mB zu 1 mB, 4 Ticks |
| Überhitztes Kryofluid zu Kryofluid | 1.000 mB zu 1.000 mB, 1 Tick |

Der Turm arbeitet ohne Strom. Er braucht nur das richtige Medium im
Eingangs-Port und Platz im Ausgangstank.

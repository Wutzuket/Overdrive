---
navigation:
  title: Elektronenkanone
  icon: overdrive:electron_gun
  parent: index.md
item_ids:
  - overdrive:electron_gun
  - overdrive:tungsten_target
  - overdrive:tungsten_mesh
  - overdrive:tungsten_casing
  - overdrive:positron_vacuum_output_port
categories:
  - Maschinen
  - Antimatter
---

# Elektronenkanone

Die Elektronenkanone erzeugt **Positronen**. Zusammen mit den gekühlten
Antiprotonen des [ELENA-Rings](elena-ring.md) sind sie der Brennstoff des
[Annihilationsreaktors](annihilation-reactor.md).

## Bauart

Die Maschine ist eine gerade Reihe aus drei Blöcken in Blickrichtung der
Elektronenkanone:

| Abstand | Block |
|---|---|
| 0 | <ItemLink id="overdrive:electron_gun" /> |
| 1 | <ItemLink id="overdrive:tungsten_target" /> |
| 2 | <ItemLink id="overdrive:positron_vacuum_output_port" /> |

Die Blöcke müssen direkt aneinanderstehen. Der
<ItemLink id="overdrive:positron_vacuum_output_port" /> ist gleichzeitig
Antimatter-Ausgang und Vakuum-Port der Maschine.

Beide Positionen sind **fest**: Das Target muss genau einen Block vor der
Kanone stehen, der Positronen-Port genau zwei Blöcke davor. Dort wird nichts
anderes akzeptiert.

Das Vakuum kommt **nicht** in den Positronen-Port, sondern in die
**Elektronenkanone** selbst. Das <ItemLink id="overdrive:vacuum_pipe" /> muss
deshalb die Kanone berühren, zum Beispiel direkt dahinter. Nur dann sieht die
Kanone den Vakuumpegel des Pumpennetzes.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../structures/electron_gun.snbt" />
  <BlockAnnotation x="0" y="0" z="2" color="#ffaa00">
    **Elektronenkanone**<br />Beschießt das Target und nimmt das Vakuum vom Rohr.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="0" z="1" color="#ff5555">
    **Wolfram-Target**<br />Hält das Wolfram-Netz, das bei jedem Vorgang verschleißt.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="0" z="0" color="#55ff55">
    **Positronen-Vakuum-Port**<br />Lagert die Positronen und führt sie ins Antimatternetz. Feste Position: hier kann kein anderer Port stehen.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="0" z="3" color="#5599ff">
    **Vakuumrohr**<br />Führt das Vakuum in die Elektronenkanone.
  </BlockAnnotation>
</GameScene>

## Benötigte Komponenten

| Block | Anzahl |
|---|---|
| <ItemLink id="overdrive:electron_gun" /> | 1 |
| <ItemLink id="overdrive:tungsten_target" /> | 1 |
| <ItemLink id="overdrive:positron_vacuum_output_port" /> | 1 |
| <ItemLink id="overdrive:tungsten_mesh" /> | 1 pro Vorgang |
| <ItemLink id="overdrive:tungsten_casing" /> | optional, zählt nur zum Vakuumvolumen |

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Energiespeicher | 500.000 FE |
| Energierate | bis 50.000 FE/t (Standard 50.000) |
| Benötigtes Vakuum | 99 % |
| Ergebnis pro Vorgang | 4.000 mB Positronen |
| Prozesszeit | 1 Tick |
| Netzverschleiß | 100 pro Vorgang (1x) |

Jeder Vorgang verbraucht Haltbarkeit vom
<ItemLink id="overdrive:tungsten_mesh" /> im Target. Ein frisches Netz hat 100
Haltbarkeit, übersteht bei Rate 1x also genau einen Vorgang. Setze ein neues
Netz ein, bevor die Maschine leerläuft.

Der <ItemLink id="overdrive:positron_vacuum_output_port" /> hat einen Tank von
500.000 mB. Rechtsklick mit einer leeren
<ItemLink id="overdrive:empty_antimatter_cell" /> füllt die Zelle, mit einer
gefüllten Zelle wird sie geleert.

## Ausfall

Fällt das Vakuum unter 99 %, explodiert die Maschine.

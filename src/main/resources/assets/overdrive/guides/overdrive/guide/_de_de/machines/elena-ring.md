---
navigation:
  title: ELENA-Ring
  icon: overdrive:elena_controller
  parent: index.md
item_ids:
  - overdrive:elena_controller
  - overdrive:elena_casing
  - overdrive:elena_energy_input_port
  - overdrive:elena_vacuum_port
  - overdrive:elena_input_port
  - overdrive:elena_output_port
categories:
  - Maschinen
  - Antimatter
---

# ELENA-Ring

Der ELENA-Ring ist die letzte Stufe der Antimatter-Kette. Er **kühlt** die
abgebremsten Antiprotonen herunter, damit sie als Brennstoff im
[Annihilationsreaktor](annihilation-reactor.md) taugen.

## Bauart

Der ELENA-Ring ist ein quadratischer Ring mit einem Chebyshev-Radius von
**2 Blöcken** und **drei Lagen**. Das Ringzentrum liegt 2 Blöcke vor dem
Controller, in dessen Blickrichtung.

* **Boden** und **Deckel** bestehen nur aus
  <ItemLink id="overdrive:elena_casing" />.
* Die **Mittellage** trägt die Außenwand (Radius 2) und die Innenwand
  (Radius 1).
* Jede Position der Mittellage kann durch einen der vier Ports ersetzt
  werden. Der Ring braucht mindestens einen Energie-Port, einen Vakuum-Port,
  einen Eingangs-Port und einen Ausgangs-Port.
* Der Innenraum des Rings bleibt leer.

Der komplette Ring besteht aus **72 Blöcken**.

<GameScene zoom="2" interactive={true}>
  <ImportStructure src="../structures/elena_ring.snbt" />
  <PortSlots controller="overdrive:elena_controller" />
  <BlockAnnotation x="2" y="1" z="4" color="#ffaa00">
    **Controller**<br />Sitzt auf der Außenwand und blickt zum Ringzentrum.
  </BlockAnnotation>
  <BlockAnnotation x="3" y="1" z="4" color="#55ff55">
    **ELENA-Energie-Eingang**<br />Versorgt den Ring mit Forge Energy. Kann ersetzt werden durch: <ItemLink id="overdrive:elena_vacuum_port" />, <ItemLink id="overdrive:elena_input_port" /> oder <ItemLink id="overdrive:elena_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="4" y="1" z="4" color="#55ff55">
    **ELENA-Vakuum-Port**<br />Bringt den Vakuumpegel der Pumpe in den Ring. Kann ersetzt werden durch: <ItemLink id="overdrive:elena_energy_input_port" />, <ItemLink id="overdrive:elena_input_port" /> oder <ItemLink id="overdrive:elena_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="1" y="1" z="4" color="#55ff55">
    **ELENA-Eingangs-Port**<br />Antimatter-Rohre enden hier und füllen den Tank im Controller. Kann ersetzt werden durch: <ItemLink id="overdrive:elena_energy_input_port" />, <ItemLink id="overdrive:elena_vacuum_port" /> oder <ItemLink id="overdrive:elena_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="1" z="4" color="#55ff55">
    **ELENA-Ausgangs-Port**<br />Rohre entnehmen hier die gekühlten Antiprotonen. Kann ersetzt werden durch: <ItemLink id="overdrive:elena_energy_input_port" />, <ItemLink id="overdrive:elena_vacuum_port" /> oder <ItemLink id="overdrive:elena_input_port" />.
  </BlockAnnotation>
</GameScene>

## Benötigte Bauteile

| Block | Anzahl |
|---|---|
| <ItemLink id="overdrive:elena_controller" /> | 1 |
| <ItemLink id="overdrive:elena_energy_input_port" /> | mindestens 1 |
| <ItemLink id="overdrive:elena_vacuum_port" /> | mindestens 1 |
| <ItemLink id="overdrive:elena_input_port" /> | mindestens 1 |
| <ItemLink id="overdrive:elena_output_port" /> | mindestens 1 |
| <ItemLink id="overdrive:elena_casing" /> | der Rest |

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Energiespeicher | 10.000.000.000 FE |
| Energierate | 1 – 100.000.000 FE/t (Standard 50.000.000) |
| Benötigtes Vakuum | 90 % |
| Ein-/Ausgangstank | je 500.000 mB |

Das Standardrezept verwandelt 20.000 mB abgebremste Antiprotonen pro Tick in
20.000 mB gekühlte Antiprotonen.

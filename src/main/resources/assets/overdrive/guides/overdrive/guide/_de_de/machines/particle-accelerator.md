---
navigation:
  title: Teilchenbeschleuniger
  icon: overdrive:particle_accelerator_controller
  parent: index.md
item_ids:
  - overdrive:particle_accelerator_controller
  - overdrive:accelerator_casing
  - overdrive:accelerator_energy_input_port
  - overdrive:accelerator_vacuum_input_port
  - overdrive:accelerator_output
  - overdrive:iridium_target
categories:
  - Maschinen
  - Antimatter
---

# Teilchenbeschleuniger

Der Teilchenbeschleuniger ist die erste Maschine der Antimatter-Kette. Er
verwandelt **flüssiges Iridium** in **Antiprotonen**.

## Bauart

Der Beschleuniger ist ein großer kreisförmiger Ring mit einem Außenradius von
**25 Blöcken** und **drei Lagen**. Das Ringzentrum liegt genau 25 Blöcke vor dem
Controller, in dessen Blickrichtung.

* **Boden** und **Deckel** sind im Ringband vollständig mit
  <ItemLink id="overdrive:accelerator_casing" /> gefüllt.
* Die **Mittellage** hat nur eine Außen- und eine Innenwand. Der breite Tunnel
  dazwischen muss **leer** bleiben.
* Der Controller sitzt auf der Außenwand und blickt zum Ringzentrum.
* Jede Position der beiden Wände kann durch einen der fünf Funktionsblöcke
  ersetzt werden. Der Beschleuniger braucht mindestens ein Target, einen
  Ausgang, einen Energie-Port und einen Vakuum-Port.

Das Ringband reicht von 21,5 bis 25,5 Blöcken um das Zentrum.

<GameScene zoom="0.4" interactive={true}>
  <ImportStructure src="../structures/particle_accelerator.snbt" />
  <PortSlots controller="overdrive:particle_accelerator_controller" />
  <BlockAnnotation x="25" y="1" z="50" color="#ffaa00">
    **Controller**<br />Sitzt auf der Außenwand und blickt zum Ringzentrum.
  </BlockAnnotation>
  <BlockAnnotation x="25" y="1" z="3" color="#ff5555">
    **Iridium-Target**<br />Fasst 100.000 mB flüssiges Iridium und muss gefüllt sein, bevor die Maschine läuft. Kann ersetzt werden durch: <ItemLink id="overdrive:accelerator_energy_input_port" />, <ItemLink id="overdrive:accelerator_vacuum_input_port" /> oder <ItemLink id="overdrive:accelerator_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="40" y="1" z="9" color="#55ff55">
    **Beschleuniger-Ausgang**<br />Sammelt die Antiprotonen und speist sie ins Antimatter-Rohrnetz. Kann ersetzt werden durch: <ItemLink id="overdrive:accelerator_energy_input_port" />, <ItemLink id="overdrive:accelerator_vacuum_input_port" /> oder <ItemLink id="overdrive:iridium_target" />.
  </BlockAnnotation>
  <BlockAnnotation x="5" y="1" z="40" color="#55ff55">
    **Energie-Eingang**<br />Versorgt den Beschleuniger mit Forge Energy. Kann ersetzt werden durch: <ItemLink id="overdrive:accelerator_vacuum_input_port" />, <ItemLink id="overdrive:accelerator_output" /> oder <ItemLink id="overdrive:iridium_target" />.
  </BlockAnnotation>
  <BlockAnnotation x="45" y="1" z="10" color="#55ff55">
    **Vakuum-Eingang**<br />Bringt den Vakuumpegel der Pumpe in den Ring. Kann ersetzt werden durch: <ItemLink id="overdrive:accelerator_energy_input_port" />, <ItemLink id="overdrive:accelerator_output" /> oder <ItemLink id="overdrive:iridium_target" />.
  </BlockAnnotation>
</GameScene>

## Benötigte Bauteile

| Block | Anzahl | Position |
|---|---|---|
| <ItemLink id="overdrive:particle_accelerator_controller" /> | 1 | Außenwand, Mittellage, blickt zum Zentrum |
| <ItemLink id="overdrive:iridium_target" /> | mindestens 1 | Wand, Mittellage |
| <ItemLink id="overdrive:accelerator_output" /> | mindestens 1 | Wand, Mittellage |
| <ItemLink id="overdrive:accelerator_energy_input_port" /> | mindestens 1 | Wand, Mittellage |
| <ItemLink id="overdrive:accelerator_vacuum_input_port" /> | mindestens 1 | Wand, Mittellage |
| <ItemLink id="overdrive:accelerator_casing" /> | der Rest | Boden, Deckel und Wände |

Der komplette Ring besteht aus **1500 Blöcken**.

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Energiespeicher | 50.000.000.000 FE |
| Energierate | 1 – 800.000.000 FE/t (Standard 250.000.000) |
| Benötigtes Vakuum | 90 % |
| Ausgangstank | 500.000 mB |
| Target-Tank | 100.000 mB flüssiges Iridium |

Das Standardrezept verwandelt 1.000 mB flüssiges Iridium pro Tick in 20.000 mB
Antiprotonen. Der Ausgangsblock sammelt die Antiprotonen und schiebt sie in die
Antimatter-Rohre – in dieser Mod gibt es keine Eimer.

## Ausfall

Bricht die Struktur oder geht das Vakuum verloren, explodiert der Beschleuniger.
Die Zerstörungschance pro Lage beträgt 15 % unten, 50 % in der Mitte und 85 %
oben. Das Verhalten lässt sich in der gemeinsamen Konfiguration abschalten.

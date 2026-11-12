---
navigation:
  title: AD-Ring
  icon: overdrive:ad_controller
  parent: index.md
item_ids:
  - overdrive:ad_controller
  - overdrive:ad_casing
  - overdrive:ad_energy_input_port
  - overdrive:ad_vacuum_port
  - overdrive:ad_input_port
  - overdrive:ad_output_port
categories:
  - Maschinen
  - Antimatter
---

# AD-Ring

Der Antiprotonen-Abbremser ist die zweite Stufe der Antimatter-Kette. Er
**bremst** die Antiprotonen des
[Teilchenbeschleunigers](particle-accelerator.md) ab, damit sie danach
heruntergekühlt werden können.

## Bauart

Der AD-Ring ist ein quadratischer Ring mit einem Chebyshev-Radius von
**4 Blöcken** und **drei Lagen**. Das Ringzentrum liegt 4 Blöcke vor dem
Controller, in dessen Blickrichtung.

* **Boden** und **Deckel** bestehen nur aus
  <ItemLink id="overdrive:ad_casing" />.
* Die **Mittellage** trägt die Außenwand (Radius 4) und die Innenwand
  (Radius 3). Beide bestehen aus Gehäuse und den Funktionsblöcken.
* Jede Position der Mittellage kann durch einen der vier Ports ersetzt
  werden. Der Ring braucht mindestens einen Energie-Port, einen Vakuum-Port,
  einen Eingangs-Port und einen Ausgangs-Port.
* Der Innenraum des Rings bleibt leer.

Der komplette Ring besteht aus **168 Blöcken**.

<GameScene zoom="1.5" interactive={true}>
  <ImportStructure src="../structures/ad_ring.snbt" />
  <PortSlots controller="overdrive:ad_controller" />
  <BlockAnnotation x="4" y="1" z="8" color="#ffaa00">
    **Controller**<br />Sitzt auf der Außenwand und blickt zum Ringzentrum.
  </BlockAnnotation>
  <BlockAnnotation x="8" y="1" z="4" color="#55ff55">
    **AD-Energie-Eingang**<br />Versorgt den Ring mit Forge Energy. Kann ersetzt werden durch: <ItemLink id="overdrive:ad_vacuum_port" />, <ItemLink id="overdrive:ad_input_port" /> oder <ItemLink id="overdrive:ad_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="1" z="4" color="#55ff55">
    **AD-Vakuum-Port**<br />Bringt den Vakuumpegel der Pumpe in den Ring. Kann ersetzt werden durch: <ItemLink id="overdrive:ad_energy_input_port" />, <ItemLink id="overdrive:ad_input_port" /> oder <ItemLink id="overdrive:ad_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="4" y="1" z="0" color="#55ff55">
    **AD-Eingangs-Port**<br />Antimatter-Rohre enden hier und füllen den Tank im Controller. Kann ersetzt werden durch: <ItemLink id="overdrive:ad_energy_input_port" />, <ItemLink id="overdrive:ad_vacuum_port" /> oder <ItemLink id="overdrive:ad_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="4" y="1" z="7" color="#55ff55">
    **AD-Ausgangs-Port**<br />Rohre entnehmen hier die abgebremsten Antiprotonen. Kann ersetzt werden durch: <ItemLink id="overdrive:ad_energy_input_port" />, <ItemLink id="overdrive:ad_vacuum_port" /> oder <ItemLink id="overdrive:ad_input_port" />.
  </BlockAnnotation>
</GameScene>

## Benötigte Bauteile

| Block | Anzahl |
|---|---|
| <ItemLink id="overdrive:ad_controller" /> | 1 |
| <ItemLink id="overdrive:ad_energy_input_port" /> | mindestens 1 |
| <ItemLink id="overdrive:ad_vacuum_port" /> | mindestens 1 |
| <ItemLink id="overdrive:ad_input_port" /> | mindestens 1 |
| <ItemLink id="overdrive:ad_output_port" /> | mindestens 1 |
| <ItemLink id="overdrive:ad_casing" /> | der Rest |

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Energiespeicher | 20.000.000.000 FE |
| Energierate | 1 – 200.000.000 FE/t (Standard 100.000.000) |
| Benötigtes Vakuum | 90 % |
| Ein-/Ausgangstank | je 500.000 mB |

Das Standardrezept verwandelt 20.000 mB Antiprotonen pro Tick in 20.000 mB
abgebremste Antiprotonen. Ein- und Ausgangs-Port haben keinen eigenen Puffer,
sie führen direkt zu den Tanks im Controller.

## Ausfall

Eine kaputte Struktur oder ein Vakuumverlust lässt den Ring explodieren und
zerstört die Lagen mit 15 %, 50 % und 85 % Wahrscheinlichkeit. Das lässt sich in
der gemeinsamen Konfiguration abschalten.

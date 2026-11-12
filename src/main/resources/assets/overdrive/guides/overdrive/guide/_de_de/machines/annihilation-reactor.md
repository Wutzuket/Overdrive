---
navigation:
  title: Annihilationsreaktor
  icon: overdrive:annihilation_controller
  parent: index.md
item_ids:
  - overdrive:annihilation_controller
  - overdrive:annihilation_heavy_casing
  - overdrive:reactor_core_block
  - overdrive:core_containment_glass
  - overdrive:cell_docking_port
  - overdrive:annihilation_vacuum_port
  - overdrive:coolant_input_port
  - overdrive:steam_output_port
  - overdrive:annihilation_energy_output
  - overdrive:corium_block
categories:
  - Maschinen
  - Antimatter
---

# Annihilationsreaktor

Der Annihilationsreaktor ist das Herzstück von Overdrive. Er vernichtet
**gekühlte Antiprotonen** und **Positronen** und wandelt sie in rohe Energie um.

## Bauart

Der Reaktor ist ein **25 x 25 x 25** großer Torus. Der Torus hat einen
Hauptradius von 8 und einen Rohrradius von 4 Blöcken, daraus ergeben sich 2132
Wandblöcke in neun Lagen.

* Die **Wand** des Torus besteht aus
  <ItemLink id="overdrive:annihilation_heavy_casing" /> und den unten
  genannten Ports. Kein anderer Block und keine Luft sind auf der Wand erlaubt.
* Jede Position der Wand kann durch einen der fünf Ports ersetzt werden. Der
  Reaktor braucht zwei Zellen-Andock-Ports und je einen der anderen Ports.
* Das **Innere des Rohrs** muss **leer** bleiben.
* Der **Kern** ist ein 5x5x5-Würfel in der Mitte des Torus. Seine inneren
  3x3x3-Zellen müssen <ItemLink id="overdrive:reactor_core_block" /> sein, die
  Hülle darum <ItemLink id="overdrive:core_containment_glass" />.

Der Controller sitzt auf der Wand und blickt zum Kern, das Ringzentrum liegt
also 12 Blöcke vor ihm.

<GameScene zoom="0.5" interactive={true}>
  <ImportStructure src="../structures/annihilation_reactor.snbt" />
  <PortSlots controller="overdrive:annihilation_controller" />
  <BlockAnnotation x="12" y="4" z="0" color="#ffaa00">
    **Controller**<br />Sitzt auf der Toruswand und blickt zum Kern.
  </BlockAnnotation>
  <BlockAnnotation x="12" y="4" z="12" color="#ff5555">
    **Reaktorkern**<br />5x5x5: 27 Kernblöcke innen, Containment-Glas darum.
  </BlockAnnotation>
  <BlockAnnotation x="12" y="8" z="20" color="#55ff55">
    **Zellen-Andock-Port**<br />Nimmt Antiprotonen oder Positronen auf. Zwei davon sind Pflicht. Kann ersetzt werden durch: <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:coolant_input_port" />, <ItemLink id="overdrive:steam_output_port" /> oder <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="13" y="8" z="20" color="#55ff55">
    **Zellen-Andock-Port**<br />Der zweite Zellen-Andock-Port. Kann ersetzt werden durch: <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:coolant_input_port" />, <ItemLink id="overdrive:steam_output_port" /> oder <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="14" y="8" z="20" color="#55ff55">
    **Vakuum-Port**<br />Braucht 90 % Vakuum aus dem Pumpennetz. Kann ersetzt werden durch: <ItemLink id="overdrive:cell_docking_port" />, <ItemLink id="overdrive:coolant_input_port" />, <ItemLink id="overdrive:steam_output_port" /> oder <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="15" y="8" z="20" color="#55ff55">
    **Kühlmittel-Eingang**<br />Wasser oder Kryofluid für den Kühlkreislauf. Kann ersetzt werden durch: <ItemLink id="overdrive:cell_docking_port" />, <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:steam_output_port" /> oder <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="16" y="8" z="20" color="#55ff55">
    **Dampf-Ausgang**<br />Sammelt das aufgeheizte Kühlmittel. Kann ersetzt werden durch: <ItemLink id="overdrive:cell_docking_port" />, <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:coolant_input_port" /> oder <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="17" y="8" z="20" color="#55ff55">
    **Energie-Ausgang**<br />Schiebt die erzeugte Energie zu allen Nachbarn. Kann ersetzt werden durch: <ItemLink id="overdrive:cell_docking_port" />, <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:coolant_input_port" /> oder <ItemLink id="overdrive:steam_output_port" />.
  </BlockAnnotation>
</GameScene>

## Benötigte Bauteile

| Block | Anzahl |
|---|---|
| <ItemLink id="overdrive:annihilation_controller" /> | 1, auf der Wand |
| <ItemLink id="overdrive:cell_docking_port" /> | 2 |
| <ItemLink id="overdrive:annihilation_vacuum_port" /> | 1 |
| <ItemLink id="overdrive:coolant_input_port" /> | 1 |
| <ItemLink id="overdrive:steam_output_port" /> | 1 |
| <ItemLink id="overdrive:annihilation_energy_output" /> | 1 |
| <ItemLink id="overdrive:annihilation_heavy_casing" /> | der Rest der Wand (2125) |
| <ItemLink id="overdrive:reactor_core_block" /> | 27 |
| <ItemLink id="overdrive:core_containment_glass" /> | 98 |

## Betrieb

| Eigenschaft | Wert |
|---|---|
| Interner Puffer | 16.000.000.000.000 FE |
| Benötigtes Vakuum | 90 % |
| Brennrate | ab 0,1, im Controller-GUI einstellbar |
| Standardrezept | 10 mB gekühlte Antiprotonen + 10 mB Positronen |
| Energie pro Tick | 285.714 FE x Brennrate |
| Temperaturgrenze | 5.000 K |
| Grenztemperatur | 3.000 K |

Der Reaktor erwärmt sich mit `19 x Wurzel(Brennrate)` Kelvin pro Tick. Der
Kühlkreislauf nimmt wieder Wärme heraus: Wasser entfernt 1 K pro mB, Kryofluid
50 K pro mB. Der <ItemLink id="overdrive:steam_output_port" /> hat drei
Ableitmodi (Idle, Dump, Dump Excess), die sich in seinem GUI umschalten lassen.

Jeder <ItemLink id="overdrive:cell_docking_port" /> fasst 2.000.000 mB. Ein Port
nimmt Antiprotonen auf, der andere Positronen.

## Ausfall

Überhitzt der Kern auf 5.000 K, schmilzt er: Der gesamte 5x5x5-Kern wird zu
<ItemLink id="overdrive:corium_block" /> und eine Grube wird in den Boden
gesprengt. Der Kern ist nicht rückgewinnbar, der Reaktor muss neu gebaut
werden. Der Ausfallmodus lässt sich in der gemeinsamen Konfiguration zwischen
Meltdown, Explosion und keinem Fehler umstellen.

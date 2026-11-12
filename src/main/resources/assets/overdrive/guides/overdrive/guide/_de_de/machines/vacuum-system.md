---
navigation:
  title: Vakuumsystem
  icon: overdrive:vacuum_pump
  parent: index.md
item_ids:
  - overdrive:vacuum_pump
  - overdrive:pump_casing
  - overdrive:vacuum_energy_port
  - overdrive:vacuum_output_port
  - overdrive:vacuum_pipe
categories:
  - Maschinen
---

# Vakuumsystem

Die <ItemLink id="overdrive:vacuum_pump" /> evakuiert ein Netz aus
<ItemLink id="overdrive:vacuum_pipe" />. Jeder Multiblock, der Vakuum braucht –
Beschleuniger, AD-Ring, ELENA-Ring, Elektronenkanone und Annihilationsreaktor —
liest seinen Vakuumpegel aus diesem Netz.

Eine Maschine arbeitet ab **90 % Vakuum**, die Elektronenkanone braucht
**99 %**.

## Bauart

Die Pumpe ist die Mitte eines massiven **3x3x3-Blocks**, der *hinter* der Pumpe
gebaut wird, also entgegen der Blickrichtung der Pumpe. Der Block direkt hinter
der Pumpe gehört zur Struktur, der Würfel reicht daher drei Blöcke tief.

Der Würfel enthält genau **zwei Ports** und sonst nichts:

* ein <ItemLink id="overdrive:vacuum_energy_port" /> für die Energieversorgung,
* ein <ItemLink id="overdrive:vacuum_output_port" /> für das Vakuumnetz.

Beide Ports können an **jeder** freien Stelle des Würfels stehen. Jedes Gehäuse
im Würfel kann durch einen Port ersetzt werden - die Szene zeigt nur eine
mögliche Anordnung. Die Pumpe braucht genau einen Energie-Port und einen
Ausgangs-Port.

Alle übrigen Blöcke sind <ItemLink id="overdrive:pump_casing" /> oder
<ItemLink id="overdrive:vacuum_pipe" />. Luft im Würfel macht die Pumpe ungültig.

<GameScene zoom="3" interactive={true}>
  <ImportStructure src="../structures/vacuum_pump.snbt" />
  <PortSlots controller="overdrive:vacuum_pump" />
  <BlockAnnotation x="1" y="1" z="0" color="#ffaa00">
    **Vakuumpumpe**<br />Blickt von der Struktur weg.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="1" z="1" color="#55ff55">
    **Energie-Port**<br />Versorgt die Pumpe mit Forge Energy. Kann ersetzt werden durch: <ItemLink id="overdrive:vacuum_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="1" z="2" color="#55ff55">
    **Vakuum-Ausgang**<br />Führt ins Vakuumnetz. Kann ersetzt werden durch: <ItemLink id="overdrive:vacuum_energy_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="1" z="3" color="#5599ff">
    **Vakuumrohr**<br />Braucht ein Rohr oder einen Maschinen-Port neben dem Ausgang, sonst meldet die Pumpe ein Leck.
  </BlockAnnotation>
</GameScene>

## Energie und Volumen

| Eigenschaft | Wert |
|---|---|
| Energiespeicher | 200.000 FE |
| Grundverbrauch | 100 FE/t |
| Verbrauch pro Volumenblock | 0,2 FE/t (konfigurierbar) |
| Verbrauch im stabilen Zustand | 10 % davon |
| Evakuierungszeit | ca. 7,4 Ticks pro Volumenblock |

Das Volumen des Netzes ist die Pumpe selbst (27 Blöcke) plus alle
angeschlossenen Rohre plus die Blöcke jedes angeschlossenen Multiblocks:

| Maschine | Volumen |
|---|---|
| Teilchenbeschleuniger | 1500 |
| AD-Ring | 168 |
| ELENA-Ring | 72 |
| Annihilationsreaktor | 1532 |

Je größer das Netz, desto länger dauert die Evakuierung und desto mehr Energie
wird verbraucht. Die Skalierung kann in der gemeinsamen Konfiguration von
linear auf exponentiell umgestellt werden.

## Rohre und Netzregeln

* <ItemLink id="overdrive:vacuum_pipe" /> verbindet die Pumpe mit den
  Vakuum-Ports der Maschinen. Ein Rohr kann in alle vier Richtungen benutzt werden.
* Mit dem <ItemLink id="overdrive:wrench" /> lassen sich einzelne Rohrseiten
  abschalten. Eine abgeschaltete Seite trennt das Netz.
* Der <ItemLink id="overdrive:vacuum_output_port" /> muss an ein Rohr oder einen
  Maschinen-Vakuumport grenzen. Ohne diese Verbindung springt die Pumpe auf den
  Status **Leck** und arbeitet nicht mehr.
* Ohne Pumpe verliert das Netz nach einer kurzen Gnadenfrist 0,01 Vakuum pro Tick.

## Rezepte

<RecipeFor id="overdrive:vacuum_pump" />
<RecipeFor id="overdrive:vacuum_pipe" />

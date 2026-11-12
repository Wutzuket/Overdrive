---
navigation:
  title: Overdrive-Handbuch
  icon: overdrive:wrench
categories:
  - Überblick
---

# Overdrive-Handbuch

Overdrive bringt Hochenergie-, Antimatter- und Kältetechnik nach Minecraft.
Dieses Handbuch erklärt jede Maschine und jeden Multiblock der Mod – inklusive
der exakten Blockbauart, die du errichten musst.

Halte <KeyBind id="key.guideme.guide" />, während du einen Gegenstand
anschaust, um direkt auf die passende Seite zu springen. Jeder als
<ItemLink id="overdrive:vacuum_pump" /> geschriebene Gegenstandsname ist ein
Link auf seine eigene Seite.

## Die Produktionskette

1. **Rohstoffe** – hacke <ItemLink id="overdrive:aluminum_ore" />,
   <ItemLink id="overdrive:iridium_ore" /> und <ItemLink id="overdrive:tungsten_ore" />
   und schmelze sie zu Barren.
2. **Vakuum** – baue eine <ItemLink id="overdrive:vacuum_pump" /> und verbinde
   sie mit <ItemLink id="overdrive:vacuum_pipe" /> mit allen Maschinen, die
   Vakuum brauchen. Alle fortgeschrittenen Maschinen arbeiten ab 90 % Vakuum.
3. **Antimatter-Kette** – der <ItemLink id="overdrive:particle_accelerator_controller" />
   verwandelt flüssiges Iridium in Antiprotonen, der
   <ItemLink id="overdrive:ad_controller" /> bremst sie ab und der
   <ItemLink id="overdrive:elena_controller" /> kühlt sie herunter.
   Die <ItemLink id="overdrive:electron_gun" /> erzeugt Positronen.
4. **Vernichtung** – der <ItemLink id="overdrive:annihilation_controller" />
   verbrennt Antiprotonen und Positronen zu roher Energie. Er braucht Kühlmittel
   vom <ItemLink id="overdrive:coolant_input_port" /> und einen funktionierenden
   <ItemLink id="overdrive:cooling_tower_controller" />, damit der Kern kühl bleibt.
5. **Kühlung** – der <ItemLink id="overdrive:cryo_distiller" /> produziert
   Kryofluid, der <ItemLink id="overdrive:cooling_tower_controller" /> kondensiert
   Dampf zurück zu Wasser bzw. überhitztes Kryofluid zu Kryofluid.

Antimatter ist keine Minecraft-Flüssigkeit. Es fließt durch ein eigenes
<ItemLink id="overdrive:antimatter_pipe_basic" />-Netz und wird in
<ItemLink id="overdrive:antimatter_tank" />-Blöcken und
<ItemLink id="overdrive:empty_antimatter_cell" />-Zellen gelagert.

## Maschinen

<Row>
  <ItemImage id="overdrive:vacuum_pump" />
  <ItemImage id="overdrive:particle_accelerator_controller" />
  <ItemImage id="overdrive:ad_controller" />
  <ItemImage id="overdrive:elena_controller" />
</Row>

<Row>
  <ItemImage id="overdrive:electron_gun" />
  <ItemImage id="overdrive:annihilation_controller" />
  <ItemImage id="overdrive:cooling_tower_controller" />
  <ItemImage id="overdrive:cryo_distiller" />
</Row>

<Row>
  <ItemImage id="overdrive:circuit_press" />
  <ItemImage id="overdrive:coal_generator" />
  <ItemImage id="overdrive:scanner" />
  <ItemImage id="overdrive:antimatter_tank" />
</Row>

| Maschine | Zweck |
|---|---|
| [Vakuumsystem](machines/vacuum-system.md) | Evakuiert das Vakuumnetz für alle Multiblocks. |
| [Teilchenbeschleuniger](machines/particle-accelerator.md) | Flüssiges Iridium zu Antiprotonen. |
| [AD-Ring](machines/ad-ring.md) | Bremst Antiprotonen ab. |
| [ELENA-Ring](machines/elena-ring.md) | Kühlt Antiprotonen herunter. |
| [Elektronenkanone](machines/electron-gun.md) | Elektronenkanone für Positronen. |
| [Annihilationsreaktor](machines/annihilation-reactor.md) | Verbrennt Antimatter zu Energie. |
| [Kühlturm](machines/cooling-tower.md) | Kondensiert Dampf und heißes Kryofluid. |
| [Kryodestillator](machines/cryo-distiller.md) | Produziert Kryofluid. |
| [Circuit Press](machines/circuit-press.md) | Fertigt Platinen und Scanner-Panels. |
| [Kohlekraftwerk](machines/coal-generator.md) | Kleiner Startgenerator. |
| [Scanner](machines/scanner.md) | Fertigt Multiblock-Bauteile in 3x3x3-Mustern. |
| [Antimatter-Transport](machines/antimatter-transport.md) | Rohre, Tanks und Zellen für Antimatter. |
| [Hologramm-Blöcke](machines/hologram-blocks.md) | Visualisieren Bauarten in der Welt. |
| [Materialien und Fluide](resources/materials.md) | Erze, Barren, Blöcke und Fluide. |

## Alle Seiten

<SubPages />

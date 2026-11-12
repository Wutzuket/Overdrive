---
navigation:
  title: Scanner
  icon: overdrive:scanner
  parent: index.md
item_ids:
  - overdrive:scanner
  - overdrive:scanner_panel
categories:
  - Machines
---

# Scanner

The scanner recognises multiblock components and crafts them. Many of the
high tier machine parts of Overdrive can only be produced with it.

## Structure

The scanner looks for a **3x3x3 pattern**. Its centre is **three blocks in
front of the scanner and one block up**. Build the pattern that JEI shows for
the recipe you want to make – mirrored layouts are allowed as well.

Trigger the scan with a **redstone signal**. The scanner animates, consumes
the pattern and drops the result.

<BlockImage id="overdrive:scanner" scale="2" />

## Operation

| Property | Value |
|---|---|
| Energy buffer | 100,000 FE |
| Energy per scan | 5,000 FE |
| Item slots | 4 |

Some recipes need additional items. You can either drop them into the empty
cells of the pattern – they rest on the block below – or put them into the
four item slots of the scanner GUI, which opens when you right click the
scanner. The items are consumed when the scan starts. Items that are still in
the slots when you break the scanner are dropped.

## Scanner panel

The <ItemLink id="overdrive:scanner_panel" /> is the tier marker of the
scanner. It is pressed in the [circuit press](circuit-press.md) and is
required by recipes such as the cooling tower controller.

<RecipeFor id="overdrive:scanner_panel" />

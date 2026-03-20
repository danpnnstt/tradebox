# Trade Box

**Trade Box** is a NeoForge mod for Minecraft 1.21.1 that adds a fully configurable in-game shop system. Players can purchase enchanted books and potions, and sell back unwanted items for a partial refund — all driven by JSON config files with no code changes required.

---

## Features

### Blocks

#### Trade Box
A shop block for purchasing enchanted books. Right-click to open the GUI, browse the list of available enchantments, see what each one costs, and buy if you have the required items in your inventory. Enchantments and their costs are fully defined in `tradebox_enchantments.json`.

#### Potion Trade Block
A shop block for purchasing potions. Supports regular, splash, and lingering variants. Empty glass bottles can also be purchased separately. Everything is configured in `tradebox_potions.json`.

#### Seller Block
Allows players to sell back enchanted books and potions for a partial refund. The base refund percentage is configurable. Players wearing armor enchanted with **Smooth Seller** receive a higher refund percentage.

---

### Custom Enchantment: Smooth Seller

- Can be applied to armor
- Increases the refund percentage from the Seller Block by a configurable amount per level
- Maximum level: 15 (configurable)
- Default: 50% base refund + 10% per level, capped at level 15 (max 200% refund rate before item cost cap)

---

### Configuration

All three JSON config files are generated automatically on first run and can be edited freely. Changes can be applied live using the `/tradebox reload` command — no server restart required.

#### `tradebox_enchantments.json`
Defines every enchantment available in the Trade Box. Each entry specifies:
- `enchant_id` — Minecraft enchantment registry ID (e.g. `minecraft:mending`)
- `level` — The level of the enchanted book sold
- `cost` — A list of items and quantities required to purchase
- `is_sellable` / `is_refundable` — Whether the enchantment appears in the Seller Block

Comes pre-configured with 100+ vanilla enchantments across all levels.

#### `tradebox_potions.json`
Defines every potion available in the Potion Trade Block. Each entry specifies:
- `potion_id` — Potion registry ID (e.g. `minecraft:swiftness`)
- `type` — `regular`, `splash`, or `lingering`
- `cost` — Items required (typically glass bottles, emeralds, gold ingots)
- `is_sellable` / `is_refundable` — Whether it appears in the Seller Block

Comes pre-configured with 100+ potion entries covering all vanilla variants including 1.21 additions (Wind Charged, Weaving, Oozing, Infested).

#### `tradebox_config.json`
Controls Seller Block refund behavior:
- `default_refund_percentage` — Base refund % (default: `50`)
- `enchant_level_increase` — % increase per Smooth Seller level (default: `10`)
- `max_enchant_level` — Maximum Smooth Seller level counted toward refund (default: `15`)

Any item in the game registry can be used as currency — not just emeralds.

---

### Admin Commands

| Command | Description |
|---|---|
| `/tradebox reload` | Reloads all three config files live, no restart needed. Reports total entries loaded. |
| `/tradebox export` | Exports all available enchantments from the game registry to `tradebox_enchantment_export.csv`. Useful for building your config. |

---

### Crafting Recipes

All three blocks have crafting recipes. Check your recipe book in-game or use a mod like Just Enough Items (JEI) to view them.

---

## Requirements

- **Minecraft:** 1.21.1
- **NeoForge:** 21.1.211 or newer (21.1.x range)
- No other mod dependencies required

---

## Installation

1. Install [NeoForge](https://neoforged.net/) for Minecraft 1.21.1
2. Drop the Trade Box `.jar` file into your `mods` folder
3. Launch the game — config files are generated automatically on first run
4. Edit the JSON config files in your `config` folder to customize trades
5. Use `/tradebox reload` to apply changes without restarting

---

## License

MIT

# Changelog

All notable changes to **Loot Piles** (`lootpiles`) are documented in this file.

Per-version release notes for CurseForge uploads also live in `docs/changelogs/<version>.md`.

---

## Current Mod State (v1.0.1)

NeoForge mod for **Minecraft 1.21.1** that adds tiered scrap pile loot blocks with shared cooldowns and a performance-focused server architecture.

| Field | Value |
|-------|-------|
| Mod ID | `lootpiles` |
| Package | `com.nerdsquadrados.lootpiles` |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.64+ |
| Java | 21 |
| License | MIT |

### Gameplay

- Five scrap pile tiers: **Common**, **Uncommon**, **Rare**, **Epic**, **Legendary**
- Right-click with an **empty hand** to search an active pile (`depleted=false`)
- **Shared cooldown per block position** (not per player)
- Guaranteed drop of **`metal_scrap`** on every valid search, plus weighted bonus loot rolls
- Pile switches to a **flattened depleted model** while on cooldown, keeping the original tier color
- Normal players **cannot break** piles; operators / creative can remove them
- **Slab hitbox** (8 px tall) regardless of the taller visual model
- Cooldown entry is **cleared when the block is broken**, so a new pile at the same coordinates starts fresh

### Unified tier configuration (JSON)

Each tier is configured by an editable file:

```text
config/lootpiles/scrap_piles/
├── common.json
├── uncommon.json
├── rare.json
├── epic.json
└── legendary.json
```

Example:

```json
{
  "cooldown": 600,
  "scrapDrop": { "min": 1, "max": 3 },
  "rolls": { "min": 2, "max": 4 },
  "entries": [
    { "item": "minecraft:iron_nugget", "weight": 20, "min": 3, "max": 8 },
    { "item": "minecraft:coal", "weight": 15, "min": 2, "max": 6 }
  ]
}
```

| Field | Description |
|-------|-------------|
| `cooldown` | Recharge time in **seconds** after searching |
| `scrapDrop` | Guaranteed `lootpiles:metal_scrap` count range |
| `rolls` | Number of extra weighted loot rolls |
| `entries` | Weighted item pool (`weight`, `min`, `max`) |

**Loader behavior:**

- Parsed with **plain Gson** — no vanilla `LootTable`, `ResourceKey`, or datapack dependency
- Items resolved via `BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(...))`
- **Reloaded from disk on every valid click** (no restart required)
- Default files copied from `defaults/lootpiles/scrap_piles/` inside the JAR on first run
- Legacy formats (simple array or old loot-table JSON) are regenerated with a `.bak` backup

**Default `scrapDrop` ranges:**

| Tier | scrapDrop | Default cooldown |
|------|-----------|------------------|
| Common | 1–3 | 600 s (10 min) |
| Uncommon | 3–6 | 1200 s (20 min) |
| Rare | 7–9 | 1800 s (30 min) |
| Epic | 9–12 | 3000 s (50 min) |
| Legendary | 13–16 | 6000 s (100 min) |

**Loot roll logic:**

1. Roll `metal_scrap` count between `scrapDrop.min` and `scrapDrop.max` → always dropped
2. Roll roll count between `rolls.min` and `rolls.max`
3. For each roll, pick an `entries` item by accumulated **weight** and drop a random stack size between that entry's `min` and `max`

### Visual design

- **Active pile:** Blockbench model (`scrap_pile.json`), 12 cubes, `lootpile_base.png` (64×64 atlas), `"tintindex": 0` on all faces
- **Depleted pile:** flattened model (`scrap_pile_empty.json`) via blockstate when `depleted=true`
- **Tier tint colors:** Common `#909090`, Uncommon `#1EFF00`, Rare `#0070DD`, Epic `#A335EE`, Legendary `#FF8000`
- **Item display names** colored via `CUSTOM_NAME` data components (inventory + JEI)
- **`metal_scrap` texture** based on vanilla netherite scrap silhouette, recolored to oxidized metal
- **No ambient particles** — static block for performance

### Architecture

- Central `ScrapCooldownManager` (`SavedData`) — no ticking block entities
- `ScrapPileLootConfig` reads tier JSON from `FMLPaths.CONFIGDIR`
- `ScrapLootService` handles weighted rolls and item entity spawning
- Admin commands: `/scrappile` (alias `/lootpiles`) — spawn, reset, status (OP level 2)
- Optional **JADE** and **JEI** integrations (`localRuntime` in dev only)
- GitHub Actions: **Build** on push/PR, **Publish Mod to CurseForge** on version tags

### Registered content

| ID | Type |
|----|------|
| `lootpiles:scrap_pile` | Block (`tier`, `depleted` properties) |
| `lootpiles:scrap_pile_common` … `legendary` | BlockItem (one per tier) |
| `lootpiles:metal_scrap` | Item |

Localization: `en_us.json`, `pt_br.json`

---

## [1.0.1] - 2026-06-26

### Added

- Unified per-tier JSON configuration at `config/lootpiles/scrap_piles/` (cooldown, `scrapDrop`, `rolls`, weighted `entries`)
- Guaranteed `metal_scrap` drop on every valid search
- Dynamic JSON reload on each interaction
- Default config generation and legacy format migration (`.bak` backup)
- Cooldown cleanup when a pile is broken

### Changed

- Loot and cooldown settings consolidated into editable tier JSON files
- Cooldown read from `"cooldown"` in seconds per tier JSON
- Bonus loot uses Gson + weighted rolls instead of vanilla loot tables

### Removed

- Server TOML cooldown config
- Vanilla loot table loading for player-editable loot
- Ambient particles on active piles

---

## [1.0.0] - 2026-06-26

### Added

- Scrap pile block with five loot tiers (Common to Legendary)
- Centralized per-dimension cooldown manager using SavedData (no ticking block entities)
- Server TOML config for tier cooldown durations
- Admin commands: `/scrappile spawn`, `/scrappile reset`, `/scrappile status` (alias `/lootpiles`)
- Blockbench scrap pile model with 64×64 `lootpile_base` texture and tier tinting via `tintindex`
- Depleted (empty) pile model that flattens geometry while preserving tier color
- Tier-colored item display names in inventory and JEI via `CUSTOM_NAME` data components
- `metal_scrap` loot item
- Optional JADE and JEI integrations for development environments
- GitHub Actions CI build and CurseForge publish workflow

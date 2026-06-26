# Changelog

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

Per-version release notes for CurseForge uploads: `docs/changelogs/<version>.md`.

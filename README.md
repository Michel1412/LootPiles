# Loot Piles — Dual Version Repository

This repository hosts **two independent Gradle projects** for the same mod:

| Folder | Minecraft | Java | Loader |
|--------|-----------|------|--------|
| [`v1.20.1/`](v1.20.1/) | 1.20.1 | **17** | NeoForge 47.1.x (legacy) |
| [`v1.21.1/`](v1.21.1/) | 1.21.1 | **21** | NeoForge 21.1.x |

Each subfolder is a **standalone project** with its own `settings.gradle`, wrapper, and toolchain. Run Gradle commands **from inside the version folder**:

```powershell
# Minecraft 1.21.1 (Java 21)
cd v1.21.1
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat genIntellijRuns

# Minecraft 1.20.1 (Java 17)
cd v1.20.1
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat genIntellijRuns
```

## Configuration

Both versions generate `config/lootpiles-config.json` on first launch with **5 tiers**:

`common`, `uncommon`, `rare`, `epic`, `legendary`

Each tier defines:

- `color` — HEX tint applied via `tintindex: 0`
- `loot_table` — vanilla loot table id
- `min_items` / `max_items` — drop count bounds
- `experience` — XP orb reward

## Assets

Shared grayscale textures + unified block/item models live under each project's:

`src/main/resources/assets/lootpiles/`

Colors are applied at runtime through `RegisterColorHandlersEvent` (no dynamic client resource packs).

## License

See [LICENSE](LICENSE).

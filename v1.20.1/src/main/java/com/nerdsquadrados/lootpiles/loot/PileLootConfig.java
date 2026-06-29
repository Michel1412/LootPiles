package com.nerdsquadrados.lootpiles.loot;

import java.util.List;

public record PileLootConfig(
        int cooldownSeconds,
        IntRange scrapDrop,
        IntRange rolls,
        List<PileLootEntry> entries,
        boolean useDefaultScrap
) {
    public long cooldownTicks() {
        return Math.max(20L, (long) cooldownSeconds * 20L);
    }

    public int cooldownMinutes() {
        return Math.max(1, (cooldownSeconds + 59) / 60);
    }
}

package com.nerdsquadrados.lootpiles.loot;

import java.util.List;

public record ScrapTierConfig(
        int cooldownSeconds,
        int scrapDropMin,
        int scrapDropMax,
        int rollsMin,
        int rollsMax,
        List<ScrapTierLootEntry> entries
) {
    public long cooldownTicks() {
        return Math.max(20L, (long) cooldownSeconds * 20L);
    }

    public int cooldownMinutes() {
        return Math.max(1, (cooldownSeconds + 59) / 60);
    }
}

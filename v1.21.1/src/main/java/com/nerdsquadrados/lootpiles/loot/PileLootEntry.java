package com.nerdsquadrados.lootpiles.loot;

import net.minecraft.resources.ResourceLocation;

public record PileLootEntry(ResourceLocation item, int weight, int min, int max) {
    public PileLootEntry {
        weight = Math.max(1, weight);
        min = Math.max(0, min);
        max = Math.max(min, max);
    }
}

package com.nerdsquadrados.lootpiles.loot;

import net.minecraft.util.RandomSource;

public record IntRange(int min, int max) {
    public IntRange {
        min = Math.max(0, min);
        max = Math.max(min, max);
    }

    public int roll(RandomSource random) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }
}

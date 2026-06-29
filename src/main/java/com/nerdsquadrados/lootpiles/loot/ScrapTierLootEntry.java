package com.nerdsquadrados.lootpiles.loot;

import net.minecraft.world.item.Item;

public record ScrapTierLootEntry(Item item, int weight, int min, int max) {
}

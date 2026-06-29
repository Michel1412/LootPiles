package com.nerdsquadrados.lootpiles.loot;

import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinition;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ScrapLootService {
    private ScrapLootService() {
    }

    public static void spawnLoot(ServerLevel level, BlockPos pos, ScrapPileDefinition definition, Player player) {
        PileLootConfig config = definition.lootConfig();
        RandomSource random = level.getRandom();
        Vec3 spawnPos = Vec3.atBottomCenterOf(pos.above());

        Item scrapItem = resolveScrapItem(definition);
        int scrapCount = config.scrapDrop().roll(random);
        if (scrapCount > 0 && scrapItem != Items.AIR) {
            spawnItemStack(level, spawnPos, random, new ItemStack(scrapItem, scrapCount));
        }

        int rollCount = config.rolls().roll(random);
        List<PileLootEntry> entries = config.entries();
        for (int i = 0; i < rollCount; i++) {
            PileLootEntry entry = pickWeighted(entries, random);
            if (entry == null) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(entry.item());
            if (item == null || item == Items.AIR) {
                continue;
            }
            int count = new IntRange(entry.min(), entry.max()).roll(random);
            if (count > 0) {
                spawnItemStack(level, spawnPos, random, new ItemStack(item, count));
            }
        }
    }

    private static Item resolveScrapItem(ScrapPileDefinition definition) {
        if (definition.useDefaultScrap()) {
            return ScrapPileRegistry.METAL_SCRAP.get();
        }
        return ScrapPileRegistry.byId(definition.id())
                .map(registered -> (Item) registered.scrapItem().get())
                .orElseGet(() -> ScrapPileRegistry.METAL_SCRAP.get());
    }

    private static PileLootEntry pickWeighted(List<PileLootEntry> entries, RandomSource random) {
        if (entries.isEmpty()) {
            return null;
        }
        int totalWeight = entries.stream().mapToInt(PileLootEntry::weight).sum();
        if (totalWeight <= 0) {
            return entries.get(0);
        }
        int roll = random.nextInt(totalWeight);
        for (PileLootEntry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry;
            }
        }
        return entries.get(entries.size() - 1);
    }

    private static void spawnItemStack(ServerLevel level, Vec3 spawnPos, RandomSource random, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(
                level,
                spawnPos.x + (random.nextDouble() - 0.5D) * 0.4D,
                spawnPos.y,
                spawnPos.z + (random.nextDouble() - 0.5D) * 0.4D,
                stack
        );
        itemEntity.setDefaultPickUpDelay();
        itemEntity.setDeltaMovement(
                (random.nextDouble() - 0.5D) * 0.1D,
                0.2D,
                (random.nextDouble() - 0.5D) * 0.1D
        );
        level.addFreshEntity(itemEntity);
    }
}

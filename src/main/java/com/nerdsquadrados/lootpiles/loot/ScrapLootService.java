package com.nerdsquadrados.lootpiles.loot;

import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ScrapLootService {
    private ScrapLootService() {
    }

    public static void spawnLoot(ServerLevel level, BlockPos pos, ScrapPileDefinition definition, Item scrapItem) {
        ScrapTierConfig config = definition.lootConfig();
        RandomSource random = level.getRandom();
        Vec3 spawnPos = Vec3.atBottomCenterOf(pos.above());

        int scrapCount = config.scrapDropMin();
        if (config.scrapDropMax() > config.scrapDropMin()) {
            scrapCount += random.nextInt(config.scrapDropMax() - config.scrapDropMin() + 1);
        }
        spawnItemStack(level, spawnPos, random, new ItemStack(scrapItem, scrapCount));

        if (config.entries().isEmpty()) {
            return;
        }

        int rollCount = config.rollsMin();
        if (config.rollsMax() > config.rollsMin()) {
            rollCount += random.nextInt(config.rollsMax() - config.rollsMin() + 1);
        }

        for (int roll = 0; roll < rollCount; roll++) {
            ScrapTierLootEntry entry = pickWeightedEntry(config.entries(), random);
            if (entry == null) {
                continue;
            }

            int count = entry.min();
            if (entry.max() > entry.min()) {
                count += random.nextInt(entry.max() - entry.min() + 1);
            }

            spawnItemStack(level, spawnPos, random, new ItemStack(entry.item(), count));
        }
    }

    private static ScrapTierLootEntry pickWeightedEntry(List<ScrapTierLootEntry> entries, RandomSource random) {
        int totalWeight = 0;
        for (ScrapTierLootEntry entry : entries) {
            totalWeight += entry.weight();
        }
        if (totalWeight <= 0) {
            return null;
        }

        int target = random.nextInt(totalWeight);
        int cumulative = 0;
        for (ScrapTierLootEntry entry : entries) {
            cumulative += entry.weight();
            if (target < cumulative) {
                return entry;
            }
        }
        return entries.getLast();
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

package com.nerdsquadrados.lootpiles.loot;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.block.ScrapTier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ScrapLootService {
    private ScrapLootService() {
    }

    public static void spawnLoot(ServerLevel level, BlockPos pos, ScrapTier tier, Player player) {
        ResourceLocation tableId = ResourceLocation.fromNamespaceAndPath(
                LootPiles.MOD_ID,
                "chests/scrap_pile_" + tier.getSerializedName()
        );

        LootTable lootTable = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, tableId));

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(pos))
                .withLuck(player.getLuck())
                .create(LootContextParamSets.CHEST);

        List<ItemStack> drops = lootTable.getRandomItems(params);
        RandomSource random = level.getRandom();
        Vec3 spawnPos = Vec3.atBottomCenterOf(pos.above());

        for (ItemStack stack : drops) {
            if (stack.isEmpty()) {
                continue;
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
}

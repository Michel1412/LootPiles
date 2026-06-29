package com.nerdsquadrados.lootpiles.cooldown;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScrapCooldownManager extends SavedData {
    public static final String DATA_NAME = LootPiles.MOD_ID + "_scrap_cooldowns";

    private final Map<BlockPos, Long> readyTimes = new HashMap<>();

    public ScrapCooldownManager() {
    }

    public static ScrapCooldownManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ScrapCooldownManager::load,
                ScrapCooldownManager::new,
                DATA_NAME
        );
    }

    public boolean isPlayerAllowedToLoot(BlockPos pos, long currentTime) {
        Long readyTime = readyTimes.get(pos);
        return readyTime == null || currentTime >= readyTime;
    }

    public void addCooldown(BlockPos pos, long readyTime) {
        readyTimes.put(pos, readyTime);
        setDirty();
    }

    public void removeCooldown(BlockPos pos) {
        if (readyTimes.remove(pos) != null) {
            setDirty();
        }
    }

    public void clearAll() {
        if (!readyTimes.isEmpty()) {
            readyTimes.clear();
            setDirty();
        }
    }

    public long getRemainingTicks(BlockPos pos, long currentTime) {
        Long readyTime = readyTimes.get(pos);
        if (readyTime == null) {
            return 0L;
        }
        return Math.max(0L, readyTime - currentTime);
    }

    public boolean isOnCooldown(BlockPos pos, long currentTime) {
        return getRemainingTicks(pos, currentTime) > 0L;
    }

    public void tickExpired(long gameTime, ServerLevel level) {
        Iterator<Map.Entry<BlockPos, Long>> iterator = readyTimes.entrySet().iterator();
        boolean changed = false;

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Long> entry = iterator.next();
            if (gameTime >= entry.getValue()) {
                BlockPos pos = entry.getKey();
                iterator.remove();
                changed = true;
                ScrapPileBlock.clearDepletedState(level, pos);
            }
        }

        if (changed) {
            setDirty();
        }
    }

    public int getActiveCooldownCount() {
        return readyTimes.size();
    }

    public java.util.Set<BlockPos> getCooldownPositions() {
        return java.util.Set.copyOf(readyTimes.keySet());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, Long> entry : readyTimes.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("X", entry.getKey().getX());
            entryTag.putInt("Y", entry.getKey().getY());
            entryTag.putInt("Z", entry.getKey().getZ());
            entryTag.putLong("ReadyTime", entry.getValue());
            list.add(entryTag);
        }
        tag.put("Cooldowns", list);
        return tag;
    }

    public static ScrapCooldownManager load(CompoundTag tag) {
        ScrapCooldownManager manager = new ScrapCooldownManager();
        ListTag list = tag.getList("Cooldowns", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            BlockPos pos = new BlockPos(entryTag.getInt("X"), entryTag.getInt("Y"), entryTag.getInt("Z"));
            manager.readyTimes.put(pos, entryTag.getLong("ReadyTime"));
        }
        return manager;
    }
}

package com.nerdsquadrados.lootpiles.cooldown;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;

public class ScrapCooldownTickHandler {
    private static final int CLEANUP_INTERVAL_TICKS = 20;
    private int tickCounter;

    public void onServerTick(TickEvent.ServerTickEvent event) {
        tickCounter++;
        if (tickCounter < CLEANUP_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            ScrapCooldownManager manager = ScrapCooldownManager.get(level);
            manager.tickExpired(level.getGameTime(), level);
        }
    }
}

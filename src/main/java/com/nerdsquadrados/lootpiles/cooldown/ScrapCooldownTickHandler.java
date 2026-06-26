package com.nerdsquadrados.lootpiles.cooldown;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ScrapCooldownTickHandler {
    private static final int CLEANUP_INTERVAL_TICKS = 20;
    private int tickCounter;

    @SubscribeEvent
    public void onServerTickPost(ServerTickEvent.Post event) {
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

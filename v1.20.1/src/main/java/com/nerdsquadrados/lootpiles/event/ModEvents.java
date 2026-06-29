package com.nerdsquadrados.lootpiles.event;

import com.nerdsquadrados.lootpiles.command.ScrapPileCommands;
import com.nerdsquadrados.lootpiles.cooldown.ScrapCooldownTickHandler;
import com.nerdsquadrados.lootpiles.config.PileConfigLoader;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModEvents {
    private final ScrapCooldownTickHandler tickHandler = new ScrapCooldownTickHandler();

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        PileConfigLoader.reload();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ScrapPileCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickHandler.onServerTick(event);
        }
    }
}

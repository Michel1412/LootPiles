package com.nerdsquadrados.lootpiles.event;

import com.nerdsquadrados.lootpiles.command.ScrapPileCommands;
import com.nerdsquadrados.lootpiles.cooldown.ScrapCooldownTickHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ModEvents {
    private final ScrapCooldownTickHandler tickHandler = new ScrapCooldownTickHandler();

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ScrapPileCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTickPost(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        tickHandler.onServerTickPost(event);
    }
}

package com.nerdsquadrados.lootpiles;

import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LootPiles.MOD_ID)
public class LootPiles {
    public static final String MOD_ID = "lootpiles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    static {
        ScrapPileRegistry.bootstrap();
    }

    public LootPiles(IEventBus modEventBus, ModContainer modContainer) {
        ScrapPileRegistry.BLOCKS.register(modEventBus);
        ScrapPileRegistry.ITEMS.register(modEventBus);

        NeoForge.EVENT_BUS.register(new com.nerdsquadrados.lootpiles.event.ModEvents());
    }
}

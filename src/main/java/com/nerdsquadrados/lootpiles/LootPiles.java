package com.nerdsquadrados.lootpiles;

import com.nerdsquadrados.lootpiles.config.ScrapPileConfig;
import com.nerdsquadrados.lootpiles.command.LootPilesArgumentTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LootPiles.MOD_ID)
public class LootPiles {
    public static final String MOD_ID = "lootpiles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public LootPiles(IEventBus modEventBus, ModContainer modContainer) {
        LootPilesBlocks.BLOCKS.register(modEventBus);
        LootPilesItems.ITEMS.register(modEventBus);
        LootPilesArgumentTypes.ARGUMENT_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ScrapPileConfig.SPEC);

        NeoForge.EVENT_BUS.register(new com.nerdsquadrados.lootpiles.event.ModEvents());
    }
}

package com.nerdsquadrados.lootpiles;

import com.nerdsquadrados.lootpiles.config.PileConfigLoader;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LootPiles.MOD_ID)
public class LootPiles {
    public static final String MOD_ID = "lootpiles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public LootPiles(IEventBus modEventBus, ModContainer modContainer) {
        PileConfigLoader.ensureDefaultPileFiles();
        ScrapPileRegistry.bootstrap();

        ScrapPileRegistry.BLOCKS.register(modEventBus);
        ScrapPileRegistry.ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(new com.nerdsquadrados.lootpiles.event.ModEvents());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PileConfigLoader::reload);
    }
}

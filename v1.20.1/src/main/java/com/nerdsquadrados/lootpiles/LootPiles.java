package com.nerdsquadrados.lootpiles;

import com.nerdsquadrados.lootpiles.config.PileConfigLoader;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LootPiles.MOD_ID)
public class LootPiles {
    public static final String MOD_ID = "lootpiles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public LootPiles() {
        PileConfigLoader.ensureDefaultPileFiles();
        ScrapPileRegistry.bootstrap();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ScrapPileRegistry.BLOCKS.register(modEventBus);
        ScrapPileRegistry.ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(new com.nerdsquadrados.lootpiles.event.ModEvents());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PileConfigLoader::reload);
    }
}

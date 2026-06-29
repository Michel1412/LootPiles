package com.nerdsquadrados.lootpiles.event;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinitionLoader;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = LootPiles.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCreativeTabEvents {
    private ModCreativeTabEvents() {
    }

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
                event.accept(registered.blockItem());
            }
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ScrapPileRegistry.METAL_SCRAP);
            for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
                event.accept(registered.scrapItem());
            }
        }
    }
}

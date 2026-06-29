package com.nerdsquadrados.lootpiles.client;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.item.ScrapMaterialItem;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = LootPiles.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
            Block block = registered.block().get();
            int tintColor = registered.definition().tintColor();
            event.register((state, level, pos, tintIndex) -> tintIndex == 0 ? tintColor : 0xFFFFFF, block);
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
            Item scrapItem = registered.scrapItem().get();
            Item blockItem = registered.blockItem().get();
            int tintColor = registered.definition().tintColor();

            event.register((stack, tintIndex) -> tintIndex == 0 ? tintColor : 0xFFFFFF, scrapItem, blockItem);
        }
    }
}

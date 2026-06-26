package com.nerdsquadrados.lootpiles.event;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.LootPilesItems;
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
            event.accept(LootPilesItems.SCRAP_PILE_COMMON);
            event.accept(LootPilesItems.SCRAP_PILE_UNCOMMON);
            event.accept(LootPilesItems.SCRAP_PILE_RARE);
            event.accept(LootPilesItems.SCRAP_PILE_EPIC);
            event.accept(LootPilesItems.SCRAP_PILE_LEGENDARY);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(LootPilesItems.METAL_SCRAP);
        }
    }
}

package com.nerdsquadrados.lootpiles.client;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.LootPilesBlocks;
import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import com.nerdsquadrados.lootpiles.block.ScrapTier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = LootPiles.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 0 || state == null) {
                return 0xFFFFFF;
            }
            ScrapTier tier = state.getValue(ScrapPileBlock.TIER);
            return switch (tier) {
                case COMMON -> 0x909090;
                case UNCOMMON -> 0x1EFF00;
                case RARE -> 0x0070DD;
                case EPIC -> 0xA335EE;
                case LEGENDARY -> 0xFF8000;
            };
        }, LootPilesBlocks.SCRAP_PILE.get());
    }
}

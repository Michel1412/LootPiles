package com.nerdsquadrados.lootpiles.client;

import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import com.nerdsquadrados.lootpiles.block.ScrapPileBlockItem;
import com.nerdsquadrados.lootpiles.config.PileConfigLoader;
import com.nerdsquadrados.lootpiles.item.ScrapMaterialItem;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import com.nerdsquadrados.lootpiles.LootPiles;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LootPiles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    private static final int DEFAULT_TINT = 0xFFFFFF;

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        DynamicPileModelMapper.registerAdditionalModels(event);
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
            event.register(ClientModEvents::tintBlock, registered.block().get());
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
            Item scrapItem = registered.scrapItem().get();
            Item blockItem = registered.blockItem().get();
            event.register(ClientModEvents::tintItem, scrapItem, blockItem);
        }
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        DynamicPileModelMapper.remapDynamicModels(event.getModels());
    }

    private static int tintBlock(BlockState state, net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos pos, int tintIndex) {
        if (tintIndex != 0) {
            return DEFAULT_TINT;
        }
        if (state.getBlock() instanceof ScrapPileBlock scrapPileBlock) {
            return resolvePileTint(scrapPileBlock.getPileId());
        }
        return DEFAULT_TINT;
    }

    private static int tintItem(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) {
            return DEFAULT_TINT;
        }
        Item item = stack.getItem();
        if (item instanceof ScrapMaterialItem scrapItem) {
            return resolvePileTint(scrapItem.getPileId());
        }
        if (item instanceof ScrapPileBlockItem blockItem) {
            return resolvePileTint(blockItem.getPileId());
        }
        return DEFAULT_TINT;
    }

    private static int resolvePileTint(String pileId) {
        if (pileId == null || pileId.isBlank()) {
            return DEFAULT_TINT;
        }
        return PileConfigLoader.getPile(pileId)
                .map(PileConfigLoader.LoadedPileConfig::tintColor)
                .or(() -> ScrapPileRegistry.byId(pileId).map(registered -> registered.definition().tintColor()))
                .orElse(DEFAULT_TINT);
    }
}

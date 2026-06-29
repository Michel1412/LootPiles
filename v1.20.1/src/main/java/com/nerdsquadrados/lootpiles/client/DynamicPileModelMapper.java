package com.nerdsquadrados.lootpiles.client;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.config.PileConfigLoader;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;

import java.util.Map;

/**
 * Remaps dynamically registered pile block/item ids to the shared models shipped in the mod jar.
 */
public final class DynamicPileModelMapper {
    public static final ResourceLocation SHARED_PILE_ITEM = new ResourceLocation(LootPiles.MOD_ID, "scrap_pile_item");
    public static final ResourceLocation SHARED_SCRAP_ITEM = new ResourceLocation(LootPiles.MOD_ID, "scrap_material_item");
    public static final ResourceLocation SHARED_BLOCK_MODEL = new ResourceLocation(LootPiles.MOD_ID, "block/scrap_pile");
    public static final ResourceLocation SHARED_DEPLETED_BLOCK_MODEL = new ResourceLocation(LootPiles.MOD_ID, "block/scrap_pile_depleted");

    private static final ModelResourceLocation SHARED_PILE_ITEM_INVENTORY = itemInventory(SHARED_PILE_ITEM);
    private static final ModelResourceLocation SHARED_SCRAP_ITEM_INVENTORY = itemInventory(SHARED_SCRAP_ITEM);

    private DynamicPileModelMapper() {
    }

    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(SHARED_PILE_ITEM_INVENTORY);
        event.register(SHARED_SCRAP_ITEM_INVENTORY);

        for (String pileId : PileConfigLoader.getPileIds()) {
            ResourceLocation pileItemId = pileItemId(pileId);
            ResourceLocation scrapItemId = scrapItemId(pileId);
            ResourceLocation blockId = pileBlockId(pileId);

            event.register(itemInventory(pileItemId));
            event.register(itemInventory(scrapItemId));
            event.register(blockVariant(blockId, "depleted=false"));
            event.register(blockVariant(blockId, "depleted=true"));
        }
    }

    public static void remapDynamicModels(Map<ResourceLocation, BakedModel> models) {
        BakedModel pileItemModel = resolvePileItemModel(models);
        BakedModel scrapItemModel = models.get(SHARED_SCRAP_ITEM_INVENTORY);
        BakedModel fullBlockModel = resolveFullBlockModel(models);
        BakedModel depletedBlockModel = resolveDepletedBlockModel(models);

        if (pileItemModel == null || scrapItemModel == null) {
            LootPiles.LOGGER.warn(
                    "Shared scrap pile item models were not found during baking (pile={}, scrap={})",
                    pileItemModel != null,
                    scrapItemModel != null
            );
            return;
        }

        if (fullBlockModel == null || depletedBlockModel == null) {
            LootPiles.LOGGER.warn(
                    "Shared scrap pile block models were not found during baking (full={}, depleted={})",
                    fullBlockModel != null,
                    depletedBlockModel != null
            );
        }

        for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
            String pileId = registered.definition().id();
            ResourceLocation pileItemId = pileItemId(pileId);
            ResourceLocation scrapItemId = scrapItemId(pileId);
            ResourceLocation blockId = pileBlockId(pileId);

            models.put(itemInventory(pileItemId), pileItemModel);
            models.put(itemInventory(scrapItemId), scrapItemModel);

            if (fullBlockModel != null) {
                models.put(blockVariant(blockId, "depleted=false"), fullBlockModel);
            }
            if (depletedBlockModel != null) {
                models.put(blockVariant(blockId, "depleted=true"), depletedBlockModel);
            }
        }
    }

    private static BakedModel resolvePileItemModel(Map<ResourceLocation, BakedModel> models) {
        BakedModel shared = models.get(SHARED_PILE_ITEM_INVENTORY);
        if (shared != null) {
            return shared;
        }

        for (String pileId : PileConfigLoader.getPileIds()) {
            BakedModel fallback = models.get(itemInventory(pileItemId(pileId)));
            if (fallback != null) {
                return fallback;
            }
        }
        return null;
    }

    private static BakedModel resolveFullBlockModel(Map<ResourceLocation, BakedModel> models) {
        BakedModel shared = models.get(blockModel(SHARED_BLOCK_MODEL));
        if (shared != null) {
            return shared;
        }

        for (String pileId : PileConfigLoader.getPileIds()) {
            BakedModel fallback = models.get(blockVariant(pileBlockId(pileId), "depleted=false"));
            if (fallback != null) {
                return fallback;
            }
        }
        return null;
    }

    private static BakedModel resolveDepletedBlockModel(Map<ResourceLocation, BakedModel> models) {
        BakedModel shared = models.get(blockModel(SHARED_DEPLETED_BLOCK_MODEL));
        if (shared != null) {
            return shared;
        }

        for (String pileId : PileConfigLoader.getPileIds()) {
            BakedModel fallback = models.get(blockVariant(pileBlockId(pileId), "depleted=true"));
            if (fallback != null) {
                return fallback;
            }
        }
        return null;
    }

    public static ResourceLocation pileItemId(String pileId) {
        return new ResourceLocation(LootPiles.MOD_ID, pileId + "_scrap_pile");
    }

    public static ResourceLocation scrapItemId(String pileId) {
        return new ResourceLocation(LootPiles.MOD_ID, pileId + "_scrap");
    }

    public static ResourceLocation pileBlockId(String pileId) {
        return new ResourceLocation(LootPiles.MOD_ID, pileId + "_scrap_pile");
    }

    public static ModelResourceLocation itemInventory(ResourceLocation itemId) {
        return new ModelResourceLocation(itemId, "inventory");
    }

    public static ModelResourceLocation blockVariant(ResourceLocation blockId, String variant) {
        return new ModelResourceLocation(blockId, variant);
    }

    public static ModelResourceLocation blockModel(ResourceLocation modelId) {
        return new ModelResourceLocation(modelId, "");
    }
}

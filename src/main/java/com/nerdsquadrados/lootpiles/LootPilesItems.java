package com.nerdsquadrados.lootpiles;

import com.nerdsquadrados.lootpiles.block.ScrapPileBlockItem;
import com.nerdsquadrados.lootpiles.block.ScrapTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LootPilesItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LootPiles.MOD_ID);

    public static final DeferredItem<Item> METAL_SCRAP = ITEMS.register(
            "metal_scrap",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static final DeferredItem<ScrapPileBlockItem> SCRAP_PILE_COMMON = registerTierItem("scrap_pile_common", ScrapTier.COMMON);
    public static final DeferredItem<ScrapPileBlockItem> SCRAP_PILE_UNCOMMON = registerTierItem("scrap_pile_uncommon", ScrapTier.UNCOMMON);
    public static final DeferredItem<ScrapPileBlockItem> SCRAP_PILE_RARE = registerTierItem("scrap_pile_rare", ScrapTier.RARE);
    public static final DeferredItem<ScrapPileBlockItem> SCRAP_PILE_EPIC = registerTierItem("scrap_pile_epic", ScrapTier.EPIC);
    public static final DeferredItem<ScrapPileBlockItem> SCRAP_PILE_LEGENDARY = registerTierItem("scrap_pile_legendary", ScrapTier.LEGENDARY);

    private static DeferredItem<ScrapPileBlockItem> registerTierItem(String name, ScrapTier tier) {
        return ITEMS.register(name, () -> new ScrapPileBlockItem(
                LootPilesBlocks.SCRAP_PILE.get(),
                tier,
                new Item.Properties().component(DataComponents.CUSTOM_NAME, tier.getDisplayNameComponent())
        ));
    }
}

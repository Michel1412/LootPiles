package com.nerdsquadrados.lootpiles.registry;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.config.PileConfigLoader;
import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import com.nerdsquadrados.lootpiles.block.ScrapPileBlockItem;
import com.nerdsquadrados.lootpiles.item.ScrapMaterialItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ScrapPileRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LootPiles.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LootPiles.MOD_ID);

    public static final DeferredItem<Item> METAL_SCRAP = ITEMS.register(
            "metal_scrap",
            () -> new Item(new Item.Properties())
    );

    private static final Map<String, RegisteredScrapPile> BY_ID = new LinkedHashMap<>();
    private static boolean bootstrapped = false;

    private ScrapPileRegistry() {
    }

    public record RegisteredScrapPile(
            ScrapPileDefinition definition,
            DeferredBlock<ScrapPileBlock> block,
            DeferredItem<ScrapMaterialItem> scrapItem,
            DeferredItem<ScrapPileBlockItem> blockItem
    ) {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }

        for (ScrapPileDefinition definition : ScrapPileDefinitionLoader.discoverDefinitions()) {
            registerDefinition(definition);
        }

        if (BY_ID.isEmpty()) {
            LootPiles.LOGGER.error("No scrap pile types were loaded from config/{}/", PileConfigLoader.PILES_DIR);
        } else {
            LootPiles.LOGGER.info("Loaded {} dynamic scrap pile definition(s)", BY_ID.size());
        }

        bootstrapped = true;
    }

    private static void registerDefinition(ScrapPileDefinition definition) {
        if (BY_ID.containsKey(definition.id())) {
            LootPiles.LOGGER.warn("Duplicate scrap pile id '{}', skipping", definition.id());
            return;
        }

        DeferredBlock<ScrapPileBlock> block = BLOCKS.register(
                definition.blockName(),
                () -> new ScrapPileBlock(
                        definition.id(),
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.METAL)
                                .strength(-1.0F, 3600000.0F)
                                .sound(SoundType.METAL)
                                .noOcclusion()
                )
        );

        DeferredItem<ScrapMaterialItem> scrapItem = ITEMS.register(
                definition.scrapName(),
                () -> new ScrapMaterialItem(definition.id(), new Item.Properties())
        );

        DeferredItem<ScrapPileBlockItem> blockItem = ITEMS.register(
                definition.blockName(),
                () -> new ScrapPileBlockItem(
                        block.get(),
                        definition.id(),
                        new Item.Properties()
                )
        );

        RegisteredScrapPile registered = new RegisteredScrapPile(definition, block, scrapItem, blockItem);
        BY_ID.put(definition.id(), registered);
    }

    public static Collection<RegisteredScrapPile> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static Optional<RegisteredScrapPile> byId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static Optional<RegisteredScrapPile> byBlock(Block block) {
        for (RegisteredScrapPile registered : BY_ID.values()) {
            if (registered.block().get() == block) {
                return Optional.of(registered);
            }
        }
        return Optional.empty();
    }

    public static Optional<RegisteredScrapPile> byScrapItem(Item item) {
        for (RegisteredScrapPile registered : BY_ID.values()) {
            if (registered.scrapItem().get() == item) {
                return Optional.of(registered);
            }
        }
        return Optional.empty();
    }

    public static ScrapPileDefinition getDefinition(String pileId) {
        return ScrapPileDefinitionLoader.loadDefinition(pileId);
    }

    public static Component createDisplayName(ScrapPileDefinition definition) {
        return Component.literal(definition.displayType() + " Scrap Pile")
                .withStyle(tierNameStyle(definition));
    }

    public static Component createScrapDisplayName(ScrapPileDefinition definition) {
        return Component.literal(definition.displayType() + " Scrap")
                .withStyle(tierNameStyle(definition));
    }

    public static Style tierNameStyle(ScrapPileDefinition definition) {
        return Style.EMPTY.withColor(TextColor.fromRgb(resolveTintColor(definition))).withBold(true);
    }

    public static Style tierLabelStyle(ScrapPileDefinition definition) {
        return Style.EMPTY.withColor(TextColor.fromRgb(resolveTintColor(definition)));
    }

    private static int resolveTintColor(ScrapPileDefinition definition) {
        return PileConfigLoader.getPile(definition.id())
                .map(PileConfigLoader.LoadedPileConfig::tintColor)
                .orElse(definition.tintColor());
    }
}

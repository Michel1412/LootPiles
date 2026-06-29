package com.nerdsquadrados.lootpiles.client;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = LootPiles.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ScrapPileClientPack {
    private static final ResourceLocation PACK_ID = ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, "dynamic_scrap_piles");

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }

        PackLocationInfo locationInfo = new PackLocationInfo(
                PACK_ID.toString(),
                Component.literal("Loot Piles Dynamic Assets"),
                PackSource.BUILT_IN,
                Optional.empty()
        );

        Pack.ResourcesSupplier resourcesSupplier = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo location) {
                return new ScrapPileDynamicResources(location);
            }

            @Override
            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return openPrimary(location);
            }
        };

        event.addRepositorySource(consumer -> consumer.accept(Pack.readMetaAndCreate(
                locationInfo,
                resourcesSupplier,
                PackType.CLIENT_RESOURCES,
                new PackSelectionConfig(false, Pack.Position.TOP, false)
        )));
    }

    private static class ScrapPileDynamicResources implements PackResources {
        private static final byte[] PACK_META = """
                {
                  "pack": {
                    "pack_format": 34,
                    "description": "Loot Piles dynamic scrap pile assets"
                  }
                }
                """.getBytes(StandardCharsets.UTF_8);

        private final PackLocationInfo locationInfo;
        private final Map<ResourceLocation, byte[]> resources;

        ScrapPileDynamicResources(PackLocationInfo locationInfo) {
            this.locationInfo = locationInfo;
            this.resources = buildResources();
        }

        private Map<ResourceLocation, byte[]> buildResources() {
            java.util.HashMap<ResourceLocation, byte[]> map = new java.util.HashMap<>();

            String blockstateTemplate = """
                    {
                      "multipart": [
                        { "when": { "depleted": "false" }, "apply": { "model": "lootpiles:block/scrap_pile" } },
                        { "when": { "depleted": "true" }, "apply": { "model": "lootpiles:block/scrap_pile_empty" } }
                      ]
                    }
                    """;

            String blockItemTemplate = """
                    {
                      "parent": "%s"
                    }
                    """;

            String scrapItemTemplate = """
                    {
                      "parent": "minecraft:item/generated",
                      "textures": {
                        "layer0": "lootpiles:item/scrap_grayscale"
                      }
                    }
                    """;

            for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
                String blockName = registered.definition().blockName();
                ResourceLocation blockstateId = ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, "blockstates/" + blockName + ".json");
                ResourceLocation blockItemModelId = ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, "models/item/" + blockName + ".json");
                ResourceLocation scrapItemModelId = ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, "models/item/" + registered.definition().scrapName() + ".json");
                ResourceLocation blockModelParent = ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, "block/" + blockName);

                map.put(blockstateId, blockstateTemplate.getBytes(StandardCharsets.UTF_8));
                map.put(blockItemModelId, blockItemTemplate.formatted(blockModelParent).getBytes(StandardCharsets.UTF_8));
                map.put(scrapItemModelId, scrapItemTemplate.getBytes(StandardCharsets.UTF_8));
                map.put(
                        ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, "models/block/" + blockName + ".json"),
                        ("{\"parent\":\"lootpiles:block/scrap_pile\"}").getBytes(StandardCharsets.UTF_8)
                );
            }

            return Map.copyOf(map);
        }

        @Override
        public IoSupplier<InputStream> getRootResource(String... elements) {
            if (elements.length == 1 && "pack.mcmeta".equals(elements[0])) {
                return () -> new ByteArrayInputStream(PACK_META);
            }
            return null;
        }

        @Override
        public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
            byte[] data = resources.get(location);
            if (data == null) {
                return null;
            }
            return () -> new ByteArrayInputStream(data);
        }

        @Override
        public void listResources(PackType packType, String namespace, String path, ResourceOutput output) {
            String prefix = namespace + ":" + path + "/";
            for (Map.Entry<ResourceLocation, byte[]> entry : resources.entrySet()) {
                ResourceLocation id = entry.getKey();
                if (id.getNamespace().equals(namespace) && id.getPath().startsWith(path + "/")) {
                    output.accept(id, () -> new ByteArrayInputStream(entry.getValue()));
                }
            }
        }

        @Override
        public Set<String> getNamespaces(PackType packType) {
            return Set.of(LootPiles.MOD_ID);
        }

        @Override
        public void close() {
        }

        @Override
        public PackLocationInfo location() {
            return locationInfo;
        }

        @Override
        public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
            return null;
        }
    }
}

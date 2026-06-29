package com.nerdsquadrados.lootpiles.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.loot.IntRange;
import com.nerdsquadrados.lootpiles.loot.PileLootConfig;
import com.nerdsquadrados.lootpiles.loot.PileLootEntry;
import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinition;
import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinitionLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class PileConfigLoader {
    public static final String PILES_DIR = "lootpiles/piles";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<String> DEFAULT_PILE_ORDER = List.of("medic", "soldier", "miner", "common", "rare", "legendary");

    private static volatile Map<String, LoadedPileConfig> loadedPiles = Map.of();

    private PileConfigLoader() {
    }

    public record LoadedPileConfig(
            String id,
            String displayType,
            String hex,
            int tintColor,
            PileLootConfig lootConfig,
            Path configPath
    ) {
    }

    public static Path getPilesDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve(PILES_DIR);
    }

    public static void ensureDefaultPileFiles() {
        Path pilesDir = getPilesDirectory();
        try {
            Files.createDirectories(pilesDir);
            if (isDirectoryEmpty(pilesDir)) {
                writeDefaultPileFiles(pilesDir);
                LootPiles.LOGGER.info("Created default pile configs in {}", pilesDir.toAbsolutePath());
            }
        } catch (IOException exception) {
            LootPiles.LOGGER.error("Failed to create default pile configs in {}", pilesDir, exception);
        }
    }

    public static void reload() {
        Path pilesDir = getPilesDirectory();
        Map<String, LoadedPileConfig> parsed = new LinkedHashMap<>();

        if (!Files.isDirectory(pilesDir)) {
            loadedPiles = Map.copyOf(parsed);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pilesDir, "*.json")) {
            for (Path path : stream) {
                try {
                    LoadedPileConfig pile = parsePileFile(path);
                    if (parsed.containsKey(pile.id())) {
                        LootPiles.LOGGER.warn("Duplicate pile id '{}' from {}, skipping", pile.id(), path.getFileName());
                        continue;
                    }
                    parsed.put(pile.id(), pile);
                } catch (Exception exception) {
                    LootPiles.LOGGER.error("Failed to parse pile config {}", path.getFileName(), exception);
                }
            }
        } catch (IOException exception) {
            LootPiles.LOGGER.error("Failed to scan pile configs in {}", pilesDir, exception);
        }

        if (parsed.isEmpty()) {
            parsed = Map.copyOf(buildInMemoryDefaults());
        }

        loadedPiles = Map.copyOf(parsed);
        LootPiles.LOGGER.info("Loaded {} pile type(s) from {}", loadedPiles.size(), pilesDir);
    }

    public static List<String> getPileIds() {
        List<String> ids = new ArrayList<>();
        for (String id : DEFAULT_PILE_ORDER) {
            if (loadedPiles.containsKey(id)) {
                ids.add(id);
            }
        }
        for (String id : loadedPiles.keySet()) {
            if (!ids.contains(id)) {
                ids.add(id);
            }
        }
        return Collections.unmodifiableList(ids);
    }

    public static Optional<LoadedPileConfig> getPile(String pileId) {
        if (pileId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(loadedPiles.get(pileId.toLowerCase(Locale.ROOT)));
    }

    public static List<ScrapPileDefinition> allDefinitions() {
        return getPileIds().stream().map(PileConfigLoader::toDefinition).toList();
    }

    public static ScrapPileDefinition toDefinition(String pileId) {
        LoadedPileConfig pile = getPile(pileId).orElseThrow(() -> new IllegalStateException("Unknown pile: " + pileId));
        ResourceLocation blockId = new ResourceLocation(LootPiles.MOD_ID, pile.id() + "_scrap_pile");
        ResourceLocation scrapItemId = new ResourceLocation(LootPiles.MOD_ID, pile.id() + "_scrap");

        return new ScrapPileDefinition(
                pile.id(),
                pile.displayType(),
                pile.tintColor(),
                pile.hex(),
                pile.lootConfig(),
                blockId,
                scrapItemId,
                pile.configPath(),
                pile.lootConfig().useDefaultScrap()
        );
    }

    public static ScrapPileDefinition loadDefinition(String pileId) {
        reload();
        return toDefinition(pileId);
    }

    private static LoadedPileConfig parsePileFile(Path path) throws IOException {
        String id = stripExtension(path.getFileName().toString()).toLowerCase(Locale.ROOT);

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new IllegalStateException("Pile config root must be a JSON object");
            }
            return parsePileObject(id, root.getAsJsonObject(), path);
        }
    }

    private static LoadedPileConfig parsePileObject(String id, JsonObject object, Path path) {
        String displayType = object.has("type") ? object.get("type").getAsString() : capitalize(id);
        String hex = object.has("hex") ? object.get("hex").getAsString() : "#FFFFFF";
        int cooldown = object.has("cooldown") ? Math.max(1, object.get("cooldown").getAsInt()) : 600;
        IntRange scrapDrop = parseRange(object.getAsJsonObject("scrapDrop"), 1, 1);
        IntRange rolls = parseRange(object.getAsJsonObject("rolls"), 1, 1);
        boolean useDefaultScrap = !object.has("use_default_scrap") || object.get("use_default_scrap").getAsBoolean();
        List<PileLootEntry> entries = parseEntries(object.has("entries") ? object.getAsJsonArray("entries") : new JsonArray());

        PileLootConfig lootConfig = new PileLootConfig(cooldown, scrapDrop, rolls, List.copyOf(entries), useDefaultScrap);
        return new LoadedPileConfig(id, displayType, hex, ScrapPileDefinitionLoader.parseHexColor(hex), lootConfig, path);
    }

    private static IntRange parseRange(JsonObject object, int defaultMin, int defaultMax) {
        if (object == null) {
            return new IntRange(defaultMin, defaultMax);
        }
        int min = object.has("min") ? object.get("min").getAsInt() : defaultMin;
        int max = object.has("max") ? object.get("max").getAsInt() : min;
        return new IntRange(min, max);
    }

    private static List<PileLootEntry> parseEntries(JsonArray array) {
        List<PileLootEntry> entries = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entryObject = element.getAsJsonObject();
            ResourceLocation item = ResourceLocation.tryParse(entryObject.get("item").getAsString());
            if (item == null) {
                continue;
            }
            int weight = entryObject.has("weight") ? entryObject.get("weight").getAsInt() : 1;
            int min = entryObject.has("min") ? entryObject.get("min").getAsInt() : 1;
            int max = entryObject.has("max") ? entryObject.get("max").getAsInt() : min;
            entries.add(new PileLootEntry(item, weight, min, max));
        }
        return entries;
    }

    private static boolean isDirectoryEmpty(Path dir) throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.findAny().isEmpty();
        }
    }

    private static void writeDefaultPileFiles(Path pilesDir) throws IOException {
        writePileFile(pilesDir.resolve("medic.json"), buildMedicDefault());
        writePileFile(pilesDir.resolve("soldier.json"), buildSoldierDefault());
        writePileFile(pilesDir.resolve("miner.json"), buildMinerDefault());
        writePileFile(pilesDir.resolve("common.json"), buildCommonDefault());
        writePileFile(pilesDir.resolve("rare.json"), buildRareDefault());
        writePileFile(pilesDir.resolve("legendary.json"), buildLegendaryDefault());
    }

    private static JsonObject buildMedicDefault() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "Medic");
        root.addProperty("hex", "#FF1D1B");
        root.addProperty("cooldown", 600);
        root.add("scrapDrop", range(1, 3));
        root.add("rolls", range(2, 4));
        root.addProperty("use_default_scrap", true);
        JsonArray entries = new JsonArray();
        entries.add(entry("minecraft:potion", 40, 1, 1));
        entries.add(entry("minecraft:glistering_melon_slice", 30, 2, 4));
        entries.add(entry("minecraft:ghast_tear", 10, 1, 2));
        entries.add(entry("minecraft:golden_apple", 5, 1, 1));
        root.add("entries", entries);
        return root;
    }

    private static JsonObject buildSoldierDefault() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "Soldier");
        root.addProperty("hex", "#4B5320");
        root.addProperty("cooldown", 900);
        root.add("scrapDrop", range(2, 4));
        root.add("rolls", range(3, 5));
        root.addProperty("use_default_scrap", false);
        JsonArray entries = new JsonArray();
        entries.add(entry("minecraft:iron_ingot", 50, 2, 6));
        entries.add(entry("minecraft:arrow", 60, 5, 15));
        entries.add(entry("minecraft:gunpowder", 35, 2, 4));
        entries.add(entry("minecraft:tnt", 8, 1, 2));
        root.add("entries", entries);
        return root;
    }

    private static JsonObject buildMinerDefault() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "Miner");
        root.addProperty("hex", "#FFD700");
        root.addProperty("cooldown", 450);
        root.add("scrapDrop", range(1, 2));
        root.add("rolls", range(1, 3));
        root.addProperty("use_default_scrap", true);
        JsonArray entries = new JsonArray();
        entries.add(entry("minecraft:raw_iron", 50, 2, 5));
        entries.add(entry("minecraft:raw_gold", 30, 1, 3));
        entries.add(entry("minecraft:diamond", 5, 1, 1));
        entries.add(entry("minecraft:coal", 70, 4, 10));
        root.add("entries", entries);
        return root;
    }

    private static JsonObject buildCommonDefault() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "Common");
        root.addProperty("hex", "#FFFFFF");
        root.addProperty("cooldown", 300);
        root.add("scrapDrop", range(1, 2));
        root.add("rolls", range(1, 2));
        root.addProperty("use_default_scrap", true);
        JsonArray entries = new JsonArray();
        entries.add(entry("minecraft:stick", 80, 2, 5));
        entries.add(entry("minecraft:string", 50, 1, 3));
        entries.add(entry("minecraft:rotten_flesh", 60, 1, 4));
        root.add("entries", entries);
        return root;
    }

    private static JsonObject buildRareDefault() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "Rare");
        root.addProperty("hex", "#0070DD");
        root.addProperty("cooldown", 750);
        root.add("scrapDrop", range(2, 4));
        root.add("rolls", range(2, 4));
        root.addProperty("use_default_scrap", true);
        JsonArray entries = new JsonArray();
        entries.add(entry("minecraft:diamond", 12, 1, 2));
        entries.add(entry("minecraft:emerald", 25, 2, 5));
        entries.add(entry("minecraft:golden_apple", 15, 1, 2));
        entries.add(entry("minecraft:ender_pearl", 20, 1, 3));
        root.add("entries", entries);
        return root;
    }

    private static JsonObject buildLegendaryDefault() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "Legendary");
        root.addProperty("hex", "#FF8000");
        root.addProperty("cooldown", 1200);
        root.add("scrapDrop", range(3, 6));
        root.add("rolls", range(4, 6));
        root.addProperty("use_default_scrap", false);
        JsonArray entries = new JsonArray();
        entries.add(entry("minecraft:netherite_scrap", 18, 1, 2));
        entries.add(entry("minecraft:netherite_ingot", 8, 1, 1));
        entries.add(entry("minecraft:enchanted_book", 22, 1, 1));
        entries.add(entry("minecraft:totem_of_undying", 4, 1, 1));
        root.add("entries", entries);
        return root;
    }

    private static JsonObject entry(String item, int weight, int min, int max) {
        JsonObject object = new JsonObject();
        object.addProperty("item", item);
        object.addProperty("weight", weight);
        object.addProperty("min", min);
        object.addProperty("max", max);
        return object;
    }

    private static JsonObject range(int min, int max) {
        JsonObject object = new JsonObject();
        object.addProperty("min", min);
        object.addProperty("max", max);
        return object;
    }

    private static void writePileFile(Path path, JsonObject content) throws IOException {
        Files.writeString(path, GSON.toJson(content), StandardCharsets.UTF_8);
    }

    private static Map<String, LoadedPileConfig> buildInMemoryDefaults() {
        Map<String, LoadedPileConfig> defaults = new LinkedHashMap<>();
        Path basePath = getPilesDirectory();
        defaults.put("medic", parsePileObject("medic", buildMedicDefault(), basePath.resolve("medic.json")));
        defaults.put("soldier", parsePileObject("soldier", buildSoldierDefault(), basePath.resolve("soldier.json")));
        defaults.put("miner", parsePileObject("miner", buildMinerDefault(), basePath.resolve("miner.json")));
        defaults.put("common", parsePileObject("common", buildCommonDefault(), basePath.resolve("common.json")));
        defaults.put("rare", parsePileObject("rare", buildRareDefault(), basePath.resolve("rare.json")));
        defaults.put("legendary", parsePileObject("legendary", buildLegendaryDefault(), basePath.resolve("legendary.json")));
        return defaults;
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    private static String capitalize(String id) {
        if (id.isEmpty()) {
            return id;
        }
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
}

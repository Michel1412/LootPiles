package com.nerdsquadrados.lootpiles.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.loot.ScrapTierConfig;
import com.nerdsquadrados.lootpiles.loot.ScrapTierLootEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class ScrapPileDefinitionLoader {
    private static final String CONFIG_SUBDIR = "scrap_piles";
    private static final String DEFAULT_RESOURCE_PREFIX = "defaults/lootpiles/scrap_piles/";

    private ScrapPileDefinitionLoader() {
    }

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve(LootPiles.MOD_ID).resolve(CONFIG_SUBDIR);
    }

    public static Path getConfigFile(String id) {
        return getConfigDirectory().resolve(id + ".json");
    }

    public static void ensureDefaultFiles() {
        try {
            Files.createDirectories(getConfigDirectory());
        } catch (IOException exception) {
            LootPiles.LOGGER.error("Failed to create scrap pile config directory", exception);
            return;
        }

        try (InputStream index = ScrapPileDefinitionLoader.class.getClassLoader().getResourceAsStream(DEFAULT_RESOURCE_PREFIX)) {
            if (index == null) {
                copyBundledDefaultsFromClasspath();
                return;
            }
        } catch (IOException ignored) {
        }

        copyBundledDefaultsFromClasspath();
    }

    private static void copyBundledDefaultsFromClasspath() {
        String[] knownDefaults = {"common", "uncommon", "rare", "epic", "legendary", "medic"};
        for (String id : knownDefaults) {
            Path target = getConfigFile(id);
            if (Files.exists(target)) {
                continue;
            }
            copyBundledDefault(id, target);
        }
    }

    public static List<ScrapPileDefinition> discoverDefinitions() {
        ensureDefaultFiles();
        Path directory = getConfigDirectory();

        if (!Files.isDirectory(directory)) {
            return List.of();
        }

        List<ScrapPileDefinition> definitions = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.toString().endsWith(".json"))
                    .filter(path -> !path.getFileName().toString().endsWith(".bak"))
                    .sorted()
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String id = fileName.substring(0, fileName.length() - ".json".length()).toLowerCase(Locale.ROOT);
                        if (!isValidId(id)) {
                            LootPiles.LOGGER.warn("Skipping scrap pile config with invalid id '{}'", id);
                            return;
                        }
                        try {
                            definitions.add(parseDefinition(id, path));
                        } catch (Exception exception) {
                            LootPiles.LOGGER.error("Failed to parse scrap pile config {}", path, exception);
                        }
                    });
        } catch (IOException exception) {
            LootPiles.LOGGER.error("Failed to list scrap pile configs in {}", directory, exception);
        }
        return Collections.unmodifiableList(definitions);
    }

    public static ScrapPileDefinition loadDefinition(String id) {
        Path file = getConfigFile(id);
        if (!Files.isRegularFile(file)) {
            ensureDefaultFiles();
        }
        return parseDefinition(id, file);
    }

    public static ScrapPileDefinition parseDefinition(String id, Path file) {
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("Missing scrap pile config: " + file);
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new IllegalStateException("Scrap pile config must be a JSON object: " + file);
            }

            JsonObject object = root.getAsJsonObject();
            if (!object.has("entries")) {
                throw new IllegalStateException("Scrap pile config missing 'entries': " + file);
            }

            String displayType = object.has("type") ? object.get("type").getAsString() : capitalize(id);
            String hex = object.has("hex") ? object.get("hex").getAsString() : "#909090";
            int tintColor = parseHexColor(hex);
            ScrapTierConfig lootConfig = parseLootConfig(object, file);

            ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, id + "_scrap_pile");
            ResourceLocation scrapItemId = ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, id + "_scrap");

            return new ScrapPileDefinition(id, displayType, tintColor, hex, lootConfig, blockId, scrapItemId, file);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read scrap pile config " + file, exception);
        }
    }

    public static int parseHexColor(String hex) {
        String normalized = hex.startsWith("#") ? hex : "#" + hex;
        return Color.decode(normalized).getRGB() & 0xFFFFFF;
    }

    private static ScrapTierConfig parseLootConfig(JsonObject object, Path file) {
        int cooldown = object.has("cooldown") ? Math.max(1, object.get("cooldown").getAsInt()) : 300;

        int scrapMin = 1;
        int scrapMax = 1;
        if (object.has("scrapDrop") && object.get("scrapDrop").isJsonObject()) {
            JsonObject scrapDrop = object.getAsJsonObject("scrapDrop");
            scrapMin = scrapDrop.has("min") ? scrapDrop.get("min").getAsInt() : 1;
            scrapMax = scrapDrop.has("max") ? scrapDrop.get("max").getAsInt() : scrapMin;
        }
        scrapMin = Math.max(1, scrapMin);
        scrapMax = Math.max(scrapMin, scrapMax);

        int rollsMin = 0;
        int rollsMax = 0;
        if (object.has("rolls") && object.get("rolls").isJsonObject()) {
            JsonObject rolls = object.getAsJsonObject("rolls");
            rollsMin = rolls.has("min") ? rolls.get("min").getAsInt() : 0;
            rollsMax = rolls.has("max") ? rolls.get("max").getAsInt() : rollsMin;
        }
        rollsMin = Math.max(0, rollsMin);
        rollsMax = Math.max(rollsMin, rollsMax);

        List<ScrapTierLootEntry> entries = parseEntries(object.getAsJsonArray("entries"), file);
        return new ScrapTierConfig(cooldown, scrapMin, scrapMax, rollsMin, rollsMax, entries);
    }

    private static List<ScrapTierLootEntry> parseEntries(JsonArray array, Path file) {
        List<ScrapTierLootEntry> entries = new ArrayList<>();

        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject entryObject = element.getAsJsonObject();
            if (!entryObject.has("item")) {
                continue;
            }

            ResourceLocation itemId = ResourceLocation.tryParse(entryObject.get("item").getAsString());
            if (itemId == null) {
                continue;
            }

            Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.AIR);
            if (item == Items.AIR) {
                LootPiles.LOGGER.warn("Skipping unknown item '{}' in scrap pile config {}", itemId, file);
                continue;
            }

            int weight = Math.max(1, entryObject.has("weight") ? entryObject.get("weight").getAsInt() : 1);
            int min = Math.max(1, entryObject.has("min") ? entryObject.get("min").getAsInt() : 1);
            int max = Math.max(min, entryObject.has("max") ? entryObject.get("max").getAsInt() : min);

            entries.add(new ScrapTierLootEntry(item, weight, min, max));
        }

        return Collections.unmodifiableList(entries);
    }

    private static void copyBundledDefault(String id, Path target) {
        String resourcePath = DEFAULT_RESOURCE_PREFIX + id + ".json";
        try (InputStream input = ScrapPileDefinitionLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                return;
            }
            Files.createDirectories(target.getParent());
            Files.copy(input, target);
            LootPiles.LOGGER.info("Created default scrap pile config: {}", target);
        } catch (IOException exception) {
            LootPiles.LOGGER.error("Failed to write default scrap pile config for id {}", id, exception);
        }
    }

    private static boolean isValidId(String id) {
        return !id.isBlank() && id.matches("[a-z0-9_]+");
    }

    private static String capitalize(String id) {
        if (id.isEmpty()) {
            return id;
        }
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
}

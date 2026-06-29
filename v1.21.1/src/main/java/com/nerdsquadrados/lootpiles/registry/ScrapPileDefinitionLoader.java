package com.nerdsquadrados.lootpiles.registry;

import com.nerdsquadrados.lootpiles.config.PileConfigLoader;

import java.awt.Color;
import java.util.List;

public final class ScrapPileDefinitionLoader {
    private ScrapPileDefinitionLoader() {
    }

    public static void ensureDefaultFiles() {
        PileConfigLoader.ensureDefaultPileFiles();
    }

    public static List<ScrapPileDefinition> discoverDefinitions() {
        PileConfigLoader.reload();
        return PileConfigLoader.allDefinitions();
    }

    public static ScrapPileDefinition loadDefinition(String id) {
        return PileConfigLoader.loadDefinition(id);
    }

    public static int parseHexColor(String hex) {
        String normalized = hex.startsWith("#") ? hex : "#" + hex;
        return Color.decode(normalized).getRGB() & 0xFFFFFF;
    }
}

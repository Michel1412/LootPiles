package com.nerdsquadrados.lootpiles.registry;

import com.nerdsquadrados.lootpiles.loot.ScrapTierConfig;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public record ScrapPileDefinition(
        String id,
        String displayType,
        int tintColor,
        String hex,
        ScrapTierConfig lootConfig,
        ResourceLocation blockId,
        ResourceLocation scrapItemId,
        Path configPath
) {
    public String blockName() {
        return id + "_scrap_pile";
    }

    public String scrapName() {
        return id + "_scrap";
    }

    public long cooldownTicks() {
        return lootConfig.cooldownTicks();
    }

    public int cooldownMinutes() {
        return lootConfig.cooldownMinutes();
    }
}

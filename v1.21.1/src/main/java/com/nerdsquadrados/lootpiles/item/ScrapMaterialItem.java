package com.nerdsquadrados.lootpiles.item;

import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ScrapMaterialItem extends net.minecraft.world.item.Item {
    private final String pileId;

    public ScrapMaterialItem(String pileId, Properties properties) {
        super(properties);
        this.pileId = pileId;
    }

    public String getPileId() {
        return pileId;
    }

    @Override
    public Component getName(ItemStack stack) {
        return ScrapPileRegistry.byId(pileId)
                .map(ScrapPileRegistry.RegisteredScrapPile::definition)
                .map(ScrapPileRegistry::createScrapDisplayName)
                .orElseGet(() -> super.getName(stack));
    }
}

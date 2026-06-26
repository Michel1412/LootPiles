package com.nerdsquadrados.lootpiles.compat.jade;

import com.nerdsquadrados.lootpiles.LootPiles;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class LootPilesJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ScrapPileJadeProvider.INSTANCE, com.nerdsquadrados.lootpiles.block.ScrapPileBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ScrapPileJadeProvider.INSTANCE, com.nerdsquadrados.lootpiles.block.ScrapPileBlock.class);
    }

    public static final String PROVIDER_UID = LootPiles.MOD_ID + ":scrap_pile";
}

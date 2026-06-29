package com.nerdsquadrados.lootpiles.compat.jade;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class ScrapPileJadeProvider implements IBlockComponentProvider {
    public static final ScrapPileJadeProvider INSTANCE = new ScrapPileJadeProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockState().getBlock() instanceof ScrapPileBlock scrapPileBlock)) {
            return;
        }

        ScrapPileRegistry.byId(scrapPileBlock.getPileId()).ifPresent(registered -> {
            var definition = registered.definition();
            tooltip.add(Component.literal(definition.displayType()).withStyle(ScrapPileRegistry.tierLabelStyle(definition)));

            if (accessor.getBlockState().getValue(ScrapPileBlock.DEPLETED)) {
                tooltip.add(Component.translatable("tooltip.lootpiles.cooldown", definition.cooldownMinutes()));
            } else {
                tooltip.add(Component.translatable("tooltip.lootpiles.ready"));
            }
        });
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(LootPilesJadePlugin.PROVIDER_UID);
    }
}

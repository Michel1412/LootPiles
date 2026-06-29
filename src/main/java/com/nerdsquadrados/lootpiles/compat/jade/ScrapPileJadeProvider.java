package com.nerdsquadrados.lootpiles.compat.jade;

import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import com.nerdsquadrados.lootpiles.cooldown.ScrapCooldownManager;
import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinitionLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class ScrapPileJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final ScrapPileJadeProvider INSTANCE = new ScrapPileJadeProvider();
    private static final String KEY_REMAINING = "RemainingTicks";
    private static final String KEY_PILE_ID = "PileId";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getLevel() instanceof ServerLevel serverLevel
                && accessor.getBlockState().getBlock() instanceof ScrapPileBlock scrapPileBlock) {
            long remaining = ScrapCooldownManager.get(serverLevel)
                    .getRemainingTicks(accessor.getPosition(), serverLevel.getGameTime());
            data.putLong(KEY_REMAINING, remaining);
            data.putString(KEY_PILE_ID, scrapPileBlock.getPileId());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockState().getBlock() instanceof ScrapPileBlock scrapPileBlock)) {
            return;
        }

        var definition = ScrapPileDefinitionLoader.loadDefinition(scrapPileBlock.getPileId());
        tooltip.add(Component.literal(definition.displayType()).withStyle(style -> style.withColor(definition.tintColor())));

        long remaining = accessor.getServerData().getLong(KEY_REMAINING);
        if (remaining > 0L) {
            long minutes = Math.max(1, (remaining + 1199) / 1200);
            tooltip.add(Component.translatable("tooltip.lootpiles.cooldown", minutes));
        } else {
            tooltip.add(Component.translatable("tooltip.lootpiles.ready"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.parse(LootPilesJadePlugin.PROVIDER_UID);
    }
}

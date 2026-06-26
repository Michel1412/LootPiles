package com.nerdsquadrados.lootpiles.compat.jade;

import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import com.nerdsquadrados.lootpiles.block.ScrapTier;
import com.nerdsquadrados.lootpiles.cooldown.ScrapCooldownManager;
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

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getLevel() instanceof ServerLevel serverLevel) {
            long remaining = ScrapCooldownManager.get(serverLevel)
                    .getRemainingTicks(accessor.getPosition(), serverLevel.getGameTime());
            data.putLong(KEY_REMAINING, remaining);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        ScrapTier tier = accessor.getBlockState().getValue(ScrapPileBlock.TIER);
        tooltip.add(Component.translatable("tooltip.lootpiles.tier", Component.translatable("tier.lootpiles." + tier.getSerializedName())));

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

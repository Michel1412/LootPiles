package com.nerdsquadrados.lootpiles.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ScrapPileBlockItem extends BlockItem {
    private final ScrapTier tier;

    public ScrapPileBlockItem(Block block, ScrapTier tier, Item.Properties properties) {
        super(block, properties);
        this.tier = tier;
    }

    public ScrapTier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(
                Component.translatable("tooltip.lootpiles.rarity.label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("tier.lootpiles." + tier.getSerializedName())
                                .withStyle(tier.getChatFormatting()))
        );
        tooltipComponents.add(
                Component.translatable("tooltip.lootpiles.cooldown_minutes", tier.getCooldownMinutes())
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, net.minecraft.world.entity.player.Player player, ItemStack stack, BlockState state) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(ScrapPileBlock.TIER, tier).setValue(ScrapPileBlock.DEPLETED, false), Block.UPDATE_ALL);
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    @Override
    public BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            return state.setValue(ScrapPileBlock.TIER, tier).setValue(ScrapPileBlock.DEPLETED, false);
        }
        return null;
    }
}

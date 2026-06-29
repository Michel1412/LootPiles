package com.nerdsquadrados.lootpiles.block;

import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinition;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
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

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScrapPileBlockItem extends BlockItem {
    private final String pileId;

    public ScrapPileBlockItem(Block block, String pileId, Item.Properties properties) {
        super(block, properties);
        this.pileId = pileId;
    }

    public String getPileId() {
        return pileId;
    }

    @Override
    public Component getName(ItemStack stack) {
        return ScrapPileRegistry.byId(pileId)
                .map(ScrapPileRegistry.RegisteredScrapPile::definition)
                .map(ScrapPileRegistry::createDisplayName)
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        ScrapPileRegistry.byId(pileId).ifPresent(registered -> {
            ScrapPileDefinition definition = registered.definition();
            tooltipComponents.add(
                    Component.literal(definition.displayType())
                            .withStyle(ScrapPileRegistry.tierLabelStyle(definition))
            );
            tooltipComponents.add(
                    Component.translatable("tooltip.lootpiles.cooldown_minutes", definition.cooldownMinutes())
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        });
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, net.minecraft.world.entity.player.Player player, ItemStack stack, BlockState state) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(ScrapPileBlock.DEPLETED, false), Block.UPDATE_ALL);
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    @Override
    public BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            return state.setValue(ScrapPileBlock.DEPLETED, false);
        }
        return null;
    }
}

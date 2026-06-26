package com.nerdsquadrados.lootpiles.block;

import com.nerdsquadrados.lootpiles.config.ScrapPileConfig;
import com.nerdsquadrados.lootpiles.cooldown.ScrapCooldownManager;
import com.nerdsquadrados.lootpiles.loot.ScrapLootService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class ScrapPileBlock extends Block {
    public static final EnumProperty<ScrapTier> TIER = EnumProperty.create("tier", ScrapTier.class);
    public static final BooleanProperty DEPLETED = BooleanProperty.create("depleted");

    protected static final VoxelShape SLAB_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public ScrapPileBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(TIER, ScrapTier.COMMON)
                .setValue(DEPLETED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER, DEPLETED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SLAB_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SLAB_SHAPE;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.canUseGameMasterBlocks()) {
            return super.getDestroyProgress(state, player, level, pos);
        }
        return 0.0F;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ScrapCooldownManager manager = ScrapCooldownManager.get(serverLevel);
        long gameTime = serverLevel.getGameTime();

        if (!manager.isPlayerAllowedToLoot(pos, gameTime)) {
            long remainingTicks = manager.getRemainingTicks(pos, gameTime);
            long minutes = Math.max(1, (remainingTicks + 1199) / 1200);
            player.displayClientMessage(
                    Component.translatable("message.lootpiles.scrap_pile.on_cooldown", minutes),
                    true
            );
            return InteractionResult.CONSUME;
        }

        ScrapTier tier = state.getValue(TIER);
        ScrapLootService.spawnLoot(serverLevel, pos, tier, player);

        level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0F, 0.9F);

        long readyTime = gameTime + ScrapPileConfig.getCooldownTicks(tier);
        manager.addCooldown(pos, readyTime);
        level.setBlock(pos, state.setValue(DEPLETED, true), Block.UPDATE_ALL);

        return InteractionResult.CONSUME;
    }

    public static void clearDepletedState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ScrapPileBlock && state.getValue(DEPLETED)) {
            level.setBlock(pos, state.setValue(DEPLETED, false), Block.UPDATE_ALL);
        }
    }
}

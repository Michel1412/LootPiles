package com.nerdsquadrados.lootpiles.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import com.nerdsquadrados.lootpiles.cooldown.ScrapCooldownManager;
import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinition;
import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinitionLoader;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ScrapPileCommands {
    private ScrapPileCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerRoot(dispatcher, "scrappile");
        registerRoot(dispatcher, "lootpiles");
    }

    private static void registerRoot(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name)
                .requires(source -> source.hasPermission(2))
                .then(buildSpawnNode())
                .then(buildResetNode())
                .then(buildStatusNode()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSpawnNode() {
        return Commands.literal("spawn")
                .then(Commands.argument("pile", ScrapPileIdArgument.pileId())
                        .suggests(ScrapPileIdArgument::suggestPileIds)
                        .executes(ctx -> spawnAt(
                                ctx.getSource(),
                                ScrapPileIdArgument.getPileId(ctx, "pile"),
                                ctx.getSource().getPlayerOrException().blockPosition()))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> spawnAt(
                                        ctx.getSource(),
                                        ScrapPileIdArgument.getPileId(ctx, "pile"),
                                        BlockPosArgument.getLoadedBlockPos(ctx, "pos")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildResetNode() {
        return Commands.literal("reset")
                .then(Commands.literal("all")
                        .executes(ctx -> resetAll(ctx.getSource())))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(ctx -> resetOne(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildStatusNode() {
        return Commands.literal("status")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(ctx -> status(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"))));
    }

    private static int spawnAt(CommandSourceStack source, String pileId, BlockPos pos) {
        ServerLevel level = source.getLevel();
        ScrapPileRegistry.RegisteredScrapPile registered = ScrapPileRegistry.byId(pileId).orElseThrow();
        BlockState state = registered.block().get().defaultBlockState().setValue(ScrapPileBlock.DEPLETED, false);

        if (!level.setBlock(pos, state, Block.UPDATE_ALL)) {
            source.sendFailure(Component.translatable("commands.lootpiles.spawn.failed", pos.getX(), pos.getY(), pos.getZ()));
            return 0;
        }

        ScrapCooldownManager.get(level).removeCooldown(pos);
        ScrapPileDefinition definition = ScrapPileDefinitionLoader.loadDefinition(pileId);
        source.sendSuccess(() -> Component.translatable(
                "commands.lootpiles.spawn.success",
                definition.displayType(),
                pos.getX(), pos.getY(), pos.getZ()
        ), true);
        return 1;
    }

    private static int resetAll(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        ScrapCooldownManager manager = ScrapCooldownManager.get(level);
        var positions = manager.getCooldownPositions();
        int count = positions.size();
        manager.clearAll();

        for (BlockPos pos : positions) {
            ScrapPileBlock.clearDepletedState(level, pos);
        }

        source.sendSuccess(() -> Component.translatable("commands.lootpiles.reset.all", count), true);
        return Math.max(1, count);
    }

    private static int resetOne(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = source.getLevel();
        ScrapCooldownManager manager = ScrapCooldownManager.get(level);
        manager.removeCooldown(pos);
        ScrapPileBlock.clearDepletedState(level, pos);
        source.sendSuccess(() -> Component.translatable("commands.lootpiles.reset.one", pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    private static int status(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = source.getLevel();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof ScrapPileBlock scrapPileBlock)) {
            source.sendFailure(Component.translatable("commands.lootpiles.status.not_scrap_pile", pos.getX(), pos.getY(), pos.getZ()));
            return 0;
        }

        ScrapPileDefinition definition = ScrapPileDefinitionLoader.loadDefinition(scrapPileBlock.getPileId());
        ScrapCooldownManager manager = ScrapCooldownManager.get(level);
        long remaining = manager.getRemainingTicks(pos, level.getGameTime());
        boolean depleted = state.getValue(ScrapPileBlock.DEPLETED);

        if (remaining <= 0L) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.lootpiles.status.ready",
                    definition.displayType(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    depleted
            ), false);
        } else {
            long minutes = Math.max(1, (remaining + 1199) / 1200);
            source.sendSuccess(() -> Component.translatable(
                    "commands.lootpiles.status.cooldown",
                    definition.displayType(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    minutes
            ), false);
        }
        return 1;
    }
}

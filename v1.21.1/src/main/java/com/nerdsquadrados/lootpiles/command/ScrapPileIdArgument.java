package com.nerdsquadrados.lootpiles.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public final class ScrapPileIdArgument {
    private static final DynamicCommandExceptionType INVALID = new DynamicCommandExceptionType(
            id -> Component.translatable("argument.lootpiles.pile.invalid", id)
    );

    private ScrapPileIdArgument() {
    }

    public static StringArgumentType pileId() {
        return StringArgumentType.string();
    }

    public static String getPileId(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, name).toLowerCase();
        if (ScrapPileRegistry.byId(id).isEmpty()) {
            throw INVALID.create(id);
        }
        return id;
    }

    public static CompletableFuture<Suggestions> suggestPileIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                ScrapPileRegistry.all().stream().map(registered -> registered.definition().id()),
                builder
        );
    }
}

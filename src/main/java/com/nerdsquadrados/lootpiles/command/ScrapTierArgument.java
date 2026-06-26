package com.nerdsquadrados.lootpiles.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.nerdsquadrados.lootpiles.block.ScrapTier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ScrapTierArgument implements ArgumentType<ScrapTier> {
    private static final DynamicCommandExceptionType INVALID_TIER = new DynamicCommandExceptionType(
            value -> Component.translatable("argument.lootpiles.tier.invalid", value)
    );

    public static ScrapTierArgument tier() {
        return new ScrapTierArgument();
    }

    public static ScrapTier getTier(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, ScrapTier.class);
    }

    @Override
    public ScrapTier parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readUnquotedString();
        for (ScrapTier tier : ScrapTier.values()) {
            if (tier.getSerializedName().equalsIgnoreCase(input)) {
                return tier;
            }
        }
        throw INVALID_TIER.create(input);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(ScrapTier.values()).map(ScrapTier::getSerializedName),
                builder
        );
    }
}

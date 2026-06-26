package com.nerdsquadrados.lootpiles.command;

import com.nerdsquadrados.lootpiles.LootPiles;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LootPilesArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, LootPiles.MOD_ID);

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ScrapTierArgument>> SCRAP_TIER =
            ARGUMENT_TYPES.register("scrap_tier", () -> ArgumentTypeInfos.registerByClass(
                    ScrapTierArgument.class,
                    SingletonArgumentInfo.contextFree(ScrapTierArgument::tier)
            ));

    private LootPilesArgumentTypes() {
    }
}

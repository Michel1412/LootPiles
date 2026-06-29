package com.nerdsquadrados.lootpiles.compat.jei;

import com.nerdsquadrados.lootpiles.LootPiles;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class LootPilesJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(LootPiles.MOD_ID, "jei");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (ScrapPileRegistry.RegisteredScrapPile registered : ScrapPileRegistry.all()) {
            registration.addIngredientInfo(
                    new ItemStack(registered.blockItem().get()),
                    mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                    Component.translatable("jei.lootpiles.scrap_pile.info")
            );
        }
    }
}

package com.nerdsquadrados.lootpiles.block;

import com.mojang.serialization.Codec;
import com.nerdsquadrados.lootpiles.config.ScrapPileConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;

public enum ScrapTier implements StringRepresentable {
    COMMON("common"),
    UNCOMMON("uncommon"),
    RARE("rare"),
    EPIC("epic"),
    LEGENDARY("legendary");

    public static final Codec<ScrapTier> CODEC = StringRepresentable.fromEnum(ScrapTier::values);

    private final String name;

    ScrapTier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public ChatFormatting getChatFormatting() {
        return switch (this) {
            case COMMON -> ChatFormatting.GRAY;
            case UNCOMMON -> ChatFormatting.GREEN;
            case RARE -> ChatFormatting.BLUE;
            case EPIC -> ChatFormatting.LIGHT_PURPLE;
            case LEGENDARY -> ChatFormatting.GOLD;
        };
    }

    public Style getDisplayNameStyle() {
        return switch (this) {
            case COMMON -> Style.EMPTY.withColor(0x909090);
            case UNCOMMON -> Style.EMPTY.withColor(0x1EFF00);
            case RARE -> Style.EMPTY.withColor(0x0070DD).withBold(true);
            case EPIC -> Style.EMPTY.withColor(0xA335EE).withBold(true);
            case LEGENDARY -> Style.EMPTY.withColor(0xFF8000).withBold(true).withUnderlined(true);
        };
    }

    public Component getDisplayNameComponent() {
        return Component.translatable("item.lootpiles.scrap_pile_" + name).withStyle(getDisplayNameStyle());
    }

    public int getCooldownMinutes() {
        return Math.max(1, ScrapPileConfig.getCooldownTicks(this) / 1200);
    }
}

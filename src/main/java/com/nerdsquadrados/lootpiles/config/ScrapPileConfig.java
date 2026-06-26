package com.nerdsquadrados.lootpiles.config;

import com.nerdsquadrados.lootpiles.block.ScrapTier;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ScrapPileConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue COMMON_COOLDOWN;
    private static final ModConfigSpec.IntValue UNCOMMON_COOLDOWN;
    private static final ModConfigSpec.IntValue RARE_COOLDOWN;
    private static final ModConfigSpec.IntValue EPIC_COOLDOWN;
    private static final ModConfigSpec.IntValue LEGENDARY_COOLDOWN;

    static {
        BUILDER.comment("Cooldown durations in ticks (20 ticks = 1 second).").push("cooldown");

        COMMON_COOLDOWN = BUILDER
                .comment("COMMON tier reset time (default: 12000 = 10 minutes)")
                .defineInRange("common", 12000, 1, Integer.MAX_VALUE);
        UNCOMMON_COOLDOWN = BUILDER
                .comment("UNCOMMON tier reset time (default: 24000 = 20 minutes)")
                .defineInRange("uncommon", 24000, 1, Integer.MAX_VALUE);
        RARE_COOLDOWN = BUILDER
                .comment("RARE tier reset time (default: 36000 = 30 minutes)")
                .defineInRange("rare", 36000, 1, Integer.MAX_VALUE);
        EPIC_COOLDOWN = BUILDER
                .comment("EPIC tier reset time (default: 60000 = 50 minutes)")
                .defineInRange("epic", 60000, 1, Integer.MAX_VALUE);
        LEGENDARY_COOLDOWN = BUILDER
                .comment("LEGENDARY tier reset time (default: 120000 = 100 minutes)")
                .defineInRange("legendary", 120000, 1, Integer.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static int getCooldownTicks(ScrapTier tier) {
        return switch (tier) {
            case COMMON -> COMMON_COOLDOWN.get();
            case UNCOMMON -> UNCOMMON_COOLDOWN.get();
            case RARE -> RARE_COOLDOWN.get();
            case EPIC -> EPIC_COOLDOWN.get();
            case LEGENDARY -> LEGENDARY_COOLDOWN.get();
        };
    }
}

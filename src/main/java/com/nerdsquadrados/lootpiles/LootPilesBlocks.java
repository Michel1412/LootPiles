package com.nerdsquadrados.lootpiles;

import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LootPilesBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LootPiles.MOD_ID);

    public static final DeferredBlock<ScrapPileBlock> SCRAP_PILE = BLOCKS.register(
            "scrap_pile",
            () -> new ScrapPileBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );
}

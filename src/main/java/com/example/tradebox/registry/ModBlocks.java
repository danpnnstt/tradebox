package com.example.tradebox.registry;

import com.example.tradebox.TradeBoxMod;
import com.example.tradebox.block.PotionTradeBlock;
import com.example.tradebox.block.TradeBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TradeBoxMod.MOD_ID);

    public static final DeferredBlock<TradeBoxBlock> TRADE_BOX = BLOCKS.register(
            "trade_box",
            () -> new TradeBoxBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.5f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            )
    );

    public static final DeferredBlock<PotionTradeBlock> POTION_TRADE_BLOCK = BLOCKS.register(
            "potion_trade_block",
            () -> new PotionTradeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );
}

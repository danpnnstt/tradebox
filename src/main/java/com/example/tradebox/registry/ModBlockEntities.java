package com.example.tradebox.registry;

import com.example.tradebox.TradeBoxMod;
import com.example.tradebox.block.PotionTradeBlockEntity;
import com.example.tradebox.block.TradeBoxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TradeBoxMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TradeBoxBlockEntity>> TRADE_BOX =
            BLOCK_ENTITIES.register("trade_box", () ->
                    BlockEntityType.Builder.of(TradeBoxBlockEntity::new, ModBlocks.TRADE_BOX.get())
                            .build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PotionTradeBlockEntity>> POTION_TRADE =
            BLOCK_ENTITIES.register("potion_trade_block", () ->
                    BlockEntityType.Builder.of(PotionTradeBlockEntity::new, ModBlocks.POTION_TRADE_BLOCK.get())
                            .build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.example.tradebox.block.SellerBlockEntity>> SELLER_BLOCK =
            BLOCK_ENTITIES.register("seller_block", () ->
                    BlockEntityType.Builder.of(com.example.tradebox.block.SellerBlockEntity::new, ModBlocks.SELLER_BLOCK.get())
                            .build(null)
            );
}

package com.example.tradebox.registry;

import com.example.tradebox.TradeBoxMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TradeBoxMod.MOD_ID);

    public static final DeferredItem<BlockItem> TRADE_BOX = ITEMS.register(
            "trade_box",
            () -> new BlockItem(ModBlocks.TRADE_BOX.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> POTION_TRADE_BLOCK = ITEMS.register(
            "potion_trade_block",
            () -> new BlockItem(ModBlocks.POTION_TRADE_BLOCK.get(), new Item.Properties())
    );
}

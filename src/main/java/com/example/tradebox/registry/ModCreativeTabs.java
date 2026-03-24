package com.example.tradebox.registry;

import com.example.tradebox.TradeBoxMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TradeBoxMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRADE_BOX_TAB =
            CREATIVE_TABS.register("trade_box", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tradebox"))
                    .icon(() -> new ItemStack(ModItems.TRADE_BOX.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.TRADE_BOX.get());
                        output.accept(ModItems.POTION_TRADE_BLOCK.get());
                        output.accept(ModItems.SELLER_BLOCK.get());
                    })
                    .build());
}

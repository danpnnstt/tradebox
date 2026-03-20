package com.example.tradebox.registry;

import com.example.tradebox.TradeBoxMod;
import com.example.tradebox.menu.TradeBoxMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, TradeBoxMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TradeBoxMenu>> TRADE_BOX_MENU =
            MENUS.register("trade_box", () ->
                    IMenuTypeExtension.create(TradeBoxMenu::new)
            );
}

package com.example.tradebox.client;

import com.example.tradebox.TradeBoxMod;
import com.example.tradebox.client.screen.TradeBoxScreen;
import com.example.tradebox.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TradeBoxMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TradeBoxClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.TRADE_BOX_MENU.get(), TradeBoxScreen::new);
    }
}

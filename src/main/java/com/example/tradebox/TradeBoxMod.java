package com.example.tradebox;

import com.example.tradebox.command.TradeBoxCommands;
import com.example.tradebox.config.EnchantmentShopConfig;
import com.example.tradebox.config.PotionShopConfig;
import com.example.tradebox.config.TradeBoxConfig;
import com.example.tradebox.network.BuyEnchantmentPacket;
import com.example.tradebox.network.BuyPotionPacket;
import com.example.tradebox.network.SellEnchantmentPacket;
import com.example.tradebox.network.SellPotionPacket;
import com.example.tradebox.registry.ModBlockEntities;
import com.example.tradebox.registry.ModBlocks;
import com.example.tradebox.registry.ModCreativeTabs;
import com.example.tradebox.registry.ModItems;
import com.example.tradebox.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(TradeBoxMod.MOD_ID)
public class TradeBoxMod {

    public static final String MOD_ID = "tradebox";

    public TradeBoxMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(EnchantmentShopConfig::load);
        event.enqueueWork(PotionShopConfig::load);
        event.enqueueWork(TradeBoxConfig::load);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        TradeBoxCommands.register(event.getDispatcher());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(BuyEnchantmentPacket.TYPE,  BuyEnchantmentPacket.STREAM_CODEC,  BuyEnchantmentPacket::handle)
                .playToServer(BuyPotionPacket.TYPE,       BuyPotionPacket.STREAM_CODEC,       BuyPotionPacket::handle)
                .playToServer(SellEnchantmentPacket.TYPE, SellEnchantmentPacket.STREAM_CODEC, SellEnchantmentPacket::handle)
                .playToServer(SellPotionPacket.TYPE,      SellPotionPacket.STREAM_CODEC,      SellPotionPacket::handle);
    }
}

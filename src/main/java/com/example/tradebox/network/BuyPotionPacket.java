package com.example.tradebox.network;

import com.example.tradebox.TradeBoxMod;
import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.GlassBottleEntry;
import com.example.tradebox.config.PotionEntry;
import com.example.tradebox.config.PotionShopConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Sent by the client to the server when the player purchases a potion or glass bottles.
 *
 * For potions: potionId = registry ID, type = "regular" | "splash" | "lingering"
 * For glass bottles: potionId = "", type = "bottle"
 */
public record BuyPotionPacket(String potionId, String potionType) implements CustomPacketPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeBoxMod.MOD_ID);

    public static final Type<BuyPotionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TradeBoxMod.MOD_ID, "buy_potion"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyPotionPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> { buf.writeUtf(pkt.potionId()); buf.writeUtf(pkt.potionType()); },
                    buf -> new BuyPotionPacket(buf.readUtf(), buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BuyPotionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (packet.potionType().equals("bottle")) {
                processBottlePurchase(player);
            } else {
                processPotionPurchase(player, packet.potionId(), packet.potionType());
            }
        });
    }

    // -------------------------------------------------------------------------

    private static void processBottlePurchase(Player player) {
        GlassBottleEntry entry = PotionShopConfig.getInstance().getGlassBottles();
        if (!canAfford(player, entry.cost())) return;

        removeCost(player, entry.cost());

        ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, entry.quantity());
        if (!player.addItem(bottles)) player.drop(bottles, false);
    }

    private static void processPotionPurchase(Player player, String potionId, String potionType) {
        List<PotionEntry> entries = PotionShopConfig.getInstance().getPotions();
        PotionEntry entry = null;
        for (PotionEntry e : entries) {
            if (e.potion_id().equals(potionId) && e.type().equals(potionType)) {
                entry = e;
                break;
            }
        }

        if (entry == null) {
            LOGGER.warn("[TradeBox] {} tried to buy unknown potion: {} ({})",
                    player.getName().getString(), potionId, potionType);
            return;
        }

        if (!canAfford(player, entry.cost())) return;

        removeCost(player, entry.cost());

        ItemStack potion = createPotionStack(player, potionId, potionType);
        if (potion != null) {
            if (!player.addItem(potion)) player.drop(potion, false);
        }
    }

    private static ItemStack createPotionStack(Player player, String potionId, String potionType) {
        ResourceLocation rl = ResourceLocation.tryParse(potionId);
        if (rl == null) return null;

        var potionRegistry = player.level().registryAccess().registryOrThrow(Registries.POTION);
        Optional<Holder.Reference<Potion>> potionHolder = potionRegistry.getHolder(rl);
        if (potionHolder.isEmpty()) {
            LOGGER.error("[TradeBox] Potion not found in registry: {}", potionId);
            return null;
        }

        Item item = switch (potionType) {
            case "splash"    -> Items.SPLASH_POTION;
            case "lingering" -> Items.LINGERING_POTION;
            default          -> Items.POTION;
        };

        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potionHolder.get()));
        return stack;
    }

    private static boolean canAfford(Player player, List<CostEntry> cost) {
        for (CostEntry c : cost) {
            if (countItem(player, c.item()) < c.quantity()) return false;
        }
        return true;
    }

    private static void removeCost(Player player, List<CostEntry> cost) {
        for (CostEntry c : cost) removeItems(player, c.item(), c.quantity());
    }

    private static int countItem(Player player, String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return 0;
        Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.getHolder(rl);
        if (opt.isEmpty()) return 0;
        Item item = opt.get().value();
        int count = 0;
        for (ItemStack s : player.getInventory().items) if (s.is(item)) count += s.getCount();
        for (ItemStack s : player.getInventory().offhand) if (s.is(item)) count += s.getCount();
        return count;
    }

    private static void removeItems(Player player, String itemId, int amount) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return;
        Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.getHolder(rl);
        if (opt.isEmpty()) return;
        Item item = opt.get().value();
        int remaining = amount;
        for (ItemStack s : player.getInventory().items) {
            if (remaining <= 0) break;
            if (s.is(item)) { int take = Math.min(s.getCount(), remaining); s.shrink(take); remaining -= take; }
        }
        for (ItemStack s : player.getInventory().offhand) {
            if (remaining <= 0) break;
            if (s.is(item)) { int take = Math.min(s.getCount(), remaining); s.shrink(take); remaining -= take; }
        }
    }
}

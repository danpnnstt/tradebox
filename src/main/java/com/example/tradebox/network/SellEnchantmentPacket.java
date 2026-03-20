package com.example.tradebox.network;

import com.example.tradebox.TradeBoxMod;
import com.example.tradebox.block.SellerBlock;
import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.EnchantmentEntry;
import com.example.tradebox.config.EnchantmentShopConfig;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public record SellEnchantmentPacket(ResourceLocation enchantId, int level) implements CustomPacketPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeBoxMod.MOD_ID);

    public static final Type<SellEnchantmentPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TradeBoxMod.MOD_ID, "sell_enchantment"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SellEnchantmentPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeResourceLocation(p.enchantId()); buf.writeVarInt(p.level()); },
                    buf -> new SellEnchantmentPacket(buf.readResourceLocation(), buf.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SellEnchantmentPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> process(ctx.player(), packet.enchantId(), packet.level()));
    }

    private static void process(Player player, ResourceLocation enchantId, int level) {
        // Validate against server config
        EnchantmentEntry entry = EnchantmentShopConfig.getInstance().getEnchantments().stream()
                .filter(e -> e.isRefundable() && e.enchant_id().equals(enchantId.toString()) && e.level() == level)
                .findFirst().orElse(null);
        if (entry == null) {
            LOGGER.warn("[TradeBox] {} tried to refund unknown/non-refundable enchantment: {} lvl {}",
                    player.getName().getString(), enchantId, level);
            return;
        }

        // Find enchanted book in player inventory
        var enchRegOpt = player.level().registryAccess().lookup(Registries.ENCHANTMENT);
        if (enchRegOpt.isEmpty()) return;
        var enchHolderOpt = enchRegOpt.get().get(
                net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT, enchantId));
        if (enchHolderOpt.isEmpty()) {
            LOGGER.error("[TradeBox] Enchantment not in registry: {}", enchantId);
            return;
        }
        var enchHolder = enchHolderOpt.get();

        ItemStack bookStack = null;
        int bookSlot = -1;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack s = player.getInventory().items.get(i);
            if (s.is(Items.ENCHANTED_BOOK)) {
                var stored = s.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                if (stored.getLevel(enchHolder) == level) {
                    bookStack = s;
                    bookSlot  = i;
                    break;
                }
            }
        }
        if (bookStack == null) {
            LOGGER.debug("[TradeBox] {} tried to sell enchanted book not in inventory", player.getName().getString());
            return;
        }

        // Remove the book
        player.getInventory().items.get(bookSlot).shrink(1);

        // Calculate refund and give items
        int refundPct = SellerBlock.calculateRefundPercentage(player);
        for (CostEntry cost : entry.cost()) {
            int qty = Math.max(0, (int) Math.round(cost.quantity() * refundPct / 100.0));
            if (qty <= 0) continue;
            ResourceLocation itemRl = ResourceLocation.tryParse(cost.item());
            if (itemRl == null) continue;
            Optional<Holder.Reference<Item>> itemOpt = BuiltInRegistries.ITEM.getHolder(itemRl);
            if (itemOpt.isEmpty()) continue;
            ItemStack reward = new ItemStack(itemOpt.get().value(), qty);
            if (!player.addItem(reward)) player.drop(reward, false);
        }

        LOGGER.debug("[TradeBox] {} sold {} lvl {} for {}% refund", player.getName().getString(), enchantId, level, refundPct);
    }
}

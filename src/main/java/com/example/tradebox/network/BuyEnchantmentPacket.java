package com.example.tradebox.network;

import com.example.tradebox.TradeBoxMod;
import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.EnchantmentEntry;
import com.example.tradebox.config.EnchantmentShopConfig;
import net.minecraft.core.Holder;
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
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Sent by the client to the server when the player clicks "Purchase" in the Trade Box screen.
 */
public record BuyEnchantmentPacket(ResourceLocation enchantId, int level)
        implements CustomPacketPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeBoxMod.MOD_ID);

    public static final Type<BuyEnchantmentPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TradeBoxMod.MOD_ID, "buy_enchantment"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyEnchantmentPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeResourceLocation(packet.enchantId());
                        buf.writeVarInt(packet.level());
                    },
                    buf -> new BuyEnchantmentPacket(buf.readResourceLocation(), buf.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BuyEnchantmentPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            processPurchase(player, packet.enchantId(), packet.level());
        });
    }

    private static void processPurchase(Player player, ResourceLocation enchantId, int level) {
        // Find the matching entry in the server-side config
        List<EnchantmentEntry> entries = EnchantmentShopConfig.getInstance().getEnchantments();
        EnchantmentEntry entry = null;
        for (EnchantmentEntry e : entries) {
            if (e.enchant_id().equals(enchantId.toString()) && e.level() == level) {
                entry = e;
                break;
            }
        }

        if (entry == null) {
            LOGGER.warn("[TradeBox] Player {} tried to purchase unknown enchantment: {} level {}",
                    player.getName().getString(), enchantId, level);
            return;
        }

        // Verify player has all required items
        for (CostEntry cost : entry.cost()) {
            int available = countItemInInventory(player, cost.item());
            if (available < cost.quantity()) {
                LOGGER.debug("[TradeBox] Player {} cannot afford {} x{} (has {})",
                        player.getName().getString(), cost.item(), cost.quantity(), available);
                return;
            }
        }

        // Remove all required items
        for (CostEntry cost : entry.cost()) {
            removeItemFromInventory(player, cost.item(), cost.quantity());
        }

        // Create and give enchanted book
        ItemStack book = createEnchantedBook(player, enchantId, level);
        if (book != null) {
            if (!player.addItem(book)) {
                player.drop(book, false);
            }
            LOGGER.debug("[TradeBox] {} purchased {} level {}", player.getName().getString(), enchantId, level);
        }
    }

    private static int countItemInInventory(Player player, String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return 0;

        Optional<Holder.Reference<Item>> itemHolder = BuiltInRegistries.ITEM.getHolder(rl);
        if (itemHolder.isEmpty()) return 0;

        Item item = itemHolder.get().value();
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) count += stack.getCount();
        }
        // Also check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static void removeItemFromInventory(Player player, String itemId, int amount) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return;

        Optional<Holder.Reference<Item>> itemHolder = BuiltInRegistries.ITEM.getHolder(rl);
        if (itemHolder.isEmpty()) return;

        Item item = itemHolder.get().value();
        int remaining = amount;

        // Remove from main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (stack.is(item)) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }

        // Remove from offhand if still needed
        for (ItemStack stack : player.getInventory().offhand) {
            if (remaining <= 0) break;
            if (stack.is(item)) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
    }

    private static ItemStack createEnchantedBook(Player player, ResourceLocation enchantId, int level) {
        var enchantmentRegistry = player.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);

        Optional<Holder.Reference<Enchantment>> enchantHolder =
                enchantmentRegistry.getHolder(enchantId);

        if (enchantHolder.isEmpty()) {
            LOGGER.error("[TradeBox] Enchantment not found in registry: {}", enchantId);
            return null;
        }

        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(enchantHolder.get(), level);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        return book;
    }
}

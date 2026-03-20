package com.example.tradebox.network;

import com.example.tradebox.TradeBoxMod;
import com.example.tradebox.block.SellerBlock;
import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.PotionEntry;
import com.example.tradebox.config.PotionShopConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public record SellPotionPacket(String potionId, String potionType) implements CustomPacketPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeBoxMod.MOD_ID);

    public static final Type<SellPotionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TradeBoxMod.MOD_ID, "sell_potion"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SellPotionPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeUtf(p.potionId()); buf.writeUtf(p.potionType()); },
                    buf -> new SellPotionPacket(buf.readUtf(), buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SellPotionPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> process(ctx.player(), packet.potionId(), packet.potionType()));
    }

    private static void process(Player player, String potionId, String potionType) {
        PotionEntry entry = PotionShopConfig.getInstance().getPotions().stream()
                .filter(p -> p.isRefundable() && p.potion_id().equals(potionId) && p.type().equals(potionType))
                .findFirst().orElse(null);
        if (entry == null) {
            LOGGER.warn("[TradeBox] {} tried to refund unknown/non-refundable potion: {} ({})",
                    player.getName().getString(), potionId, potionType);
            return;
        }

        Item targetItem = switch (potionType) {
            case "splash"    -> Items.SPLASH_POTION;
            case "lingering" -> Items.LINGERING_POTION;
            default          -> Items.POTION;
        };

        ResourceLocation potionRl = ResourceLocation.tryParse(potionId);
        if (potionRl == null) return;

        // Find potion in inventory
        int potionSlot = -1;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack s = player.getInventory().items.get(i);
            if (!s.is(targetItem)) continue;
            var contents = s.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (contents.potion().isPresent()
                    && contents.potion().get().unwrapKey()
                        .map(k -> k.location().equals(potionRl)).orElse(false)) {
                potionSlot = i;
                break;
            }
        }
        if (potionSlot < 0) {
            LOGGER.debug("[TradeBox] {} tried to sell potion not in inventory", player.getName().getString());
            return;
        }

        player.getInventory().items.get(potionSlot).shrink(1);

        int refundPct = SellerBlock.calculateRefundPercentage(player);
        for (CostEntry cost : entry.cost()) {
            if (cost.item().equals("minecraft:glass_bottle")) continue; // not refunded
            int qty = Math.max(0, (int) Math.round(cost.quantity() * refundPct / 100.0));
            if (qty <= 0) continue;
            ResourceLocation itemRl = ResourceLocation.tryParse(cost.item());
            if (itemRl == null) continue;
            Optional<Holder.Reference<Item>> itemOpt = BuiltInRegistries.ITEM.getHolder(itemRl);
            if (itemOpt.isEmpty()) continue;
            ItemStack reward = new ItemStack(itemOpt.get().value(), qty);
            if (!player.addItem(reward)) player.drop(reward, false);
        }

        LOGGER.debug("[TradeBox] {} sold {} ({}) for {}% refund", player.getName().getString(), potionId, potionType, refundPct);
    }
}

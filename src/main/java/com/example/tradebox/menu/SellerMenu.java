package com.example.tradebox.menu;

import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.EnchantmentEntry;
import com.example.tradebox.config.PotionEntry;
import com.example.tradebox.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SellerMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final int refundPercentage;
    private final List<EnchantmentEntry> refundableEnchantments;
    private final List<PotionEntry> refundablePotions;

    /** Server-side — not called directly (uses extra buf via openMenu). */
    public SellerMenu(int containerId, Inventory inv, BlockPos blockPos, int refundPercentage,
                      List<EnchantmentEntry> enchants, List<PotionEntry> potions) {
        super(ModMenuTypes.SELLER_MENU.get(), containerId);
        this.blockPos             = blockPos;
        this.refundPercentage     = refundPercentage;
        this.refundableEnchantments = enchants;
        this.refundablePotions    = potions;
    }

    /** Client-side factory — reads everything from the extra buf. */
    public SellerMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        super(ModMenuTypes.SELLER_MENU.get(), containerId);
        this.blockPos         = buf.readBlockPos();
        this.refundPercentage = buf.readVarInt();

        int enchantCount = buf.readVarInt();
        List<EnchantmentEntry> enchants = new ArrayList<>(enchantCount);
        for (int i = 0; i < enchantCount; i++) {
            String id    = buf.readUtf();
            int level    = buf.readVarInt();
            int costSize = buf.readVarInt();
            List<CostEntry> costs = new ArrayList<>(costSize);
            for (int j = 0; j < costSize; j++) costs.add(new CostEntry(buf.readUtf(), buf.readVarInt()));
            enchants.add(new EnchantmentEntry(id, level, costs));
        }
        this.refundableEnchantments = Collections.unmodifiableList(enchants);

        int potionCount = buf.readVarInt();
        List<PotionEntry> potions = new ArrayList<>(potionCount);
        for (int i = 0; i < potionCount; i++) {
            String pid   = buf.readUtf();
            String type  = buf.readUtf();
            int costSize = buf.readVarInt();
            List<CostEntry> costs = new ArrayList<>(costSize);
            for (int j = 0; j < costSize; j++) costs.add(new CostEntry(buf.readUtf(), buf.readVarInt()));
            potions.add(new PotionEntry(pid, type, costs));
        }
        this.refundablePotions = Collections.unmodifiableList(potions);
    }

    public BlockPos getBlockPos()                        { return blockPos; }
    public int getRefundPercentage()                     { return refundPercentage; }
    public List<EnchantmentEntry> getRefundableEnchantments() { return refundableEnchantments; }
    public List<PotionEntry> getRefundablePotions()      { return refundablePotions; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) <= 64.0;
    }
}

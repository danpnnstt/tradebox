package com.example.tradebox.menu;

import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.EnchantmentEntry;
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

public class TradeBoxMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final List<EnchantmentEntry> enchantments;

    /** Server-side constructor — called by the block entity. */
    public TradeBoxMenu(int containerId, Inventory playerInventory, BlockPos blockPos,
                        List<EnchantmentEntry> enchantments) {
        super(ModMenuTypes.TRADE_BOX_MENU.get(), containerId);
        this.blockPos = blockPos;
        this.enchantments = enchantments;
    }

    /** Client-side constructor — called by IMenuTypeExtension factory. Reads everything from buf. */
    public TradeBoxMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(ModMenuTypes.TRADE_BOX_MENU.get(), containerId);
        this.blockPos = buf.readBlockPos();

        int enchantCount = buf.readVarInt();
        List<EnchantmentEntry> list = new ArrayList<>(enchantCount);
        for (int i = 0; i < enchantCount; i++) {
            String enchantId = buf.readUtf();
            int level = buf.readVarInt();
            int costCount = buf.readVarInt();
            List<CostEntry> costs = new ArrayList<>(costCount);
            for (int j = 0; j < costCount; j++) {
                costs.add(new CostEntry(buf.readUtf(), buf.readVarInt()));
            }
            list.add(new EnchantmentEntry(enchantId, level, costs));
        }
        this.enchantments = Collections.unmodifiableList(list);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public List<EnchantmentEntry> getEnchantments() {
        return enchantments;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(
                blockPos.getX() + 0.5,
                blockPos.getY() + 0.5,
                blockPos.getZ() + 0.5
        ) <= 64.0;
    }
}

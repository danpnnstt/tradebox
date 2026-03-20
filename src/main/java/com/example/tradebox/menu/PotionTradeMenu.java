package com.example.tradebox.menu;

import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.GlassBottleEntry;
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

public class PotionTradeMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final List<PotionEntry> potions;
    private final GlassBottleEntry glassBottles;

    /** Server-side constructor. */
    public PotionTradeMenu(int containerId, Inventory playerInventory, BlockPos blockPos,
                           List<PotionEntry> potions, GlassBottleEntry glassBottles) {
        super(ModMenuTypes.POTION_TRADE_MENU.get(), containerId);
        this.blockPos = blockPos;
        this.potions = potions;
        this.glassBottles = glassBottles;
    }

    /** Client-side constructor — reads everything from the extra data buffer. */
    public PotionTradeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(ModMenuTypes.POTION_TRADE_MENU.get(), containerId);
        this.blockPos = buf.readBlockPos();

        // Read glass bottle entry
        int bottleQty = buf.readVarInt();
        int bottleCostCount = buf.readVarInt();
        List<CostEntry> bottleCosts = new ArrayList<>(bottleCostCount);
        for (int i = 0; i < bottleCostCount; i++) {
            bottleCosts.add(new CostEntry(buf.readUtf(), buf.readVarInt()));
        }
        this.glassBottles = new GlassBottleEntry(bottleQty, bottleCosts);

        // Read potion list
        int potionCount = buf.readVarInt();
        List<PotionEntry> list = new ArrayList<>(potionCount);
        for (int i = 0; i < potionCount; i++) {
            String potionId = buf.readUtf();
            String type = buf.readUtf();
            int costCount = buf.readVarInt();
            List<CostEntry> costs = new ArrayList<>(costCount);
            for (int j = 0; j < costCount; j++) {
                costs.add(new CostEntry(buf.readUtf(), buf.readVarInt()));
            }
            list.add(new PotionEntry(potionId, type, costs));
        }
        this.potions = Collections.unmodifiableList(list);
    }

    public BlockPos getBlockPos() { return blockPos; }
    public List<PotionEntry> getPotions() { return potions; }
    public GlassBottleEntry getGlassBottles() { return glassBottles; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(
                blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5
        ) <= 64.0;
    }
}

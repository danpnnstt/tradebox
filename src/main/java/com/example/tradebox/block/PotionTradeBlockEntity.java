package com.example.tradebox.block;

import com.example.tradebox.config.PotionShopConfig;
import com.example.tradebox.menu.PotionTradeMenu;
import com.example.tradebox.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PotionTradeBlockEntity extends BlockEntity implements MenuProvider {

    public PotionTradeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POTION_TRADE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tradebox.potion_trade_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PotionTradeMenu(containerId, playerInventory, this.worldPosition,
                PotionShopConfig.getInstance().getPotions(),
                PotionShopConfig.getInstance().getGlassBottles());
    }
}

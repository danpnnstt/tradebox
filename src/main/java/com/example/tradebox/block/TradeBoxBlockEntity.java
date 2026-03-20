package com.example.tradebox.block;

import com.example.tradebox.config.EnchantmentShopConfig;
import com.example.tradebox.menu.TradeBoxMenu;
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

public class TradeBoxBlockEntity extends BlockEntity implements MenuProvider {

    public TradeBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRADE_BOX.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tradebox.trade_box");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
                                                      Player player) {
        return new TradeBoxMenu(containerId, playerInventory, this.worldPosition,
                EnchantmentShopConfig.getInstance().getEnchantments());
    }
}

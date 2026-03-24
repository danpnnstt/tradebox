package com.example.tradebox.block;

import com.example.tradebox.config.EnchantmentShopConfig;
import com.example.tradebox.config.PotionShopConfig;
import com.example.tradebox.config.EnchantmentEntry;
import com.example.tradebox.config.PotionEntry;
import com.example.tradebox.menu.SellerMenu;
import com.example.tradebox.registry.ModBlockEntities;
import com.example.tradebox.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SellerBlockEntity extends BlockEntity implements MenuProvider {

    public SellerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SELLER_BLOCK.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.tradebox.seller_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        int refundPct = SellerBlock.calculateRefundPercentage(player);
        var enchants = EnchantmentShopConfig.getInstance().getEnchantments()
                .stream().filter(EnchantmentEntry::isRefundable).toList();
        var potions = PotionShopConfig.getInstance().getPotions()
                .stream().filter(PotionEntry::isRefundable).toList();
        return new SellerMenu(containerId, playerInventory, worldPosition, refundPct, enchants, potions);
    }
}

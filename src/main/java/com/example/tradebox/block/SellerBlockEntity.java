package com.example.tradebox.block;

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
        // Menu is created with extra data via SellerBlock.useWithoutItem — this path is not used
        return null;
    }
}

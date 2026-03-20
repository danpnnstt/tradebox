package com.example.tradebox.block;

import com.example.tradebox.config.CostEntry;
import com.example.tradebox.config.EnchantmentShopConfig;
import com.example.tradebox.config.PotionShopConfig;
import com.example.tradebox.config.TradeBoxConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SellerBlock extends BaseEntityBlock {

    public static final MapCodec<SellerBlock> CODEC = simpleCodec(SellerBlock::new);

    public SellerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SellerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SellerBlockEntity entity)) return InteractionResult.CONSUME;

        int refundPct = calculateRefundPercentage(player);

        var enchants = EnchantmentShopConfig.getInstance().getEnchantments()
                .stream().filter(e -> e.isRefundable()).toList();

        var potions = PotionShopConfig.getInstance().getPotions()
                .stream().filter(p -> p.isRefundable()).toList();

        player.openMenu(entity, buf -> {
            buf.writeBlockPos(pos);
            buf.writeVarInt(refundPct);

            // Refundable enchantments
            buf.writeVarInt(enchants.size());
            for (var e : enchants) {
                buf.writeUtf(e.enchant_id());
                buf.writeVarInt(e.level());
                buf.writeVarInt(e.cost().size());
                for (var c : e.cost()) { buf.writeUtf(c.item()); buf.writeVarInt(c.quantity()); }
            }

            // Refundable potions — exclude glass_bottle from cost (not refunded)
            buf.writeVarInt(potions.size());
            for (var p : potions) {
                buf.writeUtf(p.potion_id());
                buf.writeUtf(p.type());
                List<CostEntry> filteredCost = p.cost().stream()
                        .filter(c -> !c.item().equals("minecraft:glass_bottle"))
                        .toList();
                buf.writeVarInt(filteredCost.size());
                for (var c : filteredCost) { buf.writeUtf(c.item()); buf.writeVarInt(c.quantity()); }
            }
        });

        return InteractionResult.CONSUME;
    }

    /** Calculate effective refund % based on player's equipped Smooth Seller level. */
    public static int calculateRefundPercentage(Player player) {
        var cfg = TradeBoxConfig.getInstance();
        int smoothLevel = getSmoothSellerLevel(player);
        int effective = Math.min(smoothLevel, cfg.getMaxEnchantLevel());
        return cfg.getDefaultRefundPercentage() + effective * cfg.getEnchantLevelIncrease();
    }

    private static int getSmoothSellerLevel(Player player) {
        var regAccess = player.level().registryAccess();
        var enchRegOpt = regAccess.lookup(Registries.ENCHANTMENT);
        if (enchRegOpt.isEmpty()) return 0;

        var ssKey = ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath("tradebox", "smooth_seller"));
        var ssHolderOpt = enchRegOpt.get().get(ssKey);
        if (ssHolderOpt.isEmpty()) return 0;
        var ssHolder = ssHolderOpt.get();

        int max = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            var enc = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            max = Math.max(max, enc.getLevel(ssHolder));
        }
        for (ItemStack stack : player.getHandSlots()) {
            var enc = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            max = Math.max(max, enc.getLevel(ssHolder));
        }
        return max;
    }
}

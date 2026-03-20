package com.example.tradebox.block;

import com.example.tradebox.config.PotionShopConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PotionTradeBlock extends BaseEntityBlock {

    public static final MapCodec<PotionTradeBlock> CODEC = simpleCodec(PotionTradeBlock::new);

    public PotionTradeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotionTradeBlockEntity(pos, state);
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
        if (be instanceof PotionTradeBlockEntity entity) {
            var potions = PotionShopConfig.getInstance().getPotions()
                    .stream().filter(com.example.tradebox.config.PotionEntry::isSellable).toList();
            var bottles = PotionShopConfig.getInstance().getGlassBottles();

            player.openMenu(entity, buf -> {
                buf.writeBlockPos(pos);

                // Glass bottle entry
                buf.writeVarInt(bottles.quantity());
                buf.writeVarInt(bottles.cost().size());
                for (var c : bottles.cost()) { buf.writeUtf(c.item()); buf.writeVarInt(c.quantity()); }

                // Potion list
                buf.writeVarInt(potions.size());
                for (var p : potions) {
                    buf.writeUtf(p.potion_id());
                    buf.writeUtf(p.type());
                    buf.writeVarInt(p.cost().size());
                    for (var c : p.cost()) { buf.writeUtf(c.item()); buf.writeVarInt(c.quantity()); }
                }
            });
        }
        return InteractionResult.CONSUME;
    }
}

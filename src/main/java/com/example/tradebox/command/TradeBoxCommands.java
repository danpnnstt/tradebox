package com.example.tradebox.command;

import com.example.tradebox.config.EnchantmentShopConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TradeBoxCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tradebox")
                .requires(src -> src.hasPermission(2)) // op level 2
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        EnchantmentShopConfig.load();
                        int count = EnchantmentShopConfig.getInstance().getEnchantments().size();
                        ctx.getSource().sendSuccess(
                            () -> Component.literal(
                                "[TradeBox] Config reloaded. " + count + " enchantment(s) loaded."
                            ),
                            true
                        );
                        return 1;
                    })
                )
        );
    }
}

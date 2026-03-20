package com.example.tradebox.command;

import com.example.tradebox.config.EnchantmentShopConfig;
import com.example.tradebox.config.PotionShopConfig;
import com.example.tradebox.config.TradeBoxConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class TradeBoxCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tradebox")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        EnchantmentShopConfig.load();
                        PotionShopConfig.load();
                        TradeBoxConfig.load();
                        int enchants = EnchantmentShopConfig.getInstance().getEnchantments().size();
                        int potions  = PotionShopConfig.getInstance().getPotions().size();
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("[TradeBox] Config reloaded. "
                                + enchants + " enchantment(s), " + potions + " potion(s) loaded."),
                            true
                        );
                        return enchants + potions;
                    })
                )
                .then(Commands.literal("export")
                    .executes(ctx -> {
                        var registryAccess = ctx.getSource().getServer().registryAccess();
                        var enchantRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);

                        Path outputPath = FMLPaths.GAMEDIR.get().resolve("tradebox_enchantment_export.csv");
                        AtomicInteger count = new AtomicInteger(0);

                        try (Writer writer = Files.newBufferedWriter(outputPath)) {
                            writer.write("Mod,Enchantment ID,Min Level,Max Level\n");

                            enchantRegistry.holders()
                                .sorted(Comparator.comparing(h -> h.key().location().toString()))
                                .forEach(holder -> {
                                    try {
                                        String id = holder.key().location().toString();
                                        String namespace = holder.key().location().getNamespace();
                                        Enchantment enchantment = holder.value();
                                        String modName = resolveModName(namespace);
                                        int maxLevel = enchantment.getMaxLevel();

                                        writer.write(csvField(modName) + "," + csvField(id) + ",1," + maxLevel + "\n");
                                        count.incrementAndGet();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                        } catch (IOException e) {
                            ctx.getSource().sendFailure(
                                Component.literal("[TradeBox] Failed to write export file: " + e.getMessage())
                            );
                            return 0;
                        }

                        int total = count.get();
                        ctx.getSource().sendSuccess(
                            () -> Component.literal(
                                "[TradeBox] Exported " + total + " enchantment(s) to: " + outputPath
                            ),
                            true
                        );
                        return total;
                    })
                )
        );
    }

    /** Returns the human-readable mod name for a given registry namespace. */
    private static String resolveModName(String namespace) {
        if (namespace.equals("minecraft")) {
            return "Vanilla";
        }
        return ModList.get()
            .getModContainerById(namespace)
            .map(container -> container.getModInfo().getDisplayName())
            .orElse(namespace);
    }

    /** Wraps a CSV field in quotes if it contains a comma, quote, or newline. */
    private static String csvField(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

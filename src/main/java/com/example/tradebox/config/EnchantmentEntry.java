package com.example.tradebox.config;

import java.util.List;

/**
 * Represents a single purchasable enchantment in the trade box config.
 *
 * @param enchant_id The enchantment's resource location (e.g., "minecraft:mending")
 * @param level      The level of the enchantment book (1 = I, 2 = II, etc.)
 * @param cost       List of items required to purchase this enchantment
 */
public record EnchantmentEntry(String enchant_id, int level, List<CostEntry> cost) {
}

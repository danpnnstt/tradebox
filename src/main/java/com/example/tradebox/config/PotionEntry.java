package com.example.tradebox.config;

import java.util.List;

/**
 * Represents a single purchasable potion in the potion shop config.
 *
 * @param potion_id The potion's resource location (e.g., "minecraft:healing")
 * @param type      "regular", "splash", or "lingering"
 * @param cost      List of items required to purchase this potion
 */
public record PotionEntry(String potion_id, String type, List<CostEntry> cost) {
}

package com.example.tradebox.config;

/**
 * Represents a single item cost requirement for purchasing an enchantment.
 *
 * @param item     The item's resource location string (e.g., "minecraft:gold_ingot")
 * @param quantity How many of this item are required
 */
public record CostEntry(String item, int quantity) {
}

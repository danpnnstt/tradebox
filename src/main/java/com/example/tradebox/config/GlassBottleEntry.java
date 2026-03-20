package com.example.tradebox.config;

import java.util.List;

/**
 * Represents the glass bottle bulk-buy offer in the potion shop config.
 *
 * @param quantity How many glass bottles the player receives per purchase
 * @param cost     List of items required to buy that quantity
 */
public record GlassBottleEntry(int quantity, List<CostEntry> cost) {
}

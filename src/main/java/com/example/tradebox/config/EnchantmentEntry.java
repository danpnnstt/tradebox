package com.example.tradebox.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a single purchasable/refundable enchantment in the trade box config.
 * Uses boxed Boolean so Gson leaves fields null (absent) rather than defaulting to false.
 */
public class EnchantmentEntry {

    private String enchant_id;
    private int level;
    private List<CostEntry> cost;

    @SerializedName("is_sellable")
    private Boolean isSellable;

    @SerializedName("is_refundable")
    private Boolean isRefundable;

    /** Full constructor used when building the default config. */
    public EnchantmentEntry(String enchant_id, int level, List<CostEntry> cost,
                            Boolean isSellable, Boolean isRefundable) {
        this.enchant_id  = enchant_id;
        this.level       = level;
        this.cost        = cost;
        this.isSellable  = isSellable;
        this.isRefundable = isRefundable;
    }

    /** 3-arg constructor for wire deserialization (is_sellable/is_refundable irrelevant after filtering). */
    public EnchantmentEntry(String enchant_id, int level, List<CostEntry> cost) {
        this(enchant_id, level, cost, null, null);
    }

    public String enchant_id()   { return enchant_id; }
    public int level()           { return level; }
    public List<CostEntry> cost(){ return cost; }

    /** Defaults to true when absent from JSON. */
    public boolean isSellable()   { return isSellable  == null || isSellable; }
    /** Defaults to true when absent from JSON. */
    public boolean isRefundable() { return isRefundable == null || isRefundable; }
}

package com.example.tradebox.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a single purchasable/refundable potion in the potion shop config.
 */
public class PotionEntry {

    private String potion_id;
    private String type;
    private List<CostEntry> cost;

    @SerializedName("is_sellable")
    private Boolean isSellable;

    @SerializedName("is_refundable")
    private Boolean isRefundable;

    public PotionEntry(String potion_id, String type, List<CostEntry> cost,
                       Boolean isSellable, Boolean isRefundable) {
        this.potion_id  = potion_id;
        this.type       = type;
        this.cost       = cost;
        this.isSellable = isSellable;
        this.isRefundable = isRefundable;
    }

    public PotionEntry(String potion_id, String type, List<CostEntry> cost) {
        this(potion_id, type, cost, null, null);
    }

    public String potion_id()    { return potion_id; }
    public String type()         { return type; }
    public List<CostEntry> cost(){ return cost; }

    public boolean isSellable()   { return isSellable  == null || isSellable; }
    public boolean isRefundable() { return isRefundable == null || isRefundable; }
}

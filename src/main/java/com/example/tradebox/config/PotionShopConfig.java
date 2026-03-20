package com.example.tradebox.config;

import com.example.tradebox.TradeBoxMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PotionShopConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeBoxMod.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "tradebox_potions.json";

    private static PotionShopConfig INSTANCE;

    private List<PotionEntry> potions = new ArrayList<>();
    private GlassBottleEntry glassBottles = new GlassBottleEntry(4, List.of(new CostEntry("minecraft:emerald", 1)));

    private PotionShopConfig() {}

    public static PotionShopConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PotionShopConfig();
        }
        return INSTANCE;
    }

    public List<PotionEntry> getPotions() {
        return Collections.unmodifiableList(potions);
    }

    public GlassBottleEntry getGlassBottles() {
        return glassBottles;
    }

    public static void load() {
        Path configFile = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configFile)) {
            LOGGER.info("[TradeBox] Potion config not found, creating default at: {}", configFile);
            writeDefaultConfig(configFile);
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            ConfigRoot root = GSON.fromJson(reader, ConfigRoot.class);
            if (root != null) {
                if (root.potions != null) getInstance().potions = root.potions;
                if (root.glassBottles != null) getInstance().glassBottles = root.glassBottles;
                LOGGER.info("[TradeBox] Loaded {} potion entries from config.", getInstance().potions.size());
            } else {
                LOGGER.warn("[TradeBox] Potion config is empty or malformed: {}", configFile);
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("[TradeBox] Failed to read potion config: {}", configFile, e);
        }
    }

    private static void writeDefaultConfig(Path configFile) {
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(buildDefaultConfig(), writer);
        } catch (IOException e) {
            LOGGER.error("[TradeBox] Failed to write default potion config: {}", configFile, e);
        }
    }

    private static ConfigRoot buildDefaultConfig() {
        ConfigRoot root = new ConfigRoot();
        root.glassBottles = new GlassBottleEntry(4, List.of(new CostEntry("minecraft:emerald", 1)));
        root.potions = new ArrayList<>();

        // Helper: builds a potion entry cost list (always includes 1 glass bottle)
        // addPotion(id, type, extraEmeralds, extraGold)
        List<String> types = List.of("regular", "splash", "lingering");
        int[][] typeMultipliers = {{0, 1}, {2, 1}, {4, 2}}; // [extraEmerald, extraGold] per type

        record PotionSpec(String id, int baseEmerald, int baseGold) {}

        List<PotionSpec> specs = new ArrayList<>();

        // Tier 1 — common
        specs.add(new PotionSpec("minecraft:swiftness",          4, 2));
        specs.add(new PotionSpec("minecraft:long_swiftness",     6, 3));
        specs.add(new PotionSpec("minecraft:night_vision",       4, 2));
        specs.add(new PotionSpec("minecraft:long_night_vision",  6, 3));
        specs.add(new PotionSpec("minecraft:water_breathing",    4, 2));
        specs.add(new PotionSpec("minecraft:long_water_breathing", 6, 3));
        specs.add(new PotionSpec("minecraft:invisibility",       6, 3));
        specs.add(new PotionSpec("minecraft:long_invisibility",  8, 4));
        specs.add(new PotionSpec("minecraft:leaping",            4, 2));
        specs.add(new PotionSpec("minecraft:long_leaping",       6, 3));
        specs.add(new PotionSpec("minecraft:fire_resistance",    6, 3));
        specs.add(new PotionSpec("minecraft:long_fire_resistance", 8, 4));
        specs.add(new PotionSpec("minecraft:slowness",           4, 2));
        specs.add(new PotionSpec("minecraft:long_slowness",      6, 3));
        specs.add(new PotionSpec("minecraft:weakness",           4, 2));
        specs.add(new PotionSpec("minecraft:long_weakness",      6, 3));
        specs.add(new PotionSpec("minecraft:slow_falling",       6, 3));
        specs.add(new PotionSpec("minecraft:long_slow_falling",  8, 4));

        // Tier 2 — combat
        specs.add(new PotionSpec("minecraft:healing",            8, 4));
        specs.add(new PotionSpec("minecraft:regeneration",       8, 4));
        specs.add(new PotionSpec("minecraft:long_regeneration",  10, 5));
        specs.add(new PotionSpec("minecraft:poison",             6, 3));
        specs.add(new PotionSpec("minecraft:long_poison",        8, 4));
        specs.add(new PotionSpec("minecraft:strength",           8, 4));
        specs.add(new PotionSpec("minecraft:long_strength",      10, 5));
        specs.add(new PotionSpec("minecraft:harming",            6, 3));

        // Tier 3 — powerful
        specs.add(new PotionSpec("minecraft:strong_swiftness",   10, 5));
        specs.add(new PotionSpec("minecraft:strong_leaping",     10, 5));
        specs.add(new PotionSpec("minecraft:strong_healing",     14, 7));
        specs.add(new PotionSpec("minecraft:strong_regeneration", 14, 7));
        specs.add(new PotionSpec("minecraft:strong_poison",      10, 5));
        specs.add(new PotionSpec("minecraft:strong_strength",    14, 7));
        specs.add(new PotionSpec("minecraft:strong_harming",     12, 6));
        specs.add(new PotionSpec("minecraft:strong_slowness",    10, 5));

        // Tier 4 — special/rare
        specs.add(new PotionSpec("minecraft:turtle_master",       16, 8));
        specs.add(new PotionSpec("minecraft:long_turtle_master",  18, 9));
        specs.add(new PotionSpec("minecraft:strong_turtle_master", 20, 10));
        specs.add(new PotionSpec("minecraft:luck",               20, 10));

        // 1.21 new potions
        specs.add(new PotionSpec("minecraft:wind_charged",       12, 6));
        specs.add(new PotionSpec("minecraft:weaving",            12, 6));
        specs.add(new PotionSpec("minecraft:oozing",             12, 6));
        specs.add(new PotionSpec("minecraft:infested",           10, 5));

        for (PotionSpec spec : specs) {
            for (int t = 0; t < types.size(); t++) {
                int emeralds = spec.baseEmerald() + typeMultipliers[t][0];
                int gold = spec.baseGold() + typeMultipliers[t][1];
                root.potions.add(new PotionEntry(spec.id(), types.get(t), List.of(
                        new CostEntry("minecraft:glass_bottle", 1),
                        new CostEntry("minecraft:emerald", emeralds),
                        new CostEntry("minecraft:gold_ingot", gold)
                )));
            }
        }

        return root;
    }

    private static class ConfigRoot {
        @SerializedName("GlassBottles")
        GlassBottleEntry glassBottles;

        @SerializedName("Potions")
        List<PotionEntry> potions;
    }
}

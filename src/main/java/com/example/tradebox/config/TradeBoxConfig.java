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

/**
 * Reads tradebox_config.json from the config directory.
 * Controls default refund percentage and Smooth Seller enchantment behaviour.
 */
public class TradeBoxConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeBoxMod.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "tradebox_config.json";

    private static TradeBoxConfig INSTANCE = new TradeBoxConfig();

    @SerializedName("default_refund_percentage")
    private int defaultRefundPercentage = 50;

    @SerializedName("enchant_level_increase")
    private int enchantLevelIncrease = 10;

    @SerializedName("max_enchant_level")
    private int maxEnchantLevel = 15;

    private TradeBoxConfig() {}

    public static TradeBoxConfig getInstance() { return INSTANCE; }

    public int getDefaultRefundPercentage() { return defaultRefundPercentage; }
    public int getEnchantLevelIncrease()    { return enchantLevelIncrease; }
    public int getMaxEnchantLevel()         { return maxEnchantLevel; }

    public static void load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
        if (!Files.exists(path)) {
            LOGGER.info("[TradeBox] Creating default tradebox_config.json at: {}", path);
            saveDefault(path);
        }
        try (Reader r = Files.newBufferedReader(path)) {
            TradeBoxConfig loaded = GSON.fromJson(r, TradeBoxConfig.class);
            INSTANCE = (loaded != null) ? loaded : new TradeBoxConfig();
            LOGGER.info("[TradeBox] Loaded tradebox_config.json — refund={}%, +{}%/level, maxLevel={}",
                    INSTANCE.defaultRefundPercentage, INSTANCE.enchantLevelIncrease, INSTANCE.maxEnchantLevel);
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("[TradeBox] Failed to read tradebox_config.json: {}", path, e);
            INSTANCE = new TradeBoxConfig();
        }
    }

    private static void saveDefault(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(new TradeBoxConfig(), w);
        } catch (IOException e) {
            LOGGER.error("[TradeBox] Failed to write default tradebox_config.json", e);
        }
    }
}

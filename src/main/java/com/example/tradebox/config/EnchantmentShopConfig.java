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

public class EnchantmentShopConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeBoxMod.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "tradebox_enchantments.json";

    private static EnchantmentShopConfig INSTANCE;

    private List<EnchantmentEntry> enchantments = new ArrayList<>();

    private EnchantmentShopConfig() {}

    public static EnchantmentShopConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EnchantmentShopConfig();
        }
        return INSTANCE;
    }

    public List<EnchantmentEntry> getEnchantments() {
        return Collections.unmodifiableList(enchantments);
    }

    /** Called during common setup to load (or create) the config file. */
    public static void load() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configFile)) {
            LOGGER.info("[TradeBox] Config file not found, creating default at: {}", configFile);
            writeDefaultConfig(configFile);
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            ConfigRoot root = GSON.fromJson(reader, ConfigRoot.class);
            if (root != null && root.enchantments != null) {
                getInstance().enchantments = root.enchantments;
                LOGGER.info("[TradeBox] Loaded {} enchantment entries from config.", root.enchantments.size());
            } else {
                LOGGER.warn("[TradeBox] Config file is empty or malformed: {}", configFile);
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("[TradeBox] Failed to read config file: {}", configFile, e);
        }
    }

    private static void writeDefaultConfig(Path configFile) {
        ConfigRoot defaultConfig = buildDefaultConfig();
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(defaultConfig, writer);
        } catch (IOException e) {
            LOGGER.error("[TradeBox] Failed to write default config file: {}", configFile, e);
        }
    }

    private static ConfigRoot buildDefaultConfig() {
        List<EnchantmentEntry> entries = new ArrayList<>();

        // Mending I
        entries.add(new EnchantmentEntry("minecraft:mending", 1, List.of(
                new CostEntry("minecraft:gold_ingot", 12),
                new CostEntry("minecraft:emerald", 64)
        )));

        // Unbreaking I through V
        for (int lvl = 1; lvl <= 3; lvl++) {
            entries.add(new EnchantmentEntry("minecraft:unbreaking", lvl, List.of(
                    new CostEntry("minecraft:gold_ingot", 6 * lvl),
                    new CostEntry("minecraft:emerald", 16 * lvl)
            )));
        }

        // Power V
        entries.add(new EnchantmentEntry("minecraft:power", 5, List.of(
                new CostEntry("minecraft:gold_ingot", 10),
                new CostEntry("minecraft:emerald", 32)
        )));

        // Sharpness V
        entries.add(new EnchantmentEntry("minecraft:sharpness", 5, List.of(
                new CostEntry("minecraft:gold_ingot", 10),
                new CostEntry("minecraft:emerald", 32)
        )));

        // Protection IV
        entries.add(new EnchantmentEntry("minecraft:protection", 4, List.of(
                new CostEntry("minecraft:gold_ingot", 8),
                new CostEntry("minecraft:emerald", 24)
        )));

        ConfigRoot root = new ConfigRoot();
        root.enchantments = entries;
        return root;
    }

    /** Internal wrapper class matching the JSON root structure. */
    private static class ConfigRoot {
        @SerializedName("Enchantments")
        List<EnchantmentEntry> enchantments;
    }
}

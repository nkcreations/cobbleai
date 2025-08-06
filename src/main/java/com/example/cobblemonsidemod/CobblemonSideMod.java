package com.example.cobblemonsidemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CobblemonSideMod implements ModInitializer {
    public static final String MOD_ID = "cobblemonsidemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Config config;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cobblemonsidemod.json");

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Cobblemon Side-Mod...");
        loadConfig();
    }

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            config = new Config();
            saveConfig();
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                LOGGER.error("Error al cargar la configuración, usando valores por defecto.", e);
                config = new Config();
            }
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            LOGGER.error("Error al guardar la configuración.", e);
        }
    }

    public static class Config {
        public int flyingMinHeight = 2;
        public int flyingMaxHeight = 15;
        public String[] diggingBlocks = {"minecraft:dirt", "minecraft:stone"};
        public int diggingBlockBreakTime = 20;
        public int curiousFollowDistance = 10;
        public String[] hostilePokemonList = {"pikachu", "charmander"};
    }
}

package com.example.cobblemonsidemod;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CobblemonSideMod implements ModInitializer {
    public static final String MOD_ID = "cobblemonsidemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Config config;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cobblemonsidemod.json");
    private final Random random = new Random();

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Cobblemon Side-Mod...");

        loadConfig();

        // Registramos el evento para cuando se crea un Pokémon.
        // Aquí es donde inyectamos nuestros nuevos comportamientos.
        CobblemonEvents.POKEMON_SPAWN.subscribe(event -> {
            Pokemon pokemon = event.getPokemon();
            PokemonEntity entity = event.getEntity();

            // Inyectamos los nuevos objetivos de comportamiento en el selector de objetivos del Pokémon.
            GoalSelector goalSelector = entity.goalSelector;

            // Comportamiento 'Volador' (Swablu)
            if (pokemon.getSpecies().getName().equalsIgnoreCase("swablu")) {
                goalSelector.add(1, new FlyingPathfindGoal(entity, config.flyingMinHeight, config.flyingMaxHeight));
            }

            // Comportamiento 'Corredor' (Electrike)
            if (pokemon.getSpecies().getName().equalsIgnoreCase("electrike")) {
                // El comportamiento de correr se asemeja a 'WanderAroundGoal'.
                // Podríamos crear una meta personalizada para priorizar la velocidad.
                goalSelector.add(2, new WanderAroundGoal(entity, 1.5, 60)); // 1.5 es la velocidad, 60 es la frecuencia
            }

            // Comportamiento 'Recolector' (Teddiursa)
            if (pokemon.getSpecies().getName().equalsIgnoreCase("teddiursa")) {
                 goalSelector.add(3, new GatheringGoal(entity));
            }

            // Comportamiento 'Topo' (Diglett)
            if (pokemon.getSpecies().getName().equalsIgnoreCase("diglett")) {
                goalSelector.add(4, new DiggingGoal(entity, config.diggingBlocks, config.diggingBlockBreakTime));
            }

            // Comportamiento 'Curioso' (Spheal)
            if (pokemon.getSpecies().getName().equalsIgnoreCase("spheal")) {
                goalSelector.add(5, new CuriousGoal(entity, 1.0D, config.curiousFollowDistance));
            }
        });

        // Evento para manejar la interacción del jugador con entidades
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof PokemonEntity pokemonEntity) {
                Pokemon pokemon = pokemonEntity.getPokemon();

                // Solo aplicamos la lógica de interacción a Spheal (el Pokémon 'curioso')
                if (pokemon.getSpecies().getName().equalsIgnoreCase("spheal")) {
                    ItemStack heldItem = player.getStackInHand(hand);

                    if (!heldItem.isEmpty()) {
                        Item item = heldItem.getItem();

                        // Lógica para comer o reaccionar.
                        if (item.isFood()) {
                            LOGGER.info(pokemon.getDisplayName().getString() + " se come el ítem del jugador.");
                            heldItem.decrement(1);
                            return ActionResult.SUCCESS;
                        }

                        FoodComponent foodComponent = item.getFoodComponent();
                        boolean isPoisonous = false;
                        if (foodComponent != null) {
                           for (Pair<StatusEffectInstance, Float> effect : foodComponent.getStatusEffects()) {
                                if (effect.getFirst().getEffectType().equals(StatusEffects.POISON)) {
                                    isPoisonous = true;
                                    break;
                                }
                            }
                        }

                        if (isPoisonous) {
                            LOGGER.info(pokemon.getDisplayName().getString() + " reacciona a un ítem hostil.");

                            List<String> hostileList = Arrays.asList(config.hostilePokemonList);
                            if (hostileList.contains(pokemon.getSpecies().getName().toLowerCase())) {
                                LOGGER.info(pokemon.getDisplayName().getString() + " se vuelve hostil y te ataca.");
                                pokemonEntity.setTarget(player);
                            } else {
                                LOGGER.info(pokemon.getDisplayName().getString() + " huye del jugador.");
                                pokemonEntity.getNavigation().startMovingTo(pokemonEntity.getX() + random.nextInt(10) - 5, pokemonEntity.getY(), pokemonEntity.getZ() + random.nextInt(10) - 5, 2.0);
                            }
                            return ActionResult.SUCCESS;
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    private void loadConfig() {
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

    private void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            LOGGER.error("Error al guardar la configuración.", e);
        }
    }

    // Clase para la configuración.
    public static class Config {
        public int flyingMinHeight = 2;
        public int flyingMaxHeight = 15;
        public String[] diggingBlocks = {"minecraft:dirt", "minecraft:stone"};
        public int diggingBlockBreakTime = 20; // 20 ticks = 1 segundo
        public int curiousFollowDistance = 10;
        public String[] hostilePokemonList = {"pikachu", "charmander"};
    }
}

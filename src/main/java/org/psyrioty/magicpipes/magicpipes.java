package org.psyrioty.magicpipes;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.psyrioty.magicpipes.Database.Requests;
import org.psyrioty.magicpipes.Listeners.PipeBlockEvents;
import org.psyrioty.magicpipes.Listeners.PipeItemEvents;
import org.psyrioty.magicpipes.Listeners.PipeOtherEvents;
import org.psyrioty.magicpipes.Objects.Pipe;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public final class magicpipes extends JavaPlugin {
    static magicpipes plugin;
    List<Pipe> pipes = new ArrayList<>();
    PluginManager pm;

    FileConfiguration config;
    static File configFile;

    @Override
    public void onEnable() {
        plugin = this;
        createDataFile();

        Requests.connect();

        pm = Bukkit.getPluginManager();
        pm.registerEvents(new PipeBlockEvents(), this);
        pm.registerEvents(new PipeItemEvents(), this);
        pm.registerEvents(new PipeOtherEvents(), this);

        Bukkit.getScheduler().runTaskAsynchronously(this, this::setAllPipes);
        try {
            createCustomRecipe();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Requests.disconnect();
    }

    private void setAllPipes(){
        pipes = Requests.getAllPipes();
    }

    public void createDataFile() {
        configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            getDataFolder().mkdirs();

            try {
                saveResource("config.yml", false);
            } catch (IllegalArgumentException e) {
                try {
                    configFile.createNewFile();
                    getLogger().info("Создан новый config.yml.");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    private void createCustomRecipe() throws Exception {
        // Создаём кастомный предмет

        String value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRhMmViMmIxMDUzOWNlYzIwMmUxODY3ZjI2MWE2ODBkODcyOWFlNGFiYmE5NTdjZGZhMjQ1MzgxNTJmZGM3MSJ9fX0=";

        ItemStack head = createCustomHead(value, "pipe_funnel", "Собирательная труба");

        // Уникальный ключ рецепта
        NamespacedKey key = new NamespacedKey(this, "pipe_funnel");

        // Рецепт (пример: в форме "X X X", где X - алмаз)
        ShapedRecipe recipe = new ShapedRecipe(key, head);
        recipe.shape(" G ", " D ", " G ");
        recipe.setIngredient('D', Material.NETHERITE_BLOCK);
        recipe.setIngredient('G', Material.GLASS);

        // Регистрируем рецепт
        Bukkit.addRecipe(recipe);

        // Создаём кастомный предмет

        value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY3NTVhYzZjMDc4ZDAwODJmNjg3MTUzOWY4YzhlMDM3M2IwMDgyMTRjYWNkYjRjZGZmZmM4ODY2ZGYxZDJlNiJ9fX0=";

        head = createCustomHead(value, "pipe", "Труба");

        // Уникальный ключ рецепта
        key = new NamespacedKey(this, "pipe");

        // Рецепт (пример: в форме "X X X", где X - алмаз)
        recipe = new ShapedRecipe(key, head);
        recipe.shape(" G ", " D ", " G ");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('G', Material.GLASS);

        // Регистрируем рецепт
        Bukkit.addRecipe(recipe);


        // Создаём кастомный предмет

        value = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTBlZjQzOWIxOTdjZmQ4ZThkMWJhZDNhODIwY2Q5NmViZjI0YTQ0MjY2YmUwOTJjOTkzMWE0NGZiMzE1OWQzOCJ9fX0=";

        head = createCustomHead(value, "pipe_receiver", "Труба приёмник");

        // Уникальный ключ рецепта
        key = new NamespacedKey(this, "pipe_receiver");

        // Рецепт (пример: в форме "X X X", где X - алмаз)
        recipe = new ShapedRecipe(key, head);
        recipe.shape(" G ", " D ", " G ");
        recipe.setIngredient('D', Material.EMERALD_BLOCK);
        recipe.setIngredient('G', Material.GLASS);

        // Регистрируем рецепт
        Bukkit.addRecipe(recipe);
    }

    public ItemStack createCustomHead(String base64, String id, String name) throws Exception {
        // Декодируем Base64 и получаем URL
        String json = new String(Base64.getDecoder().decode(base64));
        int start = json.indexOf("http");
        int end = json.indexOf("\"", start);
        URL skinUrl = new URL(json.substring(start, end));
        // Создаём профиль и устанавливаем текстуру
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.fromString("604a0957-5df9-4a0f-9c89-33c41bb90342"));
        PlayerTextures textures = profile.getTextures();
        textures.setSkin(skinUrl);
        profile.setTextures(textures);
        // Создаём ItemStack головы и устанавливаем профиль
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(profile);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(
                new NamespacedKey(this, "id"),
                PersistentDataType.STRING,
                id
        );

        meta.setDisplayName(name);

        head.setItemMeta(meta);
        return head;
    }

    public static magicpipes getPlugin() {
        return plugin;
    }

    public List<Pipe> getPipes() {
        return pipes;
    }

    public PluginManager getPm() {
        return pm;
    }

    public Pipe findPipeForXYZWorld(int x, int y, int z, World world){
        if(!pipes.isEmpty()){
            for(Pipe pipe: pipes){
                if(
                        x == pipe.getX()
                        && y == pipe.getY()
                        && z == pipe.getZ()
                        && world == pipe.getWorld()
                ){
                    return pipe;
                }
            }
        }
        return null;
    }

    public Inventory getPipeInventoryForInventory(Inventory inventory){
        for(Pipe pipe: pipes){
            if(pipe.getInventory() == inventory){
                return pipe.getInventory();
            }
        }
        return null;
    }
}

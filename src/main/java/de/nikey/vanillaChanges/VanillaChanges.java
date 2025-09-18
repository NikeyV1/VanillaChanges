package de.nikey.vanillaChanges;

import de.nikey.vanillaChanges.Commands.VanillaChangesCommand;
import de.nikey.vanillaChanges.Data.MaceControlData;
import de.nikey.vanillaChanges.Listener.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class VanillaChanges extends JavaPlugin {
    private static VanillaChanges plugin;

    private final Map<String, NamespacedKey> recipeKeys = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new CooldownListener(),this);
        manager.registerEvents(new EndermiteListener(),this);
        manager.registerEvents(new ItemDestroyListener(), this);
        manager.registerEvents(new AnvilListener(), this);
        manager.registerEvents(new VillagerTradesListener(), this);
        manager.registerEvents(new FarmlandFeatherFallingListener(),this);
        manager.registerEvents(new MaceControlListener(), this);
        manager.registerEvents(new HeadDropListener(), this);
        manager.registerEvents(new DamageReductionsListener(),this);

        getCommand("vanillachanges").setExecutor(new VanillaChangesCommand());

        saveDefaultConfig();
        MaceControlData.setupDataFile();
        MaceControlData.loadConfigValues();

        loadRecipes();
        loadMultipliers();
    }


    @Override
    public void onDisable() {
        removeRecipes();
        MaceControlData.saveDataFile();
    }

    public static VanillaChanges getPlugin() {
        return plugin;
    }


    public void loadMultipliers() {
        FileConfiguration cfg = getConfig();
        ConfigurationSection section = cfg.getConfigurationSection("multipliers");
        DamageReductionsListener.multipliers.clear();
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.valueOf(key.toUpperCase());
                double mult = section.getDouble(key, 1.0);
                if (mult < 0) mult = 0;
                DamageReductionsListener.multipliers.put(cause, mult);
                getLogger().info("Loaded multiplier for " + cause + " -> " + mult);
            } catch (IllegalArgumentException ex) {
                getLogger().warning("Unknown damage cause in config: " + key);
            }
        }
    }

    private void removeRecipes() {
        for (NamespacedKey key : recipeKeys.values()) {
            Bukkit.removeRecipe(key);
        }
        recipeKeys.clear();
    }

    public void loadRecipes() {
        removeRecipes();

        FileConfiguration config = getConfig();
        ConfigurationSection recipes = config.getConfigurationSection("recipes");

        if (recipes == null) {
            getLogger().warning("Keine Rezepte in der Config gefunden!");
            return;
        }

        for (String id : recipes.getKeys(false)) {
            ConfigurationSection section = recipes.getConfigurationSection(id);
            if (section == null) continue;

            boolean enabled = section.getBoolean("enabled", true);
            if (!enabled) {
                getLogger().info("Rezept '" + id + "' ist deaktiviert.");
                continue;
            }

            String materialName = section.getString("item");
            if (materialName == null) {
                getLogger().warning("Item name in " + id + " is null!");
                continue;
            }
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                getLogger().warning("Ungültiges Material für Rezept '" + id + "': " + materialName);
                continue;
            }

            if (section.getBoolean("replace-vanilla", false)) {
                removeVanillaRecipes(material);
                getLogger().info("Alle Vanilla-Rezepte für " + material + " wurden entfernt.");
            }

            int amount = section.getInt("amount", 1);
            ItemStack result = new ItemStack(material, amount);

            NamespacedKey key = new NamespacedKey(this, id.toLowerCase());
            ShapedRecipe recipe = new ShapedRecipe(key, result);

            // Shape
            recipe.shape(section.getStringList("shape").toArray(new String[0]));

            // Zutaten
            ConfigurationSection ing = section.getConfigurationSection("ingredients");
            if (ing != null) {
                for (String symbol : ing.getKeys(false)) {
                    String matName = ing.getString(symbol);
                    Material mat = Material.matchMaterial(matName);
                    if (mat == null) {
                        getLogger().warning("Ungültiges Material in '" + id + "': " + matName);
                        continue;
                    }
                    recipe.setIngredient(symbol.charAt(0), mat);
                }
            }

            Bukkit.addRecipe(recipe);
            recipeKeys.put(id, key);
            getLogger().info("Rezept für '" + id + "' geladen!");
        }
    }

    private void removeVanillaRecipes(Material material) {
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            ItemStack result = recipe.getResult();
            if (result.getType() == material) {
                it.remove();
            }
        }
    }
}

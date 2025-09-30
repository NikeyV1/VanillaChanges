package de.nikey.vanillaChanges;

import de.nikey.vanillaChanges.Commands.VanillaChangesCommand;
import de.nikey.vanillaChanges.Data.MaceControlData;
import de.nikey.vanillaChanges.Listener.*;
import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;

public final class VanillaChanges extends JavaPlugin {
    private static VanillaChanges plugin;
    private VillagerTradesListener villagerTradeMultiplier;

    private final Map<String, NamespacedKey> recipeKeys = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;

        villagerTradeMultiplier = new VillagerTradesListener();

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new CooldownListener(),this);
        manager.registerEvents(new EndermiteListener(),this);
        manager.registerEvents(new ItemDestroyListener(), this);
        manager.registerEvents(new AnvilListener(), this);
        manager.registerEvents(villagerTradeMultiplier, this);
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
        reloadCustomPotions();
        villagerTradeMultiplier.applyMultiplierToAllVillagers(getConfig().getDouble("villager.trade-multiplier", 1.0));
    }

    public void reloadVanillaChangesVillagerConfig() {
        double newMultiplier = getConfig().getDouble("villager.trade-multiplier", 1.0);
        villagerTradeMultiplier.applyMultiplierToAllVillagers(newMultiplier);
    }

    @Override
    public void onDisable() {
        removeRecipes();
        MaceControlData.saveDataFile();
    }

    public static VanillaChanges getPlugin() {
        return plugin;
    }

    public void reloadCustomPotions() {
        PotionBrewer brewer = Bukkit.getPotionBrewer();
        brewer.resetPotionMixes();
        ConfigurationSection section = getConfig().getConfigurationSection("potions");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            if (!section.getBoolean(id+".enabled",false))continue;
            String key = id.toLowerCase(Locale.ROOT);

            int amplifier = section.getInt(id + ".amplifier", 1);
            int durationSeconds = section.getInt(id + ".duration", 120);
            int durationTicks = durationSeconds * 20;
            Color color = parseColor(section.getString(id + ".color", "255,255,255"));

            PotionEffectType effectType;
            PotionType basePotionType;
            String displayName;
            switch (key) {
                case "strength":
                    effectType = PotionEffectType.STRENGTH;
                    basePotionType = PotionType.STRENGTH;
                    displayName = "Potion of Strength";
                    break;
                case "speed":
                    effectType = PotionEffectType.SPEED;
                    basePotionType = PotionType.SWIFTNESS;
                    displayName = "Potion of Swiftness";
                    break;
                default:
                    getLogger().warning("Unknown potion key in config: " + id + " — skipping.");
                    continue;
            }

            ItemStack customNormal = createCustomPotion(Material.POTION, displayName, effectType, amplifier, durationTicks, color);
            ItemStack customSplash = createCustomPotion(Material.SPLASH_POTION, "Splash " + displayName, effectType, amplifier, durationTicks, color);

            ItemStack base = new ItemStack(Material.POTION);
            PotionMeta baseMeta = (PotionMeta) base.getItemMeta();
            baseMeta.setBasePotionType(basePotionType);
            base.setItemMeta(baseMeta);
            RecipeChoice input = new RecipeChoice.ExactChoice(base);

            ItemStack baseSplash = new ItemStack(Material.SPLASH_POTION);
            PotionMeta baseSplashMeta = (PotionMeta) baseSplash.getItemMeta();
            baseSplashMeta.setBasePotionType(basePotionType);
            baseSplash.setItemMeta(baseSplashMeta);
            RecipeChoice inputSplash = new RecipeChoice.ExactChoice(baseSplash);

            RecipeChoice glowstone = new RecipeChoice.MaterialChoice(Material.GLOWSTONE_DUST);
            RecipeChoice gunpowder = new RecipeChoice.MaterialChoice(Material.GUNPOWDER);

            NamespacedKey normalKey = new NamespacedKey(this, "custom_" + key + "_normal");
            NamespacedKey splashKey = new NamespacedKey(this, "custom_" + key + "_splash");
            NamespacedKey defSplashKey = new NamespacedKey(this, "custom_" + key + "_def_splash");

            brewer.addPotionMix(new PotionMix(normalKey, customNormal, input, glowstone));
            brewer.addPotionMix(new PotionMix(splashKey, customSplash, inputSplash, glowstone));
            brewer.addPotionMix(new PotionMix(defSplashKey, customSplash, new RecipeChoice.ExactChoice(customNormal), gunpowder));

            getLogger().info("Registered custom potion: " + displayName + " (" + durationSeconds + "s, amp=" + amplifier + ")");
        }
    }

    private ItemStack createCustomPotion(Material material, String name, PotionEffectType type, int amp, int durationTicks, Color color) {
        ItemStack potion = new ItemStack(material);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setColor(color);
        meta.customName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        meta.addCustomEffect(new PotionEffect(type, durationTicks, amp), true);
        potion.setItemMeta(meta);
        return potion;
    }

    private Color parseColor(String rgb) {
        try {
            String[] p = rgb.split(",");
            int r = Integer.parseInt(p[0].trim());
            int g = Integer.parseInt(p[1].trim());
            int b = Integer.parseInt(p[2].trim());
            return Color.fromRGB(r, g, b);
        } catch (Exception e) {
            return Color.fromRGB(255, 255, 255);
        }
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
                if (mult == 1.0)continue;
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

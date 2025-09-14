package de.nikey.vanillaChanges.Data;

import de.nikey.vanillaChanges.VanillaChanges;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MaceControlData {
    public static double damageMultiplier;
    public static int maxCraftableMaces;
    public static final Map<Enchantment, Integer> enchantLimits = new HashMap<>();

    public static File dataFile;
    public static FileConfiguration dataConfig;
    public static String craftedBroadcast;

    public static void loadConfigValues() {
        FileConfiguration cfg = VanillaChanges.getPlugin().getConfig();
        damageMultiplier = cfg.getDouble("mace-control.damage.multiplier", 1.0);
        maxCraftableMaces = cfg.getInt("mace-control.crafting.max-total", 1);

        enchantLimits.clear();
        if (cfg.isConfigurationSection("mace-control.enchant-limits")) {
            for (String key : cfg.getConfigurationSection("mace-control.enchant-limits").getKeys(false)) {
                Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(key));
                if (ench != null) {
                    enchantLimits.put(ench, cfg.getInt("mace-control.enchant-limits." + key));
                }
            }
        }
        craftedBroadcast = cfg.getString("mace-control.crafting.mace-crafted-broadcast", "");
    }

    public static void setupDataFile() {
        dataFile = new File(VanillaChanges.getPlugin().getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (!dataConfig.isSet("crafted-total")) {
            dataConfig.set("crafted-total", 0);
            saveDataFile();
        }
    }

    public static void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getCraftedMaces() {
        return dataConfig.getInt("crafted-total", 0);
    }

    public static void setCraftedMaces(int amount) {
        dataConfig.set("crafted-total", amount);
        saveDataFile();
    }
}

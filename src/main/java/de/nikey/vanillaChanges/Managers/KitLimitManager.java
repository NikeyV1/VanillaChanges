package de.nikey.vanillaChanges.Managers;

import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;

public class KitLimitManager {
    private final Map<Material, Integer> limits = new EnumMap<>(Material.class);

    public KitLimitManager() {
        reload();
    }

    public void reload() {
        limits.clear();

        ConfigurationSection section = VanillaChanges.getPlugin().getConfig().getConfigurationSection("kit-limiter.limits");

        if (section == null) return;

        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material != null) {
                limits.put(material, section.getInt(key));
            }
        }
    }

    public boolean isEnabled() {
        return VanillaChanges.getPlugin().getConfig().getBoolean("kit-limiter.enabled");
    }

    public boolean hasLimit(Material material) {
        return limits.containsKey(material);
    }

    public int getLimit(Material material) {
        return limits.getOrDefault(material, Integer.MAX_VALUE);
    }

    public Map<Material, Integer> getLimits() {
        return limits;
    }

    public String getMethod() {
        return VanillaChanges.getPlugin().getConfig().getString("kit-limiter.check-method", "onHit");
    }
}

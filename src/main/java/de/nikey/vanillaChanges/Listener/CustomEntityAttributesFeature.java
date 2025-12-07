package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class CustomEntityAttributesFeature implements Listener {
    private static final Map<EntityType, Map<Attribute, Double>> entityAttributes = new EnumMap<>(EntityType.class);
    private static boolean enabled = false;

    public CustomEntityAttributesFeature() {
        loadConfig();
    }

    public static void loadConfig() {
        VanillaChanges.getPlugin().reloadConfig();
        entityAttributes.clear();

        ConfigurationSection root = VanillaChanges.getPlugin().getConfig().getConfigurationSection("entity-attributes");
        if (root == null) {
            VanillaChanges.getPlugin().getLogger().warning("Keine Sektion 'entity-attributes' in der Config gefunden!");
            return;
        }

        enabled = root.getBoolean("enabled", true);
        if (!enabled) return;

        for (String key : root.getKeys(false)) {
            if (key.equalsIgnoreCase("enabled")) continue;

            EntityType type;
            try {
                type = EntityType.valueOf(key.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                VanillaChanges.getPlugin().getLogger().warning("Ungültiger EntityType in Config: " + key);
                continue;
            }

            ConfigurationSection attrSec = root.getConfigurationSection(key);
            if (attrSec == null) continue;

            Map<Attribute, Double> attrs = new HashMap<>();
            for (String attrKey : attrSec.getKeys(false)) {
                Attribute attribute = getAttributeByName(attrKey);
                if (attribute == null) {
                    VanillaChanges.getPlugin().getLogger().warning("Ungültiges Attribut '" + attrKey + "' für " + key);
                    continue;
                }
                double value = attrSec.getDouble(attrKey);
                attrs.put(attribute, value);
            }

            entityAttributes.put(type, attrs);
        }

        VanillaChanges.getPlugin().getLogger().info("CustomEntityAttributes loaded: " + entityAttributes.size() + " EntityTypes");
    }

    private static Attribute getAttributeByName(String name) {
        name = name.toLowerCase(Locale.ROOT);
        if (!name.contains(":")) {
            name = "minecraft:" + name;
        }

        NamespacedKey key = NamespacedKey.fromString(name);
        if (key == null) return null;

        return Registry.ATTRIBUTE.get(key);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!enabled) return;

        Entity e = event.getRightClicked();
        if (!(e instanceof LivingEntity entity))return;
        EntityType type = entity.getType();

        Map<Attribute, Double> attrs = entityAttributes.get(type);
        if (attrs == null) return;

        for (Map.Entry<Attribute, Double> entry : attrs.entrySet()) {
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null) {
                instance.setBaseValue(entry.getValue());
                if (entry.getKey() == Attribute.MAX_HEALTH) {
                    entity.setHealth(entry.getValue());
                }
            }
        }
    }


    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!enabled) return;

        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();

        Map<Attribute, Double> attrs = entityAttributes.get(type);
        if (attrs == null) return;

        for (Map.Entry<Attribute, Double> entry : attrs.entrySet()) {
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null) {
                instance.setBaseValue(entry.getValue());
                if (entry.getKey() == Attribute.MAX_HEALTH) {
                    entity.setHealth(entry.getValue());
                }
            }
        }
    }
}

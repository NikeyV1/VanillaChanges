package de.nikey.vanillaChanges.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.EnumMap;
import java.util.Map;

public class DamageReductionsListener implements Listener {
    public static Map<EntityDamageEvent.DamageCause, Double> multipliers = new EnumMap<>(EntityDamageEvent.DamageCause.class);

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        Double multiplier = multipliers.get(cause);
        if (multiplier == null) return;

        double original = event.getDamage();
        double newDamage = original * multiplier;
        if (newDamage < 0) newDamage = 0;

        event.setDamage(newDamage);
    }
}

package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ItemDestroyListener implements Listener {

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item)) return;

        switch (event.getCause()) {
            case LAVA -> {
                if (!VanillaChanges.getPlugin().getConfig().getBoolean("item-damage.lava", true)) {
                    event.setCancelled(true);
                }
            }
            case CONTACT -> {
                if (!VanillaChanges.getPlugin().getConfig().getBoolean("item-damage.cactus", true)) {
                    event.setCancelled(true);
                }
            }
            case LIGHTNING -> {
                if (!VanillaChanges.getPlugin().getConfig().getBoolean("item-damage.lightning", true)) {
                    event.setCancelled(true);
                }
            }
            case FIRE_TICK, FIRE -> {
                if (!VanillaChanges.getPlugin().getConfig().getBoolean("item-damage.fire", true)) {
                    event.setCancelled(true);
                }
            }
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> {
                if (!VanillaChanges.getPlugin().getConfig().getBoolean("item-damage.explosion", true)) {
                    event.setCancelled(true);
                }
            }
            default -> {}
        }
    }
}

package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.ThreadLocalRandom;

public class EndermiteListener implements Listener {
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.ENDERMITE) return;
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("endermite.enabled")) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.ENDER_PEARL) return;

        event.setCancelled(true);
    }


    @EventHandler
    public void onPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("endermite.enabled")) return;

        double chance = VanillaChanges.getPlugin().getConfig().getDouble("endermite.spawn-chance");
        if (ThreadLocalRandom.current().nextDouble() <= chance) {
            event.getTo().getWorld().spawn(event.getTo(), Endermite.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
    }
}

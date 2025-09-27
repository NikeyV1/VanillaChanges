package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.UUID;

public class CooldownListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerShieldDisable(PlayerShieldDisableEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("cooldowns.shield.enabled",false)) return;
        int cooldown = VanillaChanges.getPlugin().getConfig().getInt("cooldowns.shield.ticks");
        Bukkit.broadcast(Component.text(cooldown));

        event.setCooldown(cooldown);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemCooldown(PlayerItemCooldownEvent event) {
        Material item = event.getType();

        if (item == Material.ENDER_PEARL) {
            if (!VanillaChanges.getPlugin().getConfig().getBoolean("cooldowns.enderpearl.enabled",false)) return;
            int cooldown = VanillaChanges.getPlugin().getConfig().getInt("cooldowns.enderpearl.ticks");
            event.setCooldown(cooldown);
        }else if (item == Material.WIND_CHARGE) {
            if (!VanillaChanges.getPlugin().getConfig().getBoolean("cooldowns.windcharge.enabled",false)) return;
            int cooldown = VanillaChanges.getPlugin().getConfig().getInt("cooldowns.windcharge.ticks");
            event.setCooldown(cooldown);
        }else if (item == Material.CHORUS_FRUIT) {
            if (!VanillaChanges.getPlugin().getConfig().getBoolean("cooldowns.corusfruit.enabled",false)) return;
            int cooldown = VanillaChanges.getPlugin().getConfig().getInt("cooldowns.corusfruit.ticks");
            event.setCooldown(cooldown);
        }
    }

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    @EventHandler
    public void onCobwebPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.COBWEB) return;

        UUID uuid = event.getPlayer().getUniqueId();
        long now = System.currentTimeMillis();

        if (!VanillaChanges.getPlugin().getConfig().getBoolean("cooldowns.cobweb.enabled",false)) return;
        int cooldown = VanillaChanges.getPlugin().getConfig().getInt("cooldowns.cobweb.ticks");

        if (cooldowns.containsKey(uuid)) {
            long last = cooldowns.get(uuid);
            long diff = now - last;

            if (diff < cooldown) {
                event.setCancelled(true);
                return;
            }
        }

        event.getPlayer().setCooldown(Material.COBWEB,cooldown);
        cooldowns.put(uuid, now);
    }
}
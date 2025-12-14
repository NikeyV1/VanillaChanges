package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;

public class InvisibleKillsListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("invisible-kills.enabled")) return;

        Player victim = event.getEntity();
        Player killer = resolveKiller(victim);

        if (killer == null) return;
        if (!killer.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        Component message = event.deathMessage();
        if (message == null) return;

        event.deathMessage(
                message.replaceText(config -> config
                        .matchLiteral(killer.getName())
                        .replacement(
                                Component.text("hialGw")
                                        .decorate(TextDecoration.OBFUSCATED)
                        )
                )
        );
    }

    /* ===============================
       KILLER RESOLUTION
       =============================== */

    private Player resolveKiller(Player victim) {
        Entity damager = victim.getLastDamageCause() != null
                ? victim.getLastDamageCause().getEntity()
                : null;

        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                return player;
            }
        }

        return null;
    }
}

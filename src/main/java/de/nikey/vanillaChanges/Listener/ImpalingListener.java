package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ImpalingListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("impaling-rework.enabled",false))return;
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        int impalingLevel = 0;

        // Fall 1: Spieler schlägt direkt mit Trident (Nahkampf)
        if (damager instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.TRIDENT && item.containsEnchantment(Enchantment.IMPALING)) {
                impalingLevel = item.getEnchantmentLevel(Enchantment.IMPALING);
            }
        }

        // Fall 2: Trident wird geworfen
        else if (damager instanceof Trident trident) {
            ItemStack item = trident.getItemStack();
            if (item.getType() == Material.TRIDENT && item.containsEnchantment(Enchantment.IMPALING)) {
                impalingLevel = item.getEnchantmentLevel(Enchantment.IMPALING);
            }
        }
        if (impalingLevel <= 0) return;

        if (VanillaChanges.getPlugin().getConfig().getString("impaling-rework.mode").equalsIgnoreCase("bedrock")) {
            if (Tag.ENTITY_TYPES_AQUATIC.isTagged(target.getType())) {
                double vanillaBonus = 2.5 * impalingLevel;
                event.setDamage(event.getDamage() - vanillaBonus);
            }
        }

        if (target.isInWater() || target.isInRain()) {
            double bonus = 2.5 * impalingLevel;
            event.setDamage(event.getDamage() + bonus);
        }
    }
}
package de.nikey.vanillaChanges.Listener;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class TridentListener implements Listener {
    private final NamespacedKey offhandKey = new NamespacedKey(VanillaChanges.getPlugin(), "thrown_from_offhand");

    @EventHandler
    public void onTridentThrow(ProjectileLaunchEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("trident.offhand-return",false)) return;
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean isOffhandThrow = mainHand.getType() != Material.TRIDENT && offHand.getType() == Material.TRIDENT;

        if (isOffhandThrow) {
            trident.getPersistentDataContainer().set(offhandKey, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof Trident trident)) return;
        if (VanillaChanges.getPlugin().getConfig().getBoolean("trident.void-saving.enabled",false)) {
            if (trident.getItemStack().containsEnchantment(Enchantment.LOYALTY)) {
                startSingleTridentVoidTask(trident, event.getPlayer());
            }
        }
    }


    @EventHandler
    public void onTridentPickup(PlayerPickupArrowEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("trident.offhand-return",false)) return;
        if (!(event.getArrow() instanceof Trident trident)) return;

        if (trident.getPersistentDataContainer().has(offhandKey, PersistentDataType.BYTE)) {
            Player player = event.getPlayer();
            PlayerInventory inv = player.getInventory();

            if (inv.getItemInOffHand().getType() == Material.AIR) {
                ItemStack item = event.getItem().getItemStack();

                inv.setItemInOffHand(item);

                event.setCancelled(true);
                trident.remove();

                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.8f, 1f);
            }
        }
    }


    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("trident.disable-loyalty-portals")) return;

        Entity entity = event.getEntity();
        if (entity instanceof Trident trident) {
            ItemStack item = trident.getItemStack();

            if (item.containsEnchantment(Enchantment.LOYALTY)) {
                event.setCancelled(true);
            }
        }
    }

    private void startSingleTridentVoidTask(Trident trident, Player shooter) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!trident.isValid() || trident.isDead() || trident.isOnGround()) {
                    this.cancel();
                    return;
                }

                double limit = trident.getWorld().getMinHeight() - VanillaChanges.getPlugin().getConfig().getInt("trident.void-saving.min-y");

                if (trident.getLocation().getY() <= limit) {
                    if (shooter.isOnline()) {
                        trident.setHasDealtDamage(true);
                        this.cancel();
                    } else {
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(VanillaChanges.getPlugin(), 5, 1);
    }
}

package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FarmlandFeatherFallingListener implements Listener {
    private boolean hasFeatherFalling(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || boots.getItemMeta() == null) return false;
        return boots.getItemMeta().hasEnchant(Enchantment.FEATHER_FALLING);
    }

    private boolean shouldCancel(Player player) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("farmland.enabled", true)) return false;

        String mode = VanillaChanges.getPlugin().getConfig().getString("farmland.mode", "FEATHER_FALLING").toUpperCase();
        return switch (mode) {
            case "ALWAYS" -> true;
            case "FEATHER_FALLING" -> hasFeatherFalling(player);
            default -> false;
        };
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFarmlandTrample(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getBlock().getType() != Material.FARMLAND) return;

        if (shouldCancel(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPhysicalInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.FARMLAND) return;

        Player player = event.getPlayer();
        if (shouldCancel(player)) {
            event.setCancelled(true);
        }
    }
}

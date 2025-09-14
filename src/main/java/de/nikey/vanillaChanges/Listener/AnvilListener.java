package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class AnvilListener implements Listener {
    @EventHandler
    public void onAnvilUse(PrepareAnvilEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("unbreakable-anvils", false)) return;

        if (event.getInventory().getLocation() != null ) {
            Material type = event.getInventory().getLocation().getBlock().getType();
            if (type == Material.ANVIL || type == Material.CHIPPED_ANVIL || type == Material.DAMAGED_ANVIL){
                event.getInventory().getLocation().getBlock().setType(Material.ANVIL, false);
            }
        }
    }
}

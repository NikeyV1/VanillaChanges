package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VillagerTradesListener implements Listener {
    @EventHandler
    public void onPlayerPurchase(PlayerPurchaseEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("villager.infinite", false)) return;
        event.getTrade().setMaxUses(120000);
        event.getPlayer().updateInventory();
    }
}
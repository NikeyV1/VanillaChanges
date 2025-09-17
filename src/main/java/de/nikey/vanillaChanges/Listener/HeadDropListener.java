package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeadDropListener implements Listener {
    private final Random random = new Random();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("head-drop.enabled", true)) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (VanillaChanges.getPlugin().getConfig().getBoolean("head-drop.only-pvp", false) && killer == null) {
            return;
        }

        if (VanillaChanges.getPlugin().getConfig().getBoolean("head-drop.only-pvp", false) && killer == victim) {
            return;
        }

        double chance = VanillaChanges.getPlugin().getConfig().getDouble("head-drop.drop-chance", 100.0);
        if (random.nextDouble() * 100 > chance) return;

        int deaths = victim.getStatistic(org.bukkit.Statistic.DEATHS);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(victim);

            String customName = VanillaChanges.getPlugin().getConfig().getString("head-drop.custom-head-name", "{player}'s Head")
                    .replace("{player}", victim.getName());
            skullMeta.displayName(Component.text(customName));

            List<Component> lore = new ArrayList<>();

            if (killer != null) {
                lore.add(Component.text("Killed by " + killer.getName())
                        .color(TextColor.color(0xAAAAAA)));
            }

            if (VanillaChanges.getPlugin().getConfig().getBoolean("head-drop.show-deaths", true)) {
                lore.add(Component.text("Total deaths: " + deaths)
                        .color(TextColor.color(0xAAAAAA)));
            }

            skullMeta.lore(lore);
            skull.setItemMeta(skullMeta);
        }

        victim.getWorld().dropItemNaturally(victim.getLocation(), skull);
    }
}

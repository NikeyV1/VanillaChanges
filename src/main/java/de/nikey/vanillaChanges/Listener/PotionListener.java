package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PotionListener implements Listener {
    @EventHandler
    public void onPotionBrew(BrewEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("potion-durations.enabled", false)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < event.getContents().getSize(); i++) {
                    ItemStack item = event.getContents().getItem(i);
                    if (item == null || !(item.getItemMeta() instanceof PotionMeta meta)) continue;

                    PotionType type = meta.getBasePotionType();
                    String configKey = type.name().toLowerCase(); // z. B. "strong_strength"

                    if (!VanillaChanges.getPlugin().getConfig().isInt("potion-durations.overwrite." + configKey))
                        continue;

                    int ticks = VanillaChanges.getPlugin().getConfig().getInt("potion-durations.overwrite." + configKey);
                    List<PotionEffect> effects = type.getPotionEffects();

                    if (effects.isEmpty()) continue;

                    meta.clearCustomEffects();
                    for (PotionEffect original : effects) {
                        PotionEffect newEffect = new PotionEffect(
                                original.getType(),
                                ticks,
                                original.getAmplifier()
                        );
                        meta.addCustomEffect(newEffect, true);
                    }

                    item.setItemMeta(meta);
                }
            }
        }.runTask(VanillaChanges.getPlugin());
    }
}
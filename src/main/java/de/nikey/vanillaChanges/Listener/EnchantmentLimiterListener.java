package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentLimiterListener implements Listener {
    private static final Map<Enchantment, Integer> limits = new HashMap<>();

    public EnchantmentLimiterListener() {
        loadLimits();
    }

    public static void loadLimits() {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("enchantment-limiter.enabled")) return;

        VanillaChanges.getPlugin().getConfig()
                .getConfigurationSection("enchantment-limiter.limits")
                .getKeys(false)
                .forEach(key -> {
                    Enchantment enchantment = Enchantment.getByName(key.toUpperCase());
                    if (enchantment != null) {
                        int maxLevel = VanillaChanges.getPlugin().getConfig()
                                .getInt("enchantment-limiter.limits." + key);
                        limits.put(enchantment, maxLevel);
                    }
                });
    }

    /* ===============================
       ENCHANTING TABLE
       =============================== */
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        event.getEnchantsToAdd().forEach((ench, level) -> {
            if (!limits.containsKey(ench)) return;

            int max = limits.get(ench);
            if (level > max) {
                if (max == 0) {
                    event.getEnchantsToAdd().remove(ench);
                }
                event.getEnchantsToAdd().put(ench, max);
                sendLimitMessage(event.getEnchanter(), ench, max);
            }
        });
    }

    /* ===============================
       ANVIL
       =============================== */
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result == null) return;

        ItemMeta meta = result.getItemMeta();
        if (meta == null || meta.getEnchants().isEmpty()) return;

        boolean modified = false;

        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();

            if (!limits.containsKey(ench)) continue;

            int max = limits.get(ench);
            if (level > max) {
                meta.removeEnchant(ench);
                if (max != 0){
                    meta.addEnchant(ench, max, true);
                }
                modified = true;
            }
        }

        if (modified) {
            result.setItemMeta(meta);
            event.setResult(result);
        }
    }

    /* ===============================
       MANUAL EQUIP / GIVE SAFETY NET
       =============================== */
    @EventHandler
    public void onItemHold(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        boolean modified = false;

        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();

            if (!limits.containsKey(ench)) continue;

            int max = limits.get(ench);
            if (level > max) {
                meta.removeEnchant(ench);
                if (max != 0){
                    meta.addEnchant(ench, max, true);
                }
                modified = true;
                sendLimitMessage(player, ench, max);
            }
        }

        if (modified) {
            item.setItemMeta(meta);
        }
    }

    private void sendLimitMessage(Player player, Enchantment ench, int max) {
        Component message = Component.text("The enchantment ")
                .append(Component.text(ench.getKey().getKey().toUpperCase())
                        .color(TextColor.color(255, 223, 51))
                        .style(Style.style(TextColor.color(255, 223, 51))))
                .append(Component.text(" can have a maximum level of "))
                .append(Component.text(String.valueOf(max))
                        .color(TextColor.color(255, 51, 51))
                        .style(Style.style(TextColor.color(255, 51, 51))))
                .append(Component.text("."));

        player.sendMessage(message);
    }
}

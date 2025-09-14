package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.Data.MaceControlData;
import de.nikey.vanillaChanges.VanillaChanges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MaceControlListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("mace-control.enabled", false)) return;
        if (!(e.getDamager() instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.MACE) return;

        double dmg = e.getDamage();
        dmg *= MaceControlData.damageMultiplier;
        e.setDamage(dmg);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("mace-control.enabled", false) ||
                !VanillaChanges.getPlugin().getConfig().getBoolean("mace-control.crafting.enabled", false)) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getRecipe().getResult().getType() != Material.MACE) return;
        if (player.hasPermission("vanillachanges.macecontrol.bypass.craftinglimit")) return;

        int crafted = MaceControlData.getCraftedMaces();
        int max = MaceControlData.maxCraftableMaces;

        int amountCrafted = 0;

        if (e.isShiftClick()) {
            int maxPossible = Integer.MAX_VALUE;
            for (ItemStack item : e.getInventory().getMatrix()) {
                if (item != null && item.getType() != Material.AIR) {
                    maxPossible = Math.min(maxPossible, item.getAmount());
                }
            }
            amountCrafted = maxPossible * e.getRecipe().getResult().getAmount();
        } else {
            amountCrafted = e.getRecipe().getResult().getAmount();
        }

        if (crafted + amountCrafted > max) {
            player.sendMessage(Component.text("The global mace crafting limit has been reached!")
                    .color(NamedTextColor.RED));
            e.setCancelled(true);
            return;
        }

        MaceControlData.setCraftedMaces(crafted + amountCrafted);

        if (MaceControlData.craftedBroadcast != null && !MaceControlData.craftedBroadcast.isEmpty()) {
            Component msg = MiniMessage.miniMessage().deserialize(MaceControlData.craftedBroadcast.replace("<player>", player.getName()));
            Bukkit.broadcast(msg);
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("mace-control.enabled", false)) return;
        if (e.getEnchanter().hasPermission("vanillachanges.macecontrol.bypass.enchantlimit")) return;
        if (e.getItem().getType() != Material.MACE) return;
        for (Map.Entry<Enchantment, Integer> entry : MaceControlData.enchantLimits.entrySet()) {
            if (e.getEnchantsToAdd().containsKey(entry.getKey())) {
                int lvl = e.getEnchantsToAdd().get(entry.getKey());
                if (lvl > entry.getValue()) {
                    e.getEnchantsToAdd().put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCrafterCraft(CrafterCraftEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("mace-control.enabled", false) ||
                !VanillaChanges.getPlugin().getConfig().getBoolean("mace-control.crafting.enabled", false)) return;
        if (event.getResult().getType() != Material.MACE) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("mace-control.enabled", false)) return;
        if (e.getView().getPlayer().hasPermission("vanillachanges.macecontrol.bypass.enchantlimit")) return;
        ItemStack result = e.getResult();
        if (result == null) return;
        if (result.getType() != Material.MACE) return;

        Map<Enchantment, Integer> current = new HashMap<>(result.getEnchantments());
        boolean changed = false;

        for (Map.Entry<Enchantment, Integer> en : current.entrySet()) {
            Enchantment ench = en.getKey();
            int lvl = en.getValue();
            Integer max = MaceControlData.enchantLimits.get(ench);
            if (max != null && lvl > max) {
                result.removeEnchantment(ench);
                result.addUnsafeEnchantment(ench, max);
                changed = true;
            }
        }

        if (changed) {
            e.setResult(result);
        }
    }
}
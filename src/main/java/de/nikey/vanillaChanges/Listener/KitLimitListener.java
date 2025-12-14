package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.Managers.KitLimitManager;
import de.nikey.vanillaChanges.VanillaChanges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class KitLimitListener implements Listener {
    private final KitLimitManager manager;

    public KitLimitListener(KitLimitManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!manager.isEnabled()) return;
        if (!manager.getMethod().equalsIgnoreCase("onHit")) return;
        if (!(event.getEntity() instanceof Player player)) return;

        checkPlayer(player);
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        if (!manager.isEnabled()) return;
        if (!manager.getMethod().equalsIgnoreCase("onInventory")) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        checkPlayer(player);
    }

    /* ===============================
       CORE LOGIC
       =============================== */

    private void checkPlayer(Player player) {
        Inventory inv = player.getInventory();

        for (Map.Entry<Material, Integer> entry : manager.getLimits().entrySet()) {
            Material material = entry.getKey();
            int limit = entry.getValue();

            int total = count(inv, material);
            if (total > limit) {
                removeExcess(player, material, total - limit);
            }
        }
    }

    private int count(Inventory inv, Material material) {
        int total = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private void removeExcess(Player player, Material material, int excess) {
        Inventory inv = player.getInventory();

        if (VanillaChanges.getPlugin().getConfig().getBoolean("kit-limiter.drop-items")) {
            for (int i = 0; i < inv.getSize() && excess > 0; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack == null || stack.getType() != material) continue;

                int remove = Math.min(stack.getAmount(), excess);
                stack.setAmount(stack.getAmount() - remove);
                excess -= remove;

                ItemStack drop = new ItemStack(material, remove);
                Item item = player.getWorld().dropItem(player.getEyeLocation(), drop);
                item.setPickupDelay(40);
            }
        }

        player.sendMessage(Component.text("You have exceeded your item limit for ")
                        .append(Component.text(material.name().toLowerCase())
                                .color(NamedTextColor.RED)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(". Please reduce your excess items."))
        );
    }
}

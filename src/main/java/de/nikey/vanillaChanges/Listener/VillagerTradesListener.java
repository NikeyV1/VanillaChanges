package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class VillagerTradesListener implements Listener {
    private final NamespacedKey pdcKey;
    private double currentMultiplier;

    public VillagerTradesListener() {
        this.pdcKey = new NamespacedKey(VanillaChanges.getPlugin(), "villager_trade_multiplier");
    }

    public double getCurrentMultiplier() {
        return currentMultiplier;
    }

    @EventHandler
    public void onPlayerPurchase(PlayerPurchaseEvent event) {
        if (!VanillaChanges.getPlugin().getConfig().getBoolean("villager.infinite", false)) return;
        event.getTrade().setMaxUses(120000);
        event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Villager villager) {
            applyMultiplierToVillager(villager, currentMultiplier);
        }
    }

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent e) {
        if (e.getEntity() instanceof Villager villager) {
            MerchantRecipe recipe = e.getRecipe();
            AbstractVillager av = e.getEntity(); // may be AbstractVillager
            PersistentDataContainer pdc = av.getPersistentDataContainer();

            double stored = 1.0;
            if (pdc.has(pdcKey, PersistentDataType.DOUBLE)) {
                Double val = pdc.get(pdcKey, PersistentDataType.DOUBLE);
                if (val != null) stored = val;
            }

            if (Double.compare(stored, 1.0) == 0) return;

            int defaultMax = recipe.getMaxUses();
            int newMax = Math.max(1, (int) Math.round(defaultMax * stored));

            recipe.setMaxUses(newMax);
            if (recipe.getUses() > newMax) recipe.setUses(newMax);

            e.setRecipe(recipe);
            applyMultiplierToVillager(villager, currentMultiplier);
        }
    }

    @EventHandler
    public void onVillagerReplenish(VillagerReplenishTradeEvent e) {
        if (e.getEntity() instanceof Villager villager) {
            applyMultiplierToVillager(villager, currentMultiplier);
            Bukkit.broadcast(Component.text(e.getRecipe().getMaxUses()));
        }
    }

    public void applyMultiplierToVillager(Villager villager, double newMultiplier) {
        if (villager == null) return;
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        double old = pdc.getOrDefault(pdcKey, PersistentDataType.DOUBLE, 1.0);

        // If nothing changes, do nothing
        if (Double.compare(old, newMultiplier) == 0) {
            pdc.set(pdcKey, PersistentDataType.DOUBLE, newMultiplier);
            Bukkit.broadcastMessage("A");
            return;
        }

        List<MerchantRecipe> newRecipes = new ArrayList<>(villager.getRecipes().size());
        for (MerchantRecipe r : villager.getRecipes()) {
            int currentMax = r.getMaxUses();

            // Calculate original by removing old multiplier (if old==1 -> assume current is original)
            int originalMax;
            if (Double.compare(old, 1.0) == 0) {
                originalMax = currentMax;
            } else {
                originalMax = (int) Math.round((double) currentMax / old);
                if (originalMax < 1) originalMax = 1;
            }

            int newMax = Math.max(1, (int) Math.round(originalMax * newMultiplier));

            // Recreate recipe preserving all important fields except maxUses which we replace
            MerchantRecipe copy = new MerchantRecipe(
                    r.getResult(),
                    r.getUses(),
                    newMax,
                    r.hasExperienceReward(),
                    r.getVillagerExperience(),
                    r.getPriceMultiplier(),
                    r.getDemand(),
                    r.getSpecialPrice()
            );
            copy.setIngredients(new ArrayList<>(r.getIngredients()));
            copy.setUses(r.getUses()); // keep current usage count
            // other fields already set via constructor
            newRecipes.add(copy);
            Bukkit.broadcastMessage(String.valueOf(newMax));
        }

        villager.setRecipes(newRecipes);

        villager.updateDemand();
        // store new multiplier (if newMultiplier == 1.0 it's optional to remove; we keep storing 1.0 for clarity)
        if (Double.compare(newMultiplier, 1.0) == 0) {
            pdc.remove(pdcKey); // if you want to fully revert to "no multiplier" remove key
        } else {
            pdc.set(pdcKey, PersistentDataType.DOUBLE, newMultiplier);
        }
    }

    public void removeMultiplierFromVillager(Villager villager) {
        if (villager == null) return;
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        if (!pdc.has(pdcKey, PersistentDataType.DOUBLE)) return;
        double old = pdc.get(pdcKey, PersistentDataType.DOUBLE);
        if (Double.compare(old, 1.0) == 0) {
            pdc.remove(pdcKey);
            return;
        }

        List<MerchantRecipe> newRecipes = new ArrayList<>(villager.getRecipes().size());
        for (MerchantRecipe r : villager.getRecipes()) {
            int currentMax = r.getMaxUses();
            int originalMax = (int) Math.round((double) currentMax / old);
            if (originalMax < 1) originalMax = 1;

            MerchantRecipe copy = new MerchantRecipe(
                    r.getResult(),
                    r.getUses(),
                    originalMax,
                    r.hasExperienceReward(),
                    r.getVillagerExperience(),
                    r.getPriceMultiplier(),
                    r.getDemand(),
                    r.getSpecialPrice()
            );
            copy.setIngredients(new ArrayList<>(r.getIngredients()));
            copy.setUses(r.getUses());
            newRecipes.add(copy);
        }
        villager.setRecipes(newRecipes);
        pdc.remove(pdcKey);
    }

    public void applyMultiplierToAllVillagers(double newMultiplier) {
        this.currentMultiplier = Math.max(1.0, newMultiplier);
        for (World world : VanillaChanges.getPlugin().getServer().getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof Villager villager) {
                    removeMultiplierFromVillager(villager);
                    applyMultiplierToVillager(villager, this.currentMultiplier);
                }
            }
        }
    }
}
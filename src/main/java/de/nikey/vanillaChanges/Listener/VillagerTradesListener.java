package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class VillagerTradesListener implements Listener {
    private final NamespacedKey multiplierKey = new NamespacedKey(VanillaChanges.getPlugin(), "applied_multiplier");
    private final NamespacedKey baseUsesKey = new NamespacedKey(VanillaChanges.getPlugin(), "base_uses");
    private double globalMultiplier;

    public VillagerTradesListener() {
        reloadConfig();
    }

    public void reloadConfig() {
        loadMultiplier();
        VanillaChanges.getPlugin().getLogger().info("Config neu geladen! Multiplikator = " + globalMultiplier);
    }

    public void loadMultiplier() {
        FileConfiguration config = VanillaChanges.getPlugin().getConfig();
        globalMultiplier = config.getDouble("villager.trade-multiplier", 1.0);
        if (globalMultiplier <= 0) {
            globalMultiplier = 1.0;
        }
    }

    @EventHandler
    public void onAcquireTrade(VillagerAcquireTradeEvent event) {
        Villager villager = (Villager) event.getEntity();
        MerchantRecipe recipe = event.getRecipe();

        PersistentDataContainer pdc = villager.getPersistentDataContainer();

        List<Integer> bases = getBaseUses(pdc, villager);
        while (bases.size() <= villager.getRecipeCount()) {
            bases.add(recipe.getMaxUses());
        }
        saveBaseUses(pdc, bases);

        recipe.setMaxUses((int) Math.max(1, recipe.getMaxUses() * globalMultiplier));
        event.setRecipe(recipe);
    }

    @EventHandler
    public void onVillagerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        double applied = pdc.getOrDefault(multiplierKey, PersistentDataType.DOUBLE, 1.0);

        if (Double.compare(applied, globalMultiplier) != 0) {
            updateTrades(villager, pdc);
            pdc.set(multiplierKey, PersistentDataType.DOUBLE, globalMultiplier);
        }
    }

    private void updateTrades(Villager villager, PersistentDataContainer pdc) {
        List<Integer> bases = getBaseUses(pdc, villager);
        List<MerchantRecipe> updated = new ArrayList<>();

        for (int i = 0; i < villager.getRecipes().size(); i++) {
            MerchantRecipe recipe = villager.getRecipes().get(i);
            int baseUses;
            if (i < bases.size()) {
                baseUses = bases.get(i);
            } else {
                baseUses = recipe.getMaxUses(); // fallback
                bases.add(baseUses);
            }

            MerchantRecipe copy = new MerchantRecipe(recipe.getResult(), (int) Math.max(1, baseUses * globalMultiplier));
            copy.setVillagerExperience(recipe.getVillagerExperience());
            copy.setIngredients(recipe.getIngredients());
            copy.setPriceMultiplier(recipe.getPriceMultiplier());
            copy.setUses(recipe.getUses());

            updated.add(copy);
        }

        villager.setRecipes(updated);
        saveBaseUses(pdc, bases);
    }

    private void saveBaseUses(PersistentDataContainer pdc, List<Integer> bases) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bases.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(bases.get(i));
        }
        pdc.set(baseUsesKey, PersistentDataType.STRING, sb.toString());
    }

    private List<Integer> getBaseUses(PersistentDataContainer pdc, Villager villager) {
        String stored = pdc.get(baseUsesKey, PersistentDataType.STRING);
        List<Integer> list = new ArrayList<>();
        if (stored != null) {
            for (String s : stored.split(";")) {
                try {
                    list.add(Integer.parseInt(s));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }
}
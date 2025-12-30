package de.nikey.vanillaChanges.Listener;

import de.nikey.vanillaChanges.VanillaChanges;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VillagerTradesListener implements Listener {
    private final NamespacedKey multiplierKey = new NamespacedKey(VanillaChanges.getPlugin(), "applied_multiplier");
    private final NamespacedKey baseUsesKey = new NamespacedKey(VanillaChanges.getPlugin(), "base_uses");
    private double globalMultiplier;
    public static boolean instantRestock;

    public VillagerTradesListener() {
        reloadConfig();
    }

    public void reloadConfig() {
        loadMultiplier();
        VanillaChanges.getPlugin().getLogger().info("Config neu geladen! Multiplikator = " + globalMultiplier);
    }

    public void loadMultiplier() {
        FileConfiguration config = VanillaChanges.getPlugin().getConfig();
        instantRestock = config.getBoolean("villager.instantRestock");
        globalMultiplier = config.getDouble("villager.trade-multiplier", 1.0);
        if (globalMultiplier <= 0) {
            globalMultiplier = 1.0;
        }
    }

    @EventHandler
    public void onAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;

        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        List<Integer> bases = getBaseUses(pdc);

        if (villager.getRecipeCount() == 0) {
            bases.clear();
        }

        MerchantRecipe recipe = event.getRecipe();
        int originalMaxUses = recipe.getMaxUses();

        recipe.setMaxUses((int) Math.max(1, originalMaxUses * globalMultiplier));

        bases.add(originalMaxUses);
        saveBaseUses(pdc, bases);

        pdc.set(multiplierKey, PersistentDataType.DOUBLE, globalMultiplier);
    }

    @EventHandler
    public void onProfessionChange(VillagerCareerChangeEvent event) {
        Villager villager = event.getEntity();
        PersistentDataContainer pdc = villager.getPersistentDataContainer();

        pdc.remove(baseUsesKey);
        pdc.remove(multiplierKey);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void villager(VillagerReplenishTradeEvent e) {
        if (instantRestock) {
            AbstractVillager entity = e.getEntity();
            if (entity instanceof Villager v) {
                v.setRestocksToday(0);

                for (MerchantRecipe r : v.getRecipes()) {
                    if (r.getDemand() > 0) {
                        r.setDemand(0);
                    }
                }
            }
        }
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
        List<Integer> bases = getBaseUses(pdc);
        List<MerchantRecipe> currentRecipes = villager.getRecipes();
        List<MerchantRecipe> updatedRecipes = new ArrayList<>();

        for (int i = 0; i < currentRecipes.size(); i++) {
            MerchantRecipe oldRecipe = currentRecipes.get(i);
            int baseValue;

            if (i < bases.size()) {
                baseValue = bases.get(i);
            } else {
                baseValue = oldRecipe.getMaxUses();
                bases.add(baseValue);
            }

            int newMaxUses = (int) Math.max(1, baseValue * globalMultiplier);

            MerchantRecipe newRecipe = new MerchantRecipe(
                    oldRecipe.getResult(),
                    oldRecipe.getUses(),
                    newMaxUses,
                    oldRecipe.hasExperienceReward(),
                    oldRecipe.getVillagerExperience(),
                    oldRecipe.getPriceMultiplier()
            );
            newRecipe.setIngredients(oldRecipe.getIngredients());
            updatedRecipes.add(newRecipe);
        }

        villager.setRecipes(updatedRecipes);
        saveBaseUses(pdc, bases);
    }

    private void saveBaseUses(PersistentDataContainer pdc, List<Integer> bases) {
        String data = bases.stream().map(String::valueOf).collect(Collectors.joining(";"));
        pdc.set(baseUsesKey, PersistentDataType.STRING, data);
    }

    private List<Integer> getBaseUses(PersistentDataContainer pdc) {
        String stored = pdc.get(baseUsesKey, PersistentDataType.STRING);
        List<Integer> list = new ArrayList<>();
        if (stored != null && !stored.isEmpty()) {
            for (String s : stored.split(";")) {
                try {
                    list.add(Integer.parseInt(s));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }
}
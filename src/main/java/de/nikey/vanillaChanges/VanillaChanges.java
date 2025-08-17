package de.nikey.vanillaChanges;

import de.nikey.vanillaChanges.Commands.VanillaChangesCommand;
import de.nikey.vanillaChanges.Listener.CooldownListener;
import de.nikey.vanillaChanges.Listener.EndermiteListener;
import de.nikey.vanillaChanges.Listener.ItemDestroyListener;
import de.nikey.vanillaChanges.Listener.PotionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class VanillaChanges extends JavaPlugin {
    private static VanillaChanges plugin;

    @Override
    public void onEnable() {
        plugin = this;

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new CooldownListener(),this);
        manager.registerEvents(new EndermiteListener(),this);
        manager.registerEvents(new PotionListener(),this);
        manager.registerEvents(new ItemDestroyListener(), this);

        getCommand("vanillachanges").setExecutor(new VanillaChangesCommand());

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static VanillaChanges getPlugin() {
        return plugin;
    }
}

package de.nikey.vanillaChanges.Commands;

import de.nikey.vanillaChanges.Data.MaceControlData;
import de.nikey.vanillaChanges.Listener.CustomEntityAttributesFeature;
import de.nikey.vanillaChanges.Listener.EnchantmentLimiterListener;
import de.nikey.vanillaChanges.VanillaChanges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VanillaChangesCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp())return true;
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            VanillaChanges.getPlugin().reloadConfig();
            VanillaChanges.getPlugin().loadRecipes();
            VanillaChanges.getPlugin().loadMultipliers();
            VanillaChanges.getPlugin().reloadCustomPotions();
            VanillaChanges.getPlugin().reloadVanillaChangesVillagerConfig();
            CustomEntityAttributesFeature.loadConfig();
            MaceControlData.loadConfigValues();
            EnchantmentLimiterListener.loadLimits();
            sender.sendMessage(Component.text("Config was reloaded.").color(NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("Usage: /vanillachanges reload")
                .color(NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of("reload");
    }
}

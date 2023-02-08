package io.github.thebusybiscuit.electricspawners;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;

public class ElectricSpawners extends JavaPlugin implements Listener, SlimefunAddon, CommandExecutor, TabCompleter {

    @Override
    public void onEnable() {
        Config cfg = new Config(this);

        // Setting up bStats
        new Metrics(this, 6163);

        if (cfg.getBoolean("options.auto-update") && getDescription().getVersion().startsWith("DEV - ")) {
            new GitHubBuildsUpdater(this, getFile(), "TheBusyBiscuit/ElectricSpawners/master").start();
        }

        ItemGroup itemGroup = new ItemGroup(new NamespacedKey(this, "electric_spawners"), new CustomItemStack(PlayerHead.getItemStack(PlayerSkin.fromHashCode("db6bd9727abb55d5415265789d4f2984781a343c68dcaf57f554a5e9aa1cd")), "&9Electric Spawners"));
        Research research = new Research(new NamespacedKey(this, "electric_spawners"), 4820, "Powered Spawners", 30);

        for (String mob : cfg.getStringList("mobs")) {
            try {
                EntityType type = EntityType.valueOf(mob);
                new ElectricSpawner(itemGroup, mob, type, research, this).register(this);
            } catch (IllegalArgumentException x) {
                getLogger().log(Level.WARNING, "An Error has occured while adding an Electric Spawner for the (posibly outdated or invalid) EntityType \"{0}\"", mob);
            } catch (Exception x) {
                getLogger().log(Level.SEVERE, x, () -> "An Error has occured while adding an Electric Spawner for the EntityType \"" + mob + "\"");
            }
        }

        research.register();

        PluginCommand cmd = Objects.requireNonNull(this.getCommand("electricspawners"));
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/TheBusyBiscuit/ElectricSpawners/issues";
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        if (!sender.hasPermission("electricspawners.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
            return true;
        }
        this.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Reloaded ElectricSpawners.");
        return true;
    }

    @EventHandler
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        list.add("reload");
        return list;
    }
}
package com.empcraft.wrg;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.empcraft.wrg.listener.PlayerListener;
import com.empcraft.wrg.regions.WorldguardFeature;
import com.empcraft.wrg.util.MainUtil;
import com.empcraft.wrg.util.RegionHandler;
import com.empcraft.wrg.util.VaultHandler;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public final class WorldeditRegions extends JavaPlugin implements Listener {
    public static String            version   = "0";
    public static WorldeditRegions  plugin;
    public static WorldEditPlugin   worldedit = null;
    public static YamlConfiguration language;
    public static FileConfiguration config;

    @Override
    public void onDisable() {
        this.reloadConfig();
        this.saveConfig();
        MainUtil.sendMessage(null, "&f&oThanks for using &aWorldeditRegions&f by &dEmpire92&f!");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        return PlayerListener.onCommand(sender, cmd, label, args);
    }

    public static boolean iswhitelisted(final String arg) {
        final List<String> mylist = plugin.getConfig().getStringList("whitelist");
        for (final String current : mylist) {
            if (arg.equalsIgnoreCase(current)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEnable() {
        plugin = this;
        version = getDescription().getVersion();

        saveResource("english.yml", true);
        getConfig().options().copyDefaults(true);
        final Map<String, Object> options = new HashMap<String, Object>();
        getConfig().set("version", version);
        options.put("create.expand-vert", true);
        options.put("factions.max-chunk-traversal", 10);
        options.put("language", "english");
        options.put("create.add-owner", true);
        options.put("min-width", 16);
        options.put("worldguard.require-custom-flag", false);
        options.put("max-claim-area", 1024);
        final List<String> ignore = Arrays.asList("PlotMe", "PlotWorld");
        options.put("ignore-worlds", ignore);
        for (final Entry<String, Object> node : options.entrySet()) {
            if (!getConfig().contains(node.getKey())) {
                getConfig().set(node.getKey(), node.getValue());
            }
        }
        saveConfig();
        this.saveDefaultConfig();
        final File yamlFile = new File(getDataFolder(), getConfig().getString("language").toLowerCase() + ".yml");
        language = YamlConfiguration.loadConfiguration(yamlFile);
        config = getConfig();
        RegionHandler.disabled.addAll(config.getStringList("ignore-worlds"));

        MainUtil.sendMessage(null, "&8----&9====&7WorldeditRegions v" + version + "&9====&8----");
        MainUtil.sendMessage(null, "&dby Empire92");

        final Plugin vaultPlugin = getServer().getPluginManager().getPlugin("Vault");
        if ((vaultPlugin != null) && vaultPlugin.isEnabled()) {
            new VaultHandler(this, vaultPlugin);
            MainUtil.sendMessage(null, "&8[&9WRG&8] &7Hooking into Vault");
        }

        final Plugin worldguardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if ((worldguardPlugin != null) && worldguardPlugin.isEnabled()) {
            final WorldguardFeature wgf = new WorldguardFeature(worldguardPlugin, this);
            RegionHandler.regions.add(wgf);
            MainUtil.sendMessage(null, "&8[&9WRG&8] &7Hooking into WorldGuard");

        }
        worldedit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            RegionHandler.refreshPlayer(player);
            RegionHandler.setMask(player, false);
        }
    }

}

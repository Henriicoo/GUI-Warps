package com.henriquenapimo1.guiwarps;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GUIWarps extends JavaPlugin implements Listener, TabCompleter {

    private static Essentials ess;
    private static WarpManager manager;

    @Override
    public void onEnable() {
        ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

        if(ess == null) {
            getLogger().severe("[GUIWarps] Dependência EssentialsX não encontrada! Desabilitando o plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new PluginListener(), this);

        getCommand("warps").setExecutor(new PluginListener());
        getCommand("warps").setTabCompleter(this);

        getCommand("gwarp").setExecutor(new PluginListener());
        getCommand("gwarp").setTabCompleter(this);

        manager = new WarpManager(getMain());

        saveDefaultConfig();

        ess.getWarps().getList().forEach(manager::setupWarp);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(command.getName().equals("warp"))
            if(args.length <= 1)
                return new ArrayList<>(ess.getWarps().getList());

        if(command.getName().equals("gwarp")) {
            if (args.length <= 1)
                return Arrays.asList("setname", "setlore", "setglowing", "setpos", "setitem");

            if (args.length <= 2)
                return new ArrayList<>(ess.getWarps().getList());

            if (args.length <= 3) {
                if (args[0].equalsIgnoreCase("setglowing"))
                    return Arrays.asList("true", "false");

                if(args[0].equalsIgnoreCase("setitem")) {
                    List<String> idList = new ArrayList<>();
                    Arrays.stream(Material.values()).forEach(m -> {
                        if(m.name().toLowerCase().startsWith(args[2].toLowerCase()))
                            idList.add(m.name());
                    });
                    return idList;
                }
            }
        }

        return null;
    }

    public static Essentials getEssentials() {
        return ess;
    }

    public static WarpManager getManager() {
        return manager;
    }

    public static GUIWarps getMain() {
        return (GUIWarps) Bukkit.getPluginManager().getPlugin("GUI-Warps");
    }
}

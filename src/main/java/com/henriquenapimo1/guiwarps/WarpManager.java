package com.henriquenapimo1.guiwarps;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarpManager {

    private final HashMap<Integer, ItemStack> items;
    private final GUIWarps main;

    public WarpManager(GUIWarps main) {
        this.main = main;
        this.items = new HashMap<>();
    }

    public String getWarpNameByPosition(int pos) {
        ItemStack item = items.get(pos);

        if(item == null)
            return null;

        ItemMeta meta = item.getItemMeta();
        String name = meta.getLore().get(meta.getLore().size()-1);
        name = name.replace("§", "");

        return name;
    }

    public boolean teleportPlayerToWarp(Player p, int id) {
        try {
            Location warp = GUIWarps.getEssentials().getWarps().getWarp(
                    getWarpNameByPosition(id));

            p.teleport(getTeleportLocation(warp));

            p.playSound(warp, Sound.ITEM_PICKUP,100,10);

            for(int i = 0; i < 5; i++)
                warp.getWorld().playEffect(warp, Effect.ENDER_SIGNAL,0);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean teleportPlayerToWarp(Player p, String name) {
        try {
            Location warp = GUIWarps.getEssentials().getWarps().getWarp(name);

            p.teleport(getTeleportLocation(warp));

            p.playSound(warp, Sound.ITEM_PICKUP,100,10);

            for(int i = 0; i < 5; i++)
                warp.getWorld().playEffect(warp, Effect.ENDER_SIGNAL,0);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Location getTeleportLocation(Location l) {
        //Location loc = l.getWorld().getHighestBlockAt(l).getLocation();
        Location loc = l.getWorld().getBlockAt(l).getLocation();

        if(loc == null)
            return null;

        loc.add(0.5,1,0.5);
        loc.setDirection(l.getDirection());

        return loc;
    }

    public void setupWarp(String name) {
        if(main.getConfig().isSet(name)) {
            Map.Entry<Integer, ItemStack> e = loadWarp(name);

            if(e == null) {
                main.getLogger().severe("Erro ao tentar carregar a warp "+name+": O resultado é nulo!");
                return;
            }

            items.put(e.getKey(),e.getValue());
            return;
        }

        Map.Entry<Integer, ItemStack> e = setupNewWarp(name);
        items.put(e.getKey(),e.getValue());
    }

    private Map.Entry<Integer,ItemStack> loadWarp(String name) {
        Material m = Material.valueOf(main.getConfig().getString(name+".material"));

        ItemStack warpItem = new ItemStack(m);
        ItemMeta warpMeta = warpItem.getItemMeta();

        String nome = "§7"+name;
        if(main.getConfig().isSet(name+".displayname"))
            nome = ChatColor.translateAlternateColorCodes('&',main.getConfig().getString(name+".displayname"));

        warpMeta.setDisplayName(nome);

        String lore = ChatColor.translateAlternateColorCodes('&',main.getConfig().getString(name+".lore"));
        String lore2 = String.format("§%s",String.join("§", name.split("")));

        warpMeta.setLore(Arrays.asList(lore,lore2));

        if(main.getConfig().getBoolean(name+".glowing")) {
            warpMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            warpMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        warpItem.setItemMeta(warpMeta);

        int i = main.getConfig().getInt(name+".pos")-1;

        if(i < 0) {
            main.getLogger().severe("[GUIWarps] Erro ao tentar carregar a warp "+name+": A posição selecionada é inválida!");
            return null;
        }
        return new AbstractMap.SimpleEntry<>(i,warpItem);
    }

    private Map.Entry<Integer,ItemStack> setupNewWarp(String name) {
        ItemStack warpItem = new ItemStack(Material.PAPER);
        ItemMeta warpMeta = warpItem.getItemMeta();

        FileConfiguration config = main.getConfig();
        config.set(name+".material",Material.PAPER.name());

        String text = "§7"+name;
        warpMeta.setDisplayName(text);
        config.set(name+".displayname",text);

        String lore = "§7Clique para ir à warp " + name;
        String lore2 = String.format("§%s",String.join("§", name.split("")));

        warpMeta.setLore(Arrays.asList(lore,lore2));
        config.set(name+".lore",lore);

        warpItem.setItemMeta(warpMeta);

        // Pega o maior número do mapa
        Map.Entry<Integer, ItemStack> maxEntry = null;

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            if (maxEntry == null || entry.getKey().compareTo(maxEntry.getKey()) > 0)
                maxEntry = entry;
        }

        int i = -1;

        if(maxEntry != null)
            i = maxEntry.getKey();

        config.set(name+".pos",i+2);

        try {
            config.save(new File(main.getDataFolder(),"config.yml"));
        } catch (IOException e) {
            main.getLogger().severe(String.format("[GUIWarps] Erro ao tentar salvar a warp %s!",name));
            e.printStackTrace();
        }

        return new AbstractMap.SimpleEntry<>(i+1,warpItem);
    }

    public Inventory getWarpMenu(Player p) {
        int slots = main.getConfig().getInt("inv-slots")*9;

        if(slots > 9*6)
            slots = 9*6;

        if(slots == 0)
            slots = 9;

        Inventory inv = Bukkit.createInventory(null,slots,main.getConfig().getString("gui-name")+" §w§a§r§p§g§u§i");

        items.forEach((pos, item) -> {
            ItemMeta meta = item.getItemMeta();
            int n = meta.getLore().size() - 1;

            String name = meta.getLore().get(n);
            name = name.replace("§", "");

            boolean hasPerm = p.hasPermission("essentials.warp." + name);

            if (!hasPerm) {
                meta.getLore().set(n, "§c§lSem Permissão");
            } else {
                meta.getLore().remove(n);
            }

            item.setItemMeta(meta);

            inv.setItem(pos, item);
        });

        return inv;
    }

    public boolean setItem(String warp, Material m) {
        if(!main.getConfig().isSet(warp))
            return false;

        int i = main.getConfig().getInt(warp+".pos")-1;

        ItemStack item = items.get(i);

        if(item == null)
            return false;

        item.setType(m);
        items.replace(i,item);

        FileConfiguration config = main.getConfig();
        config.set(warp+".material",m.name());

        try {
            config.save(new File(main.getDataFolder(),"config.yml"));
        } catch (IOException e) {
            main.getLogger().severe(String.format("[GUIWarps] Erro ao tentar salvar a warp %s!",warp));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean setName(String warp, String name) {
        if(!main.getConfig().isSet(warp))
            return false;

        int i = main.getConfig().getInt(warp+".pos")-1;

        ItemStack item = items.get(i);

        if(item == null)
            return false;

        FileConfiguration config = main.getConfig();
        config.set(warp+".displayname",name);

        name = ChatColor.translateAlternateColorCodes('&',name);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        items.replace(i,item);

        try {
            config.save(new File(main.getDataFolder(),"config.yml"));
        } catch (IOException e) {
            main.getLogger().severe(String.format("[GUIWarps] Erro ao tentar salvar a warp %s!",warp));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean setLore(String warp, String lore) {
        if(!main.getConfig().isSet(warp))
            return false;

        int i = main.getConfig().getInt(warp+".pos")-1;

        ItemStack item = items.get(i);

        if(item == null)
            return false;

        FileConfiguration config = main.getConfig();
        config.set(warp+".lore", lore);

        lore = ChatColor.translateAlternateColorCodes('&',lore);
        String lore2 = String.format("§%s",String.join("§", warp.split("")));

        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList(lore,lore2));
        item.setItemMeta(meta);

        items.replace(i,item);

        try {
            config.save(new File(main.getDataFolder(),"config.yml"));
        } catch (IOException e) {
            main.getLogger().severe(String.format("[GUIWarps] Erro ao tentar salvar a warp %s!",warp));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean setGlowing(String warp, boolean glowing) {
        if(!main.getConfig().isSet(warp))
            return false;

        int i = main.getConfig().getInt(warp+".pos")-1;

        ItemStack item = items.get(i);

        if(item == null)
            return false;

        FileConfiguration config = main.getConfig();
        config.set(warp+".glowing",glowing);

        ItemMeta meta = item.getItemMeta();

        if(glowing) {
            meta.addEnchant(Enchantment.DURABILITY,1,false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(Enchantment.DURABILITY);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);

        items.replace(i,item);

        try {
            config.save(new File(main.getDataFolder(),"config.yml"));
        } catch (IOException e) {
            main.getLogger().severe(String.format("[GUIWarps] Erro ao tentar salvar a warp %s!",warp));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean setPos(String warp, int pos) {
        if(!main.getConfig().isSet(warp))
            return false;

        int i = main.getConfig().getInt(warp+".pos")-1;

        ItemStack item = items.get(i);

        if(item == null)
            return false;

        FileConfiguration config = main.getConfig();
        config.set(warp+".pos",pos);


        items.remove(i);
        items.put(pos-1,item);

        try {
            config.save(new File(main.getDataFolder(),"config.yml"));
        } catch (IOException e) {
            main.getLogger().severe(String.format("[GUIWarps] Erro ao tentar salvar a warp %s!",warp));
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

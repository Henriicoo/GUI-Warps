package com.henriquenapimo1.guiwarps;

import net.essentialsx.api.v2.events.WarpModifyEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class PluginListener implements CommandExecutor, org.bukkit.event.Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Você precisa ser um player para executar esse comando!");
            return false;
        }

        Player p = (Player) sender;

        switch (command.getName()) {
            case "warps":
            case "warp": {
                if(command.getName().equals("warp") && args.length != 0) {
                    if(GUIWarps.getManager().teleportPlayerToWarp(p,args[0]))
                        p.sendMessage("§8[§a§lWarps§8] §bTeletransportado com sucesso!");
                    else
                        p.sendMessage("§8[§a§lWarps§8] §cErro! Essa warp não existe! Digite §l/warp§r§c para ver as warps disponíveis.");

                    return true;
                }
                p.openInventory(GUIWarps.getManager().getWarpMenu(p));
                p.playSound(p.getLocation(), Sound.ITEM_PICKUP,100,0);

                return true;
            }
            case "gwarp": {
                if(!p.hasPermission("guiwarps.admin")) {
                    p.sendMessage("§cVocê não pode executar esse comando!");
                    return false;
                }

                if(args.length <= 2) {
                    p.sendMessage("§8[§a§lWarps§8] §b§lComandos Disponíveis:\n" +
                            "§a/gwarp setname [warp] [nome] §7- §fSeta o nome da warp\n" +
                            "§a/gwarp setlore [warp] [lore] §7- §fSeta a descrição da warp\n" +
                            "§a/gwarp setglowing [warp] [true/false] §7- §fSeta o brilho do item\n" +
                            "§a/gwarp setpos [warp] [posição] §7- §fSeta a posição da warp no gui\n" +
                            "§a/gwarp setitem [warp] [material] §7- §fSeta o material do item");
                    return false;
                } else {
                    if(!GUIWarps.getEssentials().getWarps().getList().contains(args[1])) {
                        p.sendMessage("§8[§a§lWarps§8] §bErro: Essa warp não existe!");
                       return false;
                    }

                    String arguments = String.join(" ",args).replace(args[0]+" "+args[1]+" ","");

                    switch (args[0].toLowerCase()) {
                        case "setname": {
                            if(GUIWarps.getManager().setName(args[1],arguments)) {
                                p.sendMessage("§8[§a§lWarps§8] §aNome setado com sucesso!");
                                return true;
                            } else {
                                p.sendMessage("§8[§a§lWarps§8] §cOcorreu um erro ao tentar setar o nome dessa warp! Veja o console.");
                                return false;
                            }
                        }
                        case "setlore": {
                            if(GUIWarps.getManager().setLore(args[1],arguments)) {
                                p.sendMessage("§8[§a§lWarps§8] §aLore setado com sucesso!");
                                return true;
                            } else {
                                p.sendMessage("§8[§a§lWarps§8] §cOcorreu um erro ao tentar setar o lore dessa warp! Veja o console.");
                                return false;
                            }
                        }
                        case "setglowing": {
                            boolean bool;
                            if(args[2].equalsIgnoreCase("true"))
                                bool = true;
                            else if(args[2].equalsIgnoreCase("false"))
                                bool = false;
                            else {
                                p.sendMessage("§8[§a§lWarps§8] §cErro: Use true ou false");
                                return false;
                            }

                            if(GUIWarps.getManager().setGlowing(args[1],bool)) {
                                p.sendMessage("§8[§a§lWarps§8] §aBrilho setado com sucesso!");
                                return true;
                            } else {
                                p.sendMessage("§8[§a§lWarps§8] §cOcorreu um erro ao tentar setar o brilho dessa warp! Veja o console.");
                                return false;
                            }
                        }
                        case "setpos": {
                            int i;
                            try {
                                i = Integer.parseInt(args[2]);
                            }catch (Exception e) {
                                p.sendMessage("§8[§a§lWarps§8] §cErro: Você precisa inserir um número válido!");
                                return false;
                            }

                            if(i == 0 || i > 9*6) {
                                p.sendMessage("§8[§a§lWarps§8] §cErro: O número precisa estar entre 1 e 54!");
                                return false;
                            }

                            if(GUIWarps.getManager().setPos(args[1],i)) {
                                p.sendMessage("§8[§a§lWarps§8] §aPosição setada com sucesso!");
                                return true;
                            } else {
                                p.sendMessage("§8[§a§lWarps§8] §cOcorreu um erro ao tentar setar a posição dessa warp! Veja o console.");
                                return false;
                            }
                        }
                        case "setitem": {
                            Material mat = Arrays.stream(Material.values()).filter(m -> m.name().equalsIgnoreCase(args[2]) ||
                                    args[2].equals(String.valueOf(m.getId()))).findFirst().orElse(null);

                            if(mat == null) {
                                p.sendMessage("§8[§a§lWarps§8] §cErro: Você precisa inserir um id válido!");
                                return false;
                            }

                            if(GUIWarps.getManager().setItem(args[1],mat)) {
                                p.sendMessage("§8[§a§lWarps§8] §aItem setado com sucesso!");
                                return true;
                            } else {
                                p.sendMessage("§8[§a§lWarps§8] §cOcorreu um erro ao tentar setar o item dessa warp! Veja o console.");
                                return false;
                            }
                        } default:
                            p.sendMessage("§8[§a§lWarps§8] §cErro: Comando inválido.");
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onWarpCreate(WarpModifyEvent event) {
        if(!event.getCause().equals(WarpModifyEvent.WarpModifyCause.CREATE))
            return;

        GUIWarps.getManager().setupWarp(event.getWarpName());
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player) || !event.getView().getTitle().endsWith("§w§a§r§p§g§u§i"))
            return;

        event.setCancelled(true);

        Player p = (Player) event.getWhoClicked();

        if(GUIWarps.getManager().teleportPlayerToWarp(p,event.getSlot()))
            p.sendMessage("§8[§a§lWarps§8] §bTeletransportado com sucesso!");
        else
            return;

        p.closeInventory();
    }
}

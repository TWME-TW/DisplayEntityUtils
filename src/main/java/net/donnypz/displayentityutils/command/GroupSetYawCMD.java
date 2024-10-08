package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayGroupManager;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

class GroupSetYawCMD implements SubCommand{
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.GROUP_TRANSFORM)){
            return;
        }

        SpawnedDisplayEntityGroup group = DisplayGroupManager.getSelectedSpawnedGroup(player);
        if (group == null) {
            DisplayEntityPluginCommand.noGroupSelection(player);
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("Incorrect Usage! /mdis group setyaw <yaw> [-pivot]", NamedTextColor.RED));
            return;
        }

        try{
            double yaw = Double.parseDouble(args[2]);
            boolean pivot = false;
            if (args.length > 3){
                if (args[3].equalsIgnoreCase("-pivot")){
                    pivot = true;
                }
            }
            double oldYaw = group.getLocation().getYaw();
            group.setYaw((float) yaw, pivot);
            player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.GREEN+"Yaw set!");
            player.sendMessage(Component.text("Old Yaw: "+oldYaw, NamedTextColor.GRAY));
        }
        catch(NumberFormatException e){
            player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.RED+"Please enter a valid number!");
        }
    }
}

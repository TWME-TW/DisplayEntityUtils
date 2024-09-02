package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayGroupManager;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import net.donnypz.displayentityutils.utils.GroupResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

class GroupMergeCMD implements SubCommand{
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.GROUP_MERGE)){
            return;
        }

        SpawnedDisplayEntityGroup group = DisplayGroupManager.getSelectedSpawnedGroup(player);
        if (group == null) {
            DisplayEntityPluginCommand.noGroupSelection(player);
            return;
        }

        if (args.length < 3) {
            player.sendMessage(DisplayEntityPlugin.pluginPrefix + ChatColor.RED + "Enter a number for the distance from your group to attempt to merge other groups");
            player.sendMessage(Component.text("/mdis group merge <distance>", NamedTextColor.GRAY));
            return;
        }

        try{
            double radius = Double.parseDouble(args[2]);
            if (radius <= 0){
                player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.RED+"Enter a number greater than 0 for the merging distance!");
                return;
            }
            List<GroupResult> results = DisplayGroupManager.getSpawnedGroupsNearLocation(group.getMasterPart().getEntity().getLocation(), radius);
            if (results.isEmpty() || results.size() == 1){
                player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.RED+"Your selected group is the only group within the set merging distance!");
                return;
            }
            for (GroupResult result : results){
                if (group.equals(result.group())){
                    continue;
                }
                group.merge(result.group());
            }
            player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.GREEN+"Successfully merged nearby groups");
            group.glow(60, true);
        }
        catch(NumberFormatException e){
            player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.RED+"Enter a valid number for the merging distance!");
        }
    }
}

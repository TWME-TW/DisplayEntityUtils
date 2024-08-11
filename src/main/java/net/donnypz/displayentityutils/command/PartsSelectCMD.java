package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayGroupManager;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedPartSelection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

class PartsSelectCMD implements SubCommand{
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.PARTS_SELECT)){
            return;
        }

        SpawnedDisplayEntityGroup group = DisplayGroupManager.getSelectedSpawnedGroup(player);
        if (group == null) {
            DisplayEntityPluginCommand.noGroupSelection(player);
            return;
        }

        SpawnedPartSelection partSelection = DisplayGroupManager.getPartSelection(player);
        if (partSelection != null){
            partSelection.remove();
        }
        if (args.length < 3){
            player.sendMessage(Component.text("/mdis parts select <part-tag>", NamedTextColor.RED));
            return;
        }
        partSelection = new SpawnedPartSelection(group, args[2]);
        if (partSelection.getSelectedParts().isEmpty()){
            player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.RED+"Failed to find parts with that part tag!");
            return;
        }
        player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.GREEN+"Part(s) successfully selected! "+ChatColor.WHITE+"(Part(s) Tagged: "+args[1]+")");
        DisplayGroupManager.setPartSelection(player, partSelection, false);
        partSelection.glow(30, false);
    }

}
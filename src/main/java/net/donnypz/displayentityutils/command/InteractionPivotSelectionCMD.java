package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayGroupManager;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityPart;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedPartSelection;
import net.donnypz.displayentityutils.utils.DisplayUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;

class InteractionPivotSelectionCMD implements SubCommand{
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.INTERACTION_PIVOT)){
            return;
        }

        SpawnedDisplayEntityGroup group = DisplayGroupManager.getSelectedSpawnedGroup(player);
        if (group == null){
            DisplayEntityPluginCommand.noGroupSelection(player);
            return;
        }

        if (args.length < 3){
            player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.RED+"Incorrect Usage! /mdis interaction pivotselection <angle>");
            return;
        }

        try{
            double angle = Double.parseDouble(args[2]);

            SpawnedPartSelection selection = DisplayGroupManager.getPartSelection(player);
            if (selection != null){
                selection.pivot(angle);
                player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.GREEN+"Pivoting all Interaction Entities in selection around group!");
            }

            else{
                for (SpawnedDisplayEntityPart part : group.getSpawnedParts(SpawnedDisplayEntityPart.PartType.INTERACTION)){
                    part.pivot(angle);
                }
            }
        }
        catch(NumberFormatException e){
            player.sendMessage(DisplayEntityPlugin.pluginPrefix+ChatColor.RED+"Enter a valid number for the angle!");
        }
    }
}

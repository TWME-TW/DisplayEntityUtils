package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;

class InteractionHeightCMD implements SubCommand{
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.INTERACTION_DIMENSION)){
            return;
        }

        if (args.length < 3){
            player.sendMessage(Component.text("Incorrect Usage! /mdis interaction setheight <height>", NamedTextColor.RED));
            return;
        }
        setInteractionDimensions(player, args, "height");
    }

    static void setInteractionDimensions(Player p , String[] args, String dim){
        Interaction interaction = InteractionCMD.getInteraction(p, true);
        if (interaction == null){
            return;
        }
        try{
            float change = Float.parseFloat(args[2]);
            if (change <= 0){
                p.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("Invalid "+dim+", enter a positive number!", NamedTextColor.RED)));
                return;
            }
            if (dim.equals("height")){
                interaction.setInteractionHeight(change);
            }
            else{
                interaction.setInteractionWidth(change);
            }

            p.sendMessage(Component.text("Successfully set interaction entity's "+dim+" to "+change, NamedTextColor.GREEN));
        }
        catch(NumberFormatException e) {
            p.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("Invalid " + dim + ", enter a positive number!", NamedTextColor.RED)));
        }
    }
}

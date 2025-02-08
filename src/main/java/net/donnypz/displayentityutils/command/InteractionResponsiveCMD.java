package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;

class InteractionResponsiveCMD implements PlayerSubCommand {
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.INTERACTION_RESPONSIVE)){
            return;
        }


        Component result;
        Interaction interaction = InteractionCMD.getInteraction(player, true);
        if (interaction == null){
            return;
        }
        if (interaction.isResponsive()){
            result = Component.text("DISABLED", NamedTextColor.RED);
        }
        else{
            result = Component.text("ENABLED", NamedTextColor.GREEN);
        }
        interaction.setResponsive(!interaction.isResponsive());
        player.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("Toggled Interaction Responsiveness to ")
                .append(result)));
    }
}

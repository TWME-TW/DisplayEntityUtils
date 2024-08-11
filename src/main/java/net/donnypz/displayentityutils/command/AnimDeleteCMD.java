package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayAnimationManager;
import net.donnypz.displayentityutils.managers.LoadMethod;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

class AnimDeleteCMD implements SubCommand{
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.ANIM_DELETE)){
            return;
        }
        if (args.length < 4) {
            player.sendMessage(Component.text("/mdis anim delete <anim-tag> <storage-location>", NamedTextColor.RED));
            return;
        }

        String tag = args[2];

        player.sendMessage(DisplayEntityPlugin.pluginPrefix+ ChatColor.GRAY+"Attempting to delete display animation "+ChatColor.WHITE+"(Tagged: "+tag+")");
        switch(args[3].toLowerCase()){
            case "all" ->{
                DisplayAnimationManager.deleteDisplayAnimation(LoadMethod.LOCAL, tag, player);
                DisplayAnimationManager.deleteDisplayAnimation(LoadMethod.MONGODB, tag, player);
                DisplayAnimationManager.deleteDisplayAnimation(LoadMethod.MYSQL, tag, player);
            }
            case "local" -> {
                DisplayAnimationManager.deleteDisplayAnimation(LoadMethod.LOCAL, tag, player);
            }
            case "mongodb" -> {
                DisplayAnimationManager.deleteDisplayAnimation(LoadMethod.MONGODB, tag, player);
            }
            case "mysql" -> {
                DisplayAnimationManager.deleteDisplayAnimation(LoadMethod.MYSQL, tag, player);
            }
            default ->{
                player.sendMessage(Component.text("Invalid storage option!", NamedTextColor.RED));
            }
        }
    }
}
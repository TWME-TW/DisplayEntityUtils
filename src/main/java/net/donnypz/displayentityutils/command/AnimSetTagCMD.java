package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayAnimationManager;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

class AnimSetTagCMD implements PlayerSubCommand {
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.ANIM_SET_TAG)){
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Component.text("Incorrect Usage! /mdis anim settag <anim-tag>", NamedTextColor.RED));
            return;
        }

        SpawnedDisplayAnimation anim = DisplayAnimationManager.getSelectedSpawnedAnimation(player);
        if (anim == null) {
            AnimCMD.noAnimationSelection(player);
            return;
        }
        String tag = args[2];
        anim.setAnimationTag(tag);
        player.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("Animation tag successfully set to \"" + tag + "\"", NamedTextColor.GREEN)));
    }
}

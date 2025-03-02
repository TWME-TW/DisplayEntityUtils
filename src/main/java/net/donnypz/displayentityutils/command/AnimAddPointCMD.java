package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayAnimationManager;
import net.donnypz.displayentityutils.managers.DisplayGroupManager;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimation;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimationFrame;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import net.donnypz.displayentityutils.utils.command.DEUCommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

class AnimAddPointCMD implements PlayerSubCommand {
    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.ANIM_ADD_FRAME_POINT)){
            return;
        }

        SpawnedDisplayEntityGroup group = DisplayGroupManager.getSelectedSpawnedGroup(player);
        if (group == null) {
            player.sendMessage(Component.text("You must have a group selected to do this animation command!", NamedTextColor.RED));
            return;
        }

        SpawnedDisplayAnimation anim = DisplayAnimationManager.getSelectedSpawnedAnimation(player);
        if (anim == null) {
            AnimCMD.noAnimationSelection(player);
            return;
        }

        if (DEUCommandUtils.isViewingRelativePoints(player)){
            player.sendMessage(Component.text("You cannot add a point while already viewing points!", NamedTextColor.RED));
            player.sendMessage(Component.text("| Run \"/mdis anim cancelpoints\" to stop viewing points", NamedTextColor.GRAY));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("Incorrect Usage! /mdis anim addpoint <frame-id>", NamedTextColor.RED));
            return;
        }
        try {
            int id = Integer.parseInt(args[2]);
            SpawnedDisplayAnimationFrame frame = anim.getFrame(id);
            frame.addFramePoint(group, player.getLocation());

            player.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("Frame Point Added!", NamedTextColor.GREEN)));
            player.sendMessage(Component.text("| Frame ID: "+id, NamedTextColor.GRAY));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            player.sendMessage(Component.text("Invalid Frame ID! Enter a number >= 0", NamedTextColor.RED));
        }
    }
}

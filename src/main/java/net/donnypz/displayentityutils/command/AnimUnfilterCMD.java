package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayAnimationManager;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

class AnimUnfilterCMD extends PlayerSubCommand {
    AnimUnfilterCMD() {
        super(Permission.ANIM_UNFILTER);
    }

    @Override
    public void execute(Player player, String[] args) {
        SpawnedDisplayAnimation anim = DisplayAnimationManager.getSelectedSpawnedAnimation(player);
        if (anim == null) {
            AnimCMD.noAnimationSelection(player);
            return;
        }
        anim.unfilter();

        player.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("Your selected animation no longer has a part filter", NamedTextColor.YELLOW)));
    }
}
package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.utils.bdengine.BDEngineModelResult;
import net.donnypz.displayentityutils.utils.bdengine.BDEngineUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URISyntaxException;


class BDEngineImportModelCMD implements SubCommand{

    @Override
    public void execute(Player player, String[] args) {
        if (!DisplayEntityPluginCommand.hasPermission(player, Permission.BDENGINE_SPAWN_MODEL)){
            return;
        }
        if (args.length < 3) {
            player.sendMessage(DisplayEntityPlugin.pluginPrefix + ChatColor.RED + "Incorrect Usage! /mdis bdengine importmodel <model-id>");
            return;
        }
        Location spawnLoc = player.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(DisplayEntityPlugin.getInstance(), () -> {
            try{
                int modelID = Integer.parseInt(args[2]);
                player.sendMessage(Component.text("Retrieving Model...", NamedTextColor.GRAY));
                BDEngineModelResult result = BDEngineUtils.requestModel(modelID);
                if (result == null){
                    throw new InterruptedException("Null Result");
                }

                Bukkit.getScheduler().runTask(DisplayEntityPlugin.getInstance(), () -> {
                    if (!result.spawn(spawnLoc)){
                        player.sendMessage(DisplayEntityPlugin.pluginPrefix + ChatColor.RED+ "Failed to spawn model! The spawn location's chunk is not loaded!");
                        return;
                    }
                    player.sendMessage(DisplayEntityPlugin.pluginPrefix + ChatColor.GREEN+"Attempted to spawn model at your location!");

                    if (result.getCommandCount() > 1){
                        player.sendMessage(Component.text("! The model resulted in creating multiple groups! Use \"/mdis group merge\" to merge " +
                                "the produced groups!", NamedTextColor.RED));
                    }

                    player.sendMessage(Component.text("\n- If your model did NOT spawn, the commands that are stored on the model are not compatible with your server version.\n", NamedTextColor.GRAY));

                });

            }
            catch(NumberFormatException e){
                player.sendMessage(Component.text("Enter a valid Model ID! This can be found on the page of a model on BDEngine. " +
                        "Look for \"ID for API\" and enter the number provided", NamedTextColor.RED));
            }
            catch(InterruptedException | IOException | URISyntaxException e){
                player.sendMessage(Component.text("An error occurred when attempting to retrieve the BDEngine model!", NamedTextColor.RED));
                e.printStackTrace();
            }
            catch(RuntimeException e){
                player.sendMessage(Component.text("An error occurred when attempting to retrieve the BDEngine model!", NamedTextColor.RED));
                player.sendMessage(Component.text("Error: "+e.getMessage(), NamedTextColor.GRAY, TextDecoration.ITALIC));
                e.printStackTrace();
            }
        });
    }
}

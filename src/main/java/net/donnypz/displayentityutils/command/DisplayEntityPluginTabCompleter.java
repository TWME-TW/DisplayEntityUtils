package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.utils.Direction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public final class DisplayEntityPluginTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {
            return DisplayEntityPluginCommand.getTabComplete();
        }

        List<String> suggestions = new ArrayList<>();
        if (args.length == 2){
            String arg1 = args[0].toLowerCase();
            switch (arg1) {
                case "interaction" -> {
                    return DisplayEntityPluginCommand.getInteractionTabComplete();
                }
                case "anim" -> {
                    return DisplayEntityPluginCommand.getAnimationTabComplete();
                }
                case "group" -> {
                    return DisplayEntityPluginCommand.getGroupTabComplete();
                }
                case "parts" -> {
                    return DisplayEntityPluginCommand.getPartsTabComplete();
                }
                case "bdengine" -> {
                    return DisplayEntityPluginCommand.getBDEngineTabComplete();
                }
                case "text" -> {
                    return DisplayEntityPluginCommand.getTextTabComplete();
                }
                case "item" -> {
                    return DisplayEntityPluginCommand.getItemTabComplete();
                }
                case "listgroups", "listanim" -> addStorages(suggestions);
                case "reload" -> {
                    suggestions.add("config");
                    suggestions.add("mythic");
                }
            }
        }
        else if (args.length == 3){
            switch (args[0].toLowerCase()){
                case "group" -> {
                    switch (args[1].toLowerCase()){
                        case "selectnearest" -> {
                            suggestions.add("<interaction-distance>");
                        }
                        case "glowcolor" -> {
                            addColors(suggestions);
                        }
                        case "move", "translate" -> {
                            addDirections(suggestions);
                        }
                        case "save" -> {
                            addStorages(suggestions);
                        }
                        case "billboard" -> {
                            addBillboard(suggestions);
                        }
                    }
                }
                case "anim" -> {
                    if (args[1].equalsIgnoreCase("save")) {
                        addStorages(suggestions);
                    }
                }
                case "parts" -> {
                    switch (args[1].toLowerCase()){
                        case "glowcolor" -> {
                            addColors(suggestions);
                        }
                        case "translate" -> {
                            addDirections(suggestions);
                        }
                        case "cycle" -> {
                            suggestions.add("first");
                            suggestions.add("prev");
                            suggestions.add("next");
                            suggestions.add("last");
                        }
                        case "seeduuids" -> {
                            suggestions.add("group");
                            suggestions.add("selection");
                        }
                        case "setblock" -> {
                            suggestions.add("-target");
                            suggestions.add("-held");
                            suggestions.add("block-id");
                        }
                        case "listtags" -> {
                            suggestions.add("part");
                            suggestions.add("selection");
                        }
                        case "billboard" -> {
                            addBillboard(suggestions);
                        }
                    }
                }
                case "item" -> {
                    switch (args[1].toLowerCase()){
                        case "set" -> {
                            suggestions.add("-held");
                            suggestions.add("item-id");
                        }
                        case "transform" -> {
                            for (ItemDisplay.ItemDisplayTransform transform : ItemDisplay.ItemDisplayTransform.values()){
                                suggestions.add(transform.name());
                            }
                        }
                    }
                }
                case "interaction" -> {
                    if (args[1].equalsIgnoreCase("addcmd")){
                        suggestions.add("player");
                        suggestions.add("console");
                    }
                }
                case "text" -> {
                    if (args[1].equalsIgnoreCase("background")){
                        addColors(suggestions);
                    }
                }
            }
        }
        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("group")){
                if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("setspawnanim")){
                    addStorages(suggestions);
                }
            }
            else if (args[0].equalsIgnoreCase("anim")){
                if (args[1].equalsIgnoreCase("delete")){
                    addStorages(suggestions);
                }
            }

            else if (args[0].equalsIgnoreCase("interaction")){
                if (args[1].equalsIgnoreCase("addcmd")){
                    suggestions.add("left");
                    suggestions.add("right");
                    suggestions.add("both");
                }
            }
        }
        else if (args.length == 5){
            if (args[0].equalsIgnoreCase("group")){
                if (args[1].equalsIgnoreCase("setspawnanim")){
                    suggestions.add("linear");
                    suggestions.add("loop");
                }
            }
        }
        return suggestions;
    }

    private void addDirections(List<String> suggestions){
        for (Direction dir : Direction.values()){
            suggestions.add(dir.name().toLowerCase());
        }
    }

    private void addBillboard(List<String> suggestions){
        for (Display.Billboard billboard : Display.Billboard.values()){
            suggestions.add(billboard.name());
        }
    }

    private void addStorages(List<String> suggestions){
        suggestions.add("all");
        suggestions.add("local");
        suggestions.add("mysql");
        suggestions.add("mongodb");
    }

    private void addColors(List<String> suggestions){
        suggestions.add("<hex-color>");
        suggestions.add("white");
        suggestions.add("silver");
        suggestions.add("gray");
        suggestions.add("black");
        suggestions.add("red");
        suggestions.add("maroon");
        suggestions.add("yellow");
        suggestions.add("olive");
        suggestions.add("lime");
        suggestions.add("green");
        suggestions.add("aqua");
        suggestions.add("teal");
        suggestions.add("blue");
        suggestions.add("navy");
        suggestions.add("fuchsia");
        suggestions.add("purple");
        suggestions.add("orange");
    }

}

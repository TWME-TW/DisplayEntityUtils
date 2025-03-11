package net.donnypz.displayentityutils.command;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.utils.Direction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

@ApiStatus.Internal
public class DisplayEntityPluginCommand implements CommandExecutor {

    private static final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public DisplayEntityPluginCommand(){
        subCommands.put("listgroups", new ListGroupsCMD());
        subCommands.put("listanims", new ListAnimationsCMD());

        subCommands.put("group", new GroupCMD());
        subCommands.put("parts", new PartsCMD());
        subCommands.put("item", new ItemCMD());
        subCommands.put("text", new TextCMD());
        subCommands.put("interaction", new InteractionCMD());
        subCommands.put("anim", new AnimCMD());
        subCommands.put("bdengine", new BDEngineCMD());
        subCommands.put("reload", new ReloadCMD());

    }

    public static List<String> getTabComplete(){
        return subCommands.keySet().stream().toList();
    }

    public static List<String> getInteractionTabComplete(){
        return InteractionCMD.getTabComplete();
    }

    public static List<String> getAnimationTabComplete(){
        return AnimCMD.getTabComplete();
    }

    public static List<String> getGroupTabComplete(){
        return GroupCMD.getTabComplete();
    }

    public static List<String> getPartsTabComplete(){
        return PartsCMD.getTabComplete();
    }

    public static List<String> getBDEngineTabComplete(){
        return BDEngineCMD.getTabComplete();
    }

    public static List<String> getTextTabComplete(){
        return TextCMD.getTabComplete();
    }

    public static List<String> getItemTabComplete(){
        return ItemCMD.getTabComplete();
    }


    static void invalidDirection(CommandSender sender){
        sender.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("Invalid direction type!", NamedTextColor.RED)));
        for (Direction d : Direction.values()){
            sender.sendMessage(Component.text("- ").append(Component.text(d.name().toLowerCase(), NamedTextColor.YELLOW)));
        }
    }

    static void noGroupSelection(Player player){
        player.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("You have not selected a spawned display entity group!", NamedTextColor.RED)));
        player.sendMessage(Component.text("/mdis group selectnearest <interaction-distance>", NamedTextColor.GRAY));
    }

    static void noPartSelection(Player player){
        player.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("You have not selected a part!", NamedTextColor.RED)));
        player.sendMessage(Component.text("/mdis parts cycle <first | prev | next>", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/mdis parts select <part-tag>", NamedTextColor.GRAY));
    }

    static void noPartSelectionInteraction(Player player){
        player.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("You must look at the interaction you wish to add the command to, or select a part!", NamedTextColor.RED)));
        player.sendMessage(Component.text("/mdis parts cycle <first | prev | next>", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/mdis parts select <part-tag>", NamedTextColor.GRAY));
    }

    static void invalidTag(Player player, String tag){
        player.sendMessage(DisplayEntityPlugin.pluginPrefix.append(Component.text("Failed to add tag: "+tag, NamedTextColor.RED)));
        invalidTagRestrictions(player);
    }

    static void invalidTagRestrictions(Player player){
        player.sendMessage(Component.text("Valid tags do not start with an \"!\" and do not contain commas.", NamedTextColor.GRAY, TextDecoration.ITALIC));
    }

    static void suggestUpdateSelection(Player player){
        player.sendMessage(Component.text("| It is recommended to update/reset your part selection after adding parts!", NamedTextColor.GRAY));
        player.sendMessage(Component.text("| Quickly reset with \"/mdis parts reselect", NamedTextColor.GRAY));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            mainCommandHelp(sender);
            return true;
        }
        String arg = args[0];
        SubCommand subCommand = subCommands.get(arg);
        if (subCommand == null){
            mainCommandHelp(sender);
        }
        else{
            executeCommand(subCommand, sender, args);
        }
        return true;
    }

    static void executeCommand(SubCommand subCommand, CommandSender sender, String[] args){
        if (!hasPermission(sender, subCommand.getPermission())){
            return;
        }
        if (subCommand instanceof ConsoleUsableSubCommand c){
            c.execute(sender, args);
        }
        else if (subCommand instanceof PlayerSubCommand c){
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Component.text("You cannot use this command in the console!", NamedTextColor.RED));
                return;
            }
            c.execute(p, args);
        }
    }

    public static boolean hasPermission(@NotNull CommandSender sender, @NotNull Permission permission){
        if (!sender.hasPermission(permission.getPermission())){
            sender.sendMessage(Component.text("You do not have permission to do that!", NamedTextColor.RED));
            return false;
        }
        return true;
    }


    static void mainCommandHelp(CommandSender sender){
        if (!hasPermission(sender, Permission.HELP)) {
            return;
        }
        sender.sendMessage(DisplayEntityPlugin.pluginPrefixLong);
        //sender.sendMessage(Component.text("Valid storage is \"local\", \"mongodb\", \"mysql\", and \"all\"", NamedTextColor.DARK_AQUA));
        CMDUtils.sendCMD(sender, "/mdis group");
        CMDUtils.sendCMD(sender, "/mdis anim");
        CMDUtils.sendCMD(sender, "/mdis parts");
        CMDUtils.sendCMD(sender, "/mdis item");
        CMDUtils.sendCMD(sender, "/mdis text");
        CMDUtils.sendCMD(sender, "/mdis interaction");
        CMDUtils.sendCMD(sender, "/mdis listgroups <storage> [page-number]");
        CMDUtils.sendCMD(sender, "/mdis listanims <storage> [page-number]");
        CMDUtils.sendCMD(sender, "/mdis bdengine", " (Import models from BDEngine or convert animations)");
        CMDUtils.sendCMD(sender, "/mdis reload <config | controllers>", " (To reload Local, MySQL or MongoDB config save options, the server must be restarted)");
    }
}

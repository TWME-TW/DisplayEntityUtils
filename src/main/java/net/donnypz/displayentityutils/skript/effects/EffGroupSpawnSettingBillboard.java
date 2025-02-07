package net.donnypz.displayentityutils.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.donnypz.displayentityutils.utils.DisplayEntities.GroupSpawnSettings;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Group Spawn Settings Billboard")
@Description("Set the billboard property of a group spawn setting")
@Examples({"set {_setting}'s billboard to vertical", "set {_setting}'s billboard to center for parts with tag \"pivotpart\""})
@Since("2.6.2")
public class EffGroupSpawnSettingBillboard extends Effect {
    static {
        Skript.registerEffect(EffGroupSpawnSettingBillboard.class,"(make|set) %groupspawnsetting%['s] billboard to %billboard% [for parts with tag[s] %strings%]");
    }

    Expression<GroupSpawnSettings> settings;
    Expression<Display.Billboard> billboard;
    Expression<String> tags;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        settings = (Expression<GroupSpawnSettings>) expressions[0];
        billboard = (Expression<Display.Billboard>) expressions[1];
        if (expressions.length == 3){
            tags = (Expression<String>) expressions[2];
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        GroupSpawnSettings s = settings.getSingle(event);
        Display.Billboard b = billboard.getSingle(event);
        if (tags == null){
            s.addBillboard(b, null);
        }
        else{
            for (String tag : tags.getArray(event)){
                s.addBillboard(b, tag);
            }
        }

    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "visible by default for spawn settings: "+settings.toString(event, debug);
    }
}

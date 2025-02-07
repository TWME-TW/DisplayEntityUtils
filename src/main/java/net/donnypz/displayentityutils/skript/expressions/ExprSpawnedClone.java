package net.donnypz.displayentityutils.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.skript.doc.Name;
import net.donnypz.displayentityutils.events.PreGroupSpawnedEvent;
import net.donnypz.displayentityutils.utils.DisplayEntities.GroupSpawnSettings;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimation;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Cloned Spawned Group/Animation")
@Description("Create a clone version of a spawned group/animation")
@Examples({"set {_groupclone} to a clone of {_spawnedgroup}",
        "",
        "#Create a clone at a certain location",
        "set {_groupclone} to a clone of {_spawnedgroup} at {_location}",
        "",
        "set {_animclone} to a clone of {_spawnedanimation}"})
@Since("2.6.2")
public class ExprSpawnedClone extends SimpleExpression<Object> {

    static{
        Skript.registerExpression(ExprSpawnedClone.class, Object.class, ExpressionType.SIMPLE, "[a] (clone[d version]|cop[y|ied version]) of %spawnedgroup/spawnedanimation% [loc:at %-location%]");
    }

    Expression<?> object;
    Expression<Location> location;

    @Override
    protected Object @Nullable [] get(Event event) {
        Object obj = object.getSingle(event);
        if (obj instanceof SpawnedDisplayEntityGroup g){
            if (location == null){
                return new SpawnedDisplayEntityGroup[]{g.clone(g.getLocation())};
            }
            else{
                Location loc = location.getSingle(event);
                if (loc == null){
                    return null;
                }
                return new SpawnedDisplayEntityGroup[]{g.clone(loc)};
            }

        }
        else if (obj instanceof SpawnedDisplayAnimation a){
            return new SpawnedDisplayAnimation[]{a.clone()};
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "group spawn setting";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        object = expressions[0];
        if (parseResult.hasTag("loc")){
            location = (Expression<Location>) expressions[1];
        }
        return true;
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET)
            return CollectionUtils.array(GroupSpawnSettings.class);
        return null;
    }

    @Override
    public void change(final Event e, final @Nullable Object[] delta, final Changer.ChangeMode mode) {
        if (e instanceof PreGroupSpawnedEvent ev && mode == Changer.ChangeMode.SET){
            ev.setGroupSpawnSettings((GroupSpawnSettings) delta[0]);
        }
    }
}

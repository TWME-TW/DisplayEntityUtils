package net.donnypz.displayentityutils.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityPart;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedPartSelection;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Spawned Group/Part/ Part Selection Interpolation")
@Description("Set the interpolation duration/delay of a spawned group / spawned part / part selection")
@Examples({"deu set interpolation duration of {_spawnedpart} to 5 ticks", "deu set interpolation delay of {_spawnedgroup} to 2 ticks"})
@Since("2.6.2")
public class EffSpawnedInterpolation extends Effect {
    static {
        Skript.registerEffect(EffSpawnedInterpolation.class,"[deu ]set interpolation (:duration|delay) of %spawnedparts/partselections/spawnedgroups% to %timespan%",
                "[deu]set %spawnedgroup/partselection/spawnedparts%'s interpolation (:duration|delay) to %timespan%");
    }

    Expression<?> object;
    Expression<Timespan> timespan;
    boolean duration;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        object = expressions[0];
        timespan = (Expression<Timespan>) expressions[1];
        duration = parseResult.hasTag("duration");
        return true;
    }

    @Override
    protected void execute(Event event) {
        Timespan ts = timespan.getSingle(event);
        if (ts == null) {
            return;
        }
        long v = ts.getAs(Timespan.TimePeriod.TICK);
        for (Object o : object.getArray(event)) {
            if (o instanceof SpawnedDisplayEntityPart part) {
                setPartData(part, v);
            } else if (o instanceof SpawnedDisplayEntityGroup group) {
                for (SpawnedDisplayEntityPart part : group.getSpawnedParts()) {
                    setPartData(part, v);
                }
            } else if (o instanceof SpawnedPartSelection sel) {
                for (SpawnedDisplayEntityPart part : sel.getSelectedParts()) {
                    setPartData(part, v);
                }
            }
        }
    }

    void setPartData(SpawnedDisplayEntityPart part, long value){
        if (part.getEntity() instanceof Display display){
            if (duration){
                display.setInterpolationDuration((int) value);
            }
            else{
                display.setInterpolationDelay((int) value);
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "set interpolation duration/delay: "+object.toString(event, debug);
    }
}

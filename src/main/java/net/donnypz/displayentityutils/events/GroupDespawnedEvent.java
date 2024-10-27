package net.donnypz.displayentityutils.events;

import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a {@link SpawnedDisplayEntityGroup} is unregistered.
 * Can be cancelled
 */
public class GroupDespawnedEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    SpawnedDisplayEntityGroup spawnedDisplayEntityGroup;

    private boolean isCancelled = false;

    public GroupDespawnedEvent(SpawnedDisplayEntityGroup group){
        this.spawnedDisplayEntityGroup = group;
    }

    /**
     * Get the {@link SpawnedDisplayEntityGroup} involved in this event
     * @return a group
     */
    public SpawnedDisplayEntityGroup getGroup() {
        return spawnedDisplayEntityGroup;
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

}

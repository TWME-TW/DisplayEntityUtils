package net.donnypz.displayentityutils.events;

import net.donnypz.displayentityutils.utils.DisplayEntities.DisplayAnimator;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimation;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimationFrame;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a {@link SpawnedDisplayAnimationFrame} ends in a {@link SpawnedDisplayAnimation}
 * This ignores the frame's delay and is called after translation of parts in the group.
 */
public class GroupAnimateFrameEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();


    private SpawnedDisplayEntityGroup spawnedDisplayEntityGroup;
    private SpawnedDisplayAnimation animation;
    private SpawnedDisplayAnimationFrame frame;
    private DisplayAnimator animator;

    public GroupAnimateFrameEndEvent(SpawnedDisplayEntityGroup group, DisplayAnimator animator, SpawnedDisplayAnimation animation, SpawnedDisplayAnimationFrame frame){
        this.spawnedDisplayEntityGroup = group;
        this.animation = animation;
        this.frame = frame;
        this.animator = animator;
    }

    /**
     * Get the {@link SpawnedDisplayEntityGroup} involved in this event
     * @return a group
     */
    public SpawnedDisplayEntityGroup getGroup() {
        return spawnedDisplayEntityGroup;
    }

    /**
     * Get the {@link SpawnedDisplayAnimation} involved in this event
     * @return a SpawnedDisplayAnimation
     */
    public SpawnedDisplayAnimation getAnimation() {
        return animation;
    }

    /**
     * Get the {@link DisplayAnimator} involved in this event
     * @return a DisplayAnimator. Null if the group animated without creating a DisplayAnimator
     */
    public @Nullable DisplayAnimator getAnimator() {
        return animator;
    }

    /**
     * Get the {@link SpawnedDisplayAnimationFrame} involved in this event
     * @return a frame
     */
    public SpawnedDisplayAnimationFrame getFrame() {
        return frame;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

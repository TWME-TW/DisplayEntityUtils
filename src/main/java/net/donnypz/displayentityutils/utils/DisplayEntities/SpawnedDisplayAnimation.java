package net.donnypz.displayentityutils.utils.DisplayEntities;

import net.donnypz.displayentityutils.utils.DisplayUtils;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public final class SpawnedDisplayAnimation {
    String animationTag;
    List<SpawnedDisplayAnimationFrame> frames = new ArrayList<>();
    String partTag = null;
    boolean respectGroupScale = true;

    @ApiStatus.Internal
    public SpawnedDisplayAnimation(){}

    @ApiStatus.Internal
    public SpawnedDisplayAnimation(String partTag){
        this.partTag = partTag;
    }


    SpawnedDisplayAnimation(SpawnedDisplayEntityGroup group){
        SpawnedDisplayAnimationFrame frame = new SpawnedDisplayAnimationFrame(0, 0);
        Location gLoc = group.getLocation();
        for (SpawnedDisplayEntityPart part : group.spawnedParts.values()){
            if (part.isMaster()){
                continue;
            }
            if (part.getType() == SpawnedDisplayEntityPart.PartType.INTERACTION){
                Interaction i = (Interaction) part.getEntity();

                InteractionTransformation transform = new InteractionTransformation(DisplayUtils.getInteractionTranslation(i).toVector3f(), gLoc.getYaw(), gLoc.getPitch(), i.getInteractionHeight(), i.getInteractionWidth());
                frame.setInteractionTransformation(part, transform);
            }
            else{
                frame.setDisplayEntityTransformation(part, ((Display) part.getEntity()).getTransformation());
            }
        }
        addFrame(frame);
    }

    @ApiStatus.Experimental
    SpawnedDisplayAnimation(SpawnedDisplayEntityGroup group, String partTag){
        this.partTag = partTag;
        Location gLoc = group.getLocation();
        SpawnedDisplayAnimationFrame frame = new SpawnedDisplayAnimationFrame(0, 0);
        for (SpawnedDisplayEntityPart part : group.spawnedParts.values()){
            if (part.isMaster()){
                continue;
            }
            if (part.getType() == SpawnedDisplayEntityPart.PartType.INTERACTION){
                Interaction i = (Interaction) part.getEntity();

                InteractionTransformation transform = new InteractionTransformation(DisplayUtils.getInteractionTranslation(i).toVector3f(), gLoc.getYaw(), gLoc.getPitch(), i.getInteractionHeight(), i.getInteractionWidth());
                frame.setInteractionTransformation(part, transform);
            }
            else{
                frame.setDisplayEntityTransformation(part, ((Display) part.getEntity()).getTransformation());
            }
        }
        addFrame(frame);
    }

    /**
     * Get the tag that represents this animation
     * @return a string, null if not set
     */
    public @Nullable String getAnimationTag() {
        return animationTag;
    }

    /**
     * Set the tag that should represent this animation
     * @param animationTag the tag to represent this animation
     */
    public void setAnimationTag(String animationTag) {
        this.animationTag = animationTag;
    }

    /**
     * Get whether this animation is a part animation, and will only animate parts in a group with a certain part tag.
     * @return a boolean
     */
    @ApiStatus.Experimental
    public boolean isPartAnimation(){
        return partTag != null;
    }

    /**
     * Get the part tag this animation will animate
     * @return a string, null if not set and this is not a part animation
     */
    @ApiStatus.Experimental
    public @Nullable String getPartTag() {
        return partTag;
    }

    /**
     * Get a list of all the {@link SpawnedDisplayAnimationFrame}s within this animation
     * @return a list of {@link SpawnedDisplayAnimationFrame}
     */
    public List<SpawnedDisplayAnimationFrame> getFrames() {
        return new ArrayList<>(frames);
    }

    /**
     * Set the frames that should be contained within this animation
     * @param frames the frames this animation should contain
     */
    public void setFrames(List<SpawnedDisplayAnimationFrame> frames) {
        this.frames = frames;
    }

    /**
     * Add a frame to this SpawnedDisplayAnimation
     * @param frame the frame to add
     */
    public void addFrame(SpawnedDisplayAnimationFrame frame){
        if (frames.isEmpty()){
            frames.add(frame);
            return;
        }
        SpawnedDisplayAnimationFrame lastFrame = frames.getLast(); //Compare previous frame

        //Remove identical part changes
        for (UUID partUUID : lastFrame.displayTransformations.keySet()){
            Transformation oldT = lastFrame.displayTransformations.get(partUUID);
            Transformation newT = frame.displayTransformations.get(partUUID);
            if (oldT != null && newT != null){
                if (newT.equals(oldT)){ //Remove identical Display Entity changes
                    frame.displayTransformations.remove(partUUID);
                }
            }
        }

        for (UUID partUUID : lastFrame.interactionTransformations.keySet()){
            Vector3f oldV = lastFrame.interactionTransformations.get(partUUID);
            Vector3f newV = frame.interactionTransformations.get(partUUID);
            if (oldV != null && newV != null){
                if (newV.equals(oldV)){ //Remove identical Interaction changes
                    frame.interactionTransformations.remove(partUUID);
                }
            }
        }

        if (!frame.isEmptyFrame()){ //Changes still remain after frame size reduction
            frames.add(frame);
        }
        else{
            lastFrame.delay+=frame.delay+frame.duration;
        }

        /*if (!frames.isEmpty() && frames.getLast().equals(frame)){
            lastFrame.duration+=frame.duration;
            lastFrame.delay+= frame.delay;
            //SpawnedDisplayAnimationFrame newFrame = new SpawnedDisplayAnimationFrame(frame.delay, frame.duration);
            //frames.add(newFrame);
            //return newFrame;
        }*/

        //return frame;
    }

    /**
     * Add a frame to this {@link SpawnedDisplayAnimation} without any optimizations
     * @param frame the frame to add
     */
    public void forceAddFrame(SpawnedDisplayAnimationFrame frame){
        frames.add(frame);
    }


    /**
     * Remove a frame from this {@link SpawnedDisplayAnimation}
     * @param frame the frame to remove
     * @return true if the animation contained the provided frame
     */
    public boolean removeFrame(SpawnedDisplayAnimationFrame frame){
        return frames.remove(frame);
    }

    /**
     * Set whether this {@link SpawnedDisplayAnimation} should respect a {@link SpawnedDisplayEntityGroup}'s scale when animating
     * @param respect
     */
    public void groupScaleRespect(boolean respect){
        respectGroupScale = respect;
    }

    /**
     * Get whether this {@link SpawnedDisplayAnimation} respects a {@link SpawnedDisplayEntityGroup}'s scale
     * @return a boolean
     */
    public boolean groupScaleRespect(){
        return respectGroupScale;
    }

    /**
     * Remove all frames from the animation, making it essentially unusable.
     */
    public void remove(){
        for (SpawnedDisplayAnimationFrame frame : frames){
            frame.displayTransformations.clear();
            frame.interactionTransformations.clear();
        }
        frames.clear();
    }

    /**
     * Convert this animation to a serializable {@link DisplayAnimation}
     * @return a {@link DisplayAnimation}
     */
    public DisplayAnimation toDisplayAnimation(){
        DisplayAnimation anim = new DisplayAnimation();

        anim.animationTag = this.animationTag;
        anim.partTag = this.partTag;
        anim.respectGroupScale = this.respectGroupScale;
        for (SpawnedDisplayAnimationFrame frame : frames){
            anim.addFrame(frame.toDisplayAnimationFrame());
        }
        return anim;
    }

    /**
     * Reverse the order of all frames in this {@link SpawnedDisplayAnimation}
     */
    public void reverse(){
        Collections.reverse(frames);
    }

    /** Creates an animation with the same frames as
     * this one in a reversed order
     * @return The reversed SpawnedDisplayAnimation
     */
    public SpawnedDisplayAnimation getReversedAnimation(){
        SpawnedDisplayAnimation reversed = new SpawnedDisplayAnimation();
        for (int i = frames.size()-1; i > 0; i--){
            SpawnedDisplayAnimationFrame frame = frames.get(i);
            SpawnedDisplayAnimationFrame newFrame = new SpawnedDisplayAnimationFrame(frame.delay, frame.duration);
            newFrame.displayTransformations = new HashMap<>(frame.displayTransformations);
            newFrame.interactionTransformations = new HashMap<>(frame.interactionTransformations);
            reversed.addFrame(newFrame);
        }
        reversed.animationTag = this.animationTag;
        reversed.partTag = this.partTag;
        reversed.respectGroupScale = this.respectGroupScale;
        return reversed;
    }

    /**
     * Check if this animation has frames
     * @return true if this animation has frames
     */
    public boolean hasFrames(){
        return !frames.isEmpty();
    }

}

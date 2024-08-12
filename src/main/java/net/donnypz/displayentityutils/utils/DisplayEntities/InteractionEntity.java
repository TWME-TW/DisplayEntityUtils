package net.donnypz.displayentityutils.utils.DisplayEntities;


import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.utils.DisplayUtils;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

public final class InteractionEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 99L;

    ArrayList<String> partTags;
    ArrayList<String> commands;
    Vector3f vector;
    UUID partUUID;
    float height;
    float width;
    private byte[] persistentDataContainer = null;

    InteractionEntity(Interaction interaction){
        this.partTags = DisplayUtils.getPartTags(interaction);

        commands = DisplayUtils.getInteractionCommands(interaction);

        height = interaction.getInteractionHeight();
        width = interaction.getInteractionWidth();
        this.vector = DisplayUtils.getInteractionTranslation(interaction).toVector3f();
        this.partUUID = DisplayUtils.getPartUUID(interaction);

        try{
            persistentDataContainer = interaction.getPersistentDataContainer().serializeToBytes();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    Interaction createEntity(Location location, boolean isVisible){
        return location.getWorld().spawn(location, Interaction.class, spawn ->{
            spawn.setInteractionHeight(height);
            spawn.setInteractionWidth(width);
            spawn.setVisibleByDefault(isVisible);
            for (String partTag : partTags){
                spawn.addScoreboardTag(partTag);
            }
            if (partUUID != null){
                spawn.getPersistentDataContainer().set(DisplayEntityPlugin.partUUIDKey, PersistentDataType.STRING, partUUID.toString());
            }
            if (persistentDataContainer != null){
                try{
                    spawn.getPersistentDataContainer().readFromBytes(persistentDataContainer);
                }
                catch(IOException ignore){}
            }
        });
    }

     Vector getVector(){
        return Vector.fromJOML(vector);
    }

    /**
     * Get this InteractionEntity's part tag
     * @return This InteractionEntity's part tag. Null if it does not have one
     */
    public ArrayList<String> getPartTags() {
        return partTags;
    }

    /**
     * Get this InteractionEntity's height
     * @return InteractionEntity's height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Get this InteractionEntity's width
     * @return InteractionEntity's width
     */
    public float getWidth() {
        return width;
    }
}
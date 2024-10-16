package net.donnypz.displayentityutils.managers;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.utils.DisplayEntities.*;
import net.donnypz.displayentityutils.utils.DisplayEntities.particles.AnimationParticle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

public final class DisplayAnimationManager {

    private DisplayAnimationManager(){}
    private static final HashMap<UUID, SpawnedDisplayAnimation> selectedAnimation = new HashMap<>();
    private static final HashMap<String, SpawnedDisplayAnimation> cachedAnimations = new HashMap<>();

    /**
     * Set the selected SpawnedDisplayAnimation of a player to the specified animation
     * @param player Player to set the selection to
     * @param spawnedDisplayAnimation SpawnedDisplayAnimation to be set to the player
     */
    public static void setSelectedSpawnedAnimation(Player player, SpawnedDisplayAnimation spawnedDisplayAnimation){
        if (selectedAnimation.get(player.getUniqueId()) != null){
            SpawnedDisplayAnimation lastAnim = selectedAnimation.get(player.getUniqueId());
            if (lastAnim == spawnedDisplayAnimation){
                return;
            }

            //boolean otherPlayersHaveSelected = false;
            for (UUID uuid : selectedAnimation.keySet()){
                if (uuid != player.getUniqueId() && selectedAnimation.get(uuid) == lastAnim){
                    //otherPlayersHaveSelected = true;
                    break;
                }
            }

            /*if (!otherPlayersHaveSelected){
                lastAnim.remove();
            }*/
        }
        selectedAnimation.put(player.getUniqueId(), spawnedDisplayAnimation);
    }

    /**
     * Get the SpawnedDisplayAnimation a player has selected
     * @param player Player to get the animation of
     * @return The SpawnedDisplayAnimation that the player has selected. Null if player does not have a selection.
     */
    public static SpawnedDisplayAnimation getSelectedSpawnedAnimation(Player player) {
        return selectedAnimation.get(player.getUniqueId());
    }

    /**
     * Remove a player's SpawnedDisplayAnimation selection
     * @param player Player to remove selection from
     */
    public static void deselectSpawnedAnimation(Player player){
        selectedAnimation.remove(player.getUniqueId());
    }


    /**
     * Save a DisplayAnimation to a Data Location
     * @param loadMethod Storage where to save the DisplayAnimation
     * @param displayAnimation The group to be saved
     * @param saver Player who is saving the animation
     * @return boolean whether the save was successful
     */
    public static boolean saveDisplayAnimation(LoadMethod loadMethod, DisplayAnimation displayAnimation, @Nullable Player saver){
        if (displayAnimation.getAnimationTag() == null){
            return false;
        }
        switch(loadMethod){
            case LOCAL -> {
                if (DisplayEntityPlugin.isLocalEnabled()){
                    return LocalManager.saveDisplayAnimation(displayAnimation, saver);
                }
            }
            case MONGODB -> {
                if (DisplayEntityPlugin.isMongoEnabled()){
                    return MongoManager.saveDisplayAnimation(displayAnimation, saver);
                }
            }
            case MYSQL -> {
                if (DisplayEntityPlugin.isMYSQLEnabled()){
                    return MYSQLManager.saveDisplayAnimation(displayAnimation, saver);
                }
            }
        }
        return false;
    }

    /**
     * Delete a DisplayAnimation from a Data Location
     * @param loadMethod Storage where the DisplayAnimation is located
     * @param tag Tag of the animation to be deleted
     * @param deleter Player who is deleting the animation (Nullable)
     */
    public static void deleteDisplayAnimation(LoadMethod loadMethod, String tag, @Nullable Player deleter) {
        switch (loadMethod) {
            case LOCAL -> {
                LocalManager.deleteDisplayAnimation(tag, deleter);
            }
            case MONGODB -> {
                MongoManager.deleteDisplayAnimation(tag, deleter);
            }
            case MYSQL -> {
                MYSQLManager.deleteDisplayAnimation(tag, deleter);
            }
        }
    }


    /**
     * Get a {@link SpawnedDisplayAnimation} that is applied to a {@link SpawnedDisplayEntityGroup} when spawned, if
     * the group has a spawn animation set, and if {@link DisplayEntityPlugin#cacheAnimations()} is true.
     * <br><br>
     * If an animation is not already cached, an attempt will be made to retrieve it from all storage locations, and will cache it if found.
     * @return a {@link SpawnedDisplayAnimation}, or null if the animation is not cached and does not exist in any storage location.
     */
    public static SpawnedDisplayAnimation getSpawnAnimation(String tag){
        SpawnedDisplayAnimation cachedAnim = cachedAnimations.get(tag);
        if (cachedAnim != null){
            return cachedAnim;
        }

        DisplayAnimation anim = getAnimation(tag);
        if (anim == null){
            return null;
        }

        cachedAnim = anim.toSpawnedDisplayAnimation();
        if (DisplayEntityPlugin.cacheAnimations()){
            cachedAnimations.put(tag, cachedAnim);
        }

        return cachedAnim;
    }

    /**
     * Attempt to get a {@link DisplayAnimation} from all storage locations.
     * Storage locations in the order of {@link LoadMethod#LOCAL}, {@link LoadMethod#MONGODB}, {@link LoadMethod#MYSQL}.
     * If a load method is disabled then it is skipped.
     * @param tag The tag of the {@link DisplayAnimation} to be retrieved
     * @return The found {@link DisplayAnimation}. Null if not found.
     */
    public static DisplayAnimation getAnimation(String tag){
        DisplayAnimation anim = getAnimation(LoadMethod.LOCAL, tag);
        if (anim == null){
            anim = getAnimation(LoadMethod.MONGODB, tag);
        }
        if (anim == null){
            anim = getAnimation(LoadMethod.MYSQL, tag);
        }
        return anim;
    }


    /**
     * Get a {@link DisplayAnimation} from a storage location.
     * @param loadMethod Where the {@link DisplayAnimation} is located
     * @param tag The tag of the {@link DisplayAnimation} to be retrieved
     * @return The found {@link DisplayAnimation}. Null if not found.
     */
    public static DisplayAnimation getAnimation(LoadMethod loadMethod, String tag){
        switch(loadMethod){
            case LOCAL ->{
                return LocalManager.retrieveDisplayAnimation(tag);
            }
            case MONGODB -> {
                return MongoManager.retrieveDisplayAnimation(tag);
            }
            case MYSQL -> {
                return MYSQLManager.retrieveDisplayAnimation(tag);
            }
        }
        return null;
    }

    /**
     * Get a {@link DisplayAnimation} from a saved file
     * @param file File of a saved {@link DisplayAnimation}
     * @return The found {@link DisplayAnimation}. Null if not found.
     */
    public static DisplayAnimation getAnimation(File file){
        try{
            return getAnimation(new FileInputStream(file));
        }
        catch(IOException ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get a {@link DisplayAnimation} from an input stream
     * @param inputStream InputStream containing a saved {@link DisplayAnimation}.
     * @return The found {@link DisplayAnimation}. Null if not found.
     */
    public static DisplayAnimation getAnimation(InputStream inputStream){
        byte[] bytes;
        try{
            bytes = inputStream.readAllBytes();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try{
            ByteArrayInputStream byteStream  = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteStream);

            ObjectInputStream objIn = new DisplayObjectInputStream(gzipInputStream);
            DisplayAnimation anim = (DisplayAnimation) objIn.readObject();

            objIn.close();
            gzipInputStream.close();
            byteStream.close();
            inputStream.close();
            for (DisplayAnimationFrame f : anim.getFrames()){
                for (AnimationParticle p : f.getFrameStartParticles()){
                    p.repair();
                }
                for (AnimationParticle p : f.getFrameEndParticles()){
                    p.repair();
                }
            }
            return anim;
        }
    //Not Compressed (Will typically be old file version)
        catch (ZipException z){
            try{
                ByteArrayInputStream byteStream  = new ByteArrayInputStream(bytes);
                ObjectInputStream objIn = new DisplayObjectInputStream(byteStream);
                DisplayAnimation anim = (DisplayAnimation) objIn.readObject();

                objIn.close();
                byteStream.close();
                inputStream.close();
                return anim;
            }
            catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        catch(IOException | ClassNotFoundException ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get a {@link DisplayAnimation} from a plugin's resources
     * @param plugin The plugin to get the {@link DisplayAnimation} from
     * @param resourcePath The path of the {@link DisplayAnimation}
     * @return The found {@link DisplayAnimation}. Null if not found.
     */
    public static DisplayAnimation getAnimation(JavaPlugin plugin, String resourcePath){
        InputStream modelStream;
        if (resourcePath.contains(DisplayAnimation.fileExtension)){
            modelStream = plugin.getResource(resourcePath);
        }
        else{
            modelStream = plugin.getResource(resourcePath+DisplayAnimation.fileExtension);
        }
        return getAnimation(modelStream);
    }


    /**
     * Get the animation tags of all saved {@link DisplayAnimation}s in a storage location.
     * @param loadMethod of the search location
     * @return a list of all animations by their group tag
     */
    public static List<String> getSavedDisplayAnimations(@NotNull LoadMethod loadMethod){
        switch(loadMethod){
            case LOCAL -> {
                return LocalManager.getDisplayAnimationTags();
            }
            case MONGODB -> {
                return MongoManager.getDisplayAnimationTags();
            }
            case MYSQL -> {
                return MYSQLManager.getDisplayAnimationTags();
            }
            default -> {
                return new ArrayList<>();
            }
        }
    }
    
}

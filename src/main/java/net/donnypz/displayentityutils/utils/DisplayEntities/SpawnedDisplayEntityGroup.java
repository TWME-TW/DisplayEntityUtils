package net.donnypz.displayentityutils.utils.DisplayEntities;

import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.events.*;
import net.donnypz.displayentityutils.managers.DisplayGroupManager;
import net.donnypz.displayentityutils.utils.Direction;
import net.donnypz.displayentityutils.utils.DisplayUtils;
import net.donnypz.displayentityutils.utils.FollowType;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;

public final class SpawnedDisplayEntityGroup {
    public static final long defaultPartUUIDSeed = 99;

    List<SpawnedDisplayEntityPart> spawnedParts = new ArrayList<>();
    List<SpawnedPartSelection> partSelections = new ArrayList<>();
    private String tag;
    SpawnedDisplayEntityPart masterPart;
    long creationTime = System.currentTimeMillis();
    public static final NamespacedKey creationTimeKey = new NamespacedKey(DisplayEntityPlugin.getInstance(), "creationtime");
    static final NamespacedKey scaleKey = new NamespacedKey(DisplayEntityPlugin.getInstance(), "scale");
    final Random partUUIDRandom = new Random(defaultPartUUIDSeed);
    boolean isVisibleByDefault;
    private String worldName;
    private float scaleMultiplier = 1;
    private UUID followedEntity = null;



    SpawnedDisplayEntityGroup(boolean isVisible, String worldName) {
        this.isVisibleByDefault = isVisible;
        this.worldName = worldName;
    }

    /**
     * Creates a SpawnedDisplayEntityGroup
     * @param masterDisplay
     * @apiNote This should NEVER have to be called manually, only do so if you know what you're doing
     */
    @ApiStatus.Internal
    public SpawnedDisplayEntityGroup(Display masterDisplay){
        this.isVisibleByDefault = masterDisplay.isVisibleByDefault();
        this.worldName = masterDisplay.getWorld().getName();
        PersistentDataContainer c = masterDisplay.getPersistentDataContainer();
        if (c.has(creationTimeKey)){
            creationTime = c.get(creationTimeKey, PersistentDataType.LONG);
        }
        if (c.has(scaleKey)){
            scaleMultiplier = c.get(scaleKey, PersistentDataType.FLOAT);
        }
        //String tag1;
        /*for (String tag: masterDisplay.getScoreboardTags()){
            if (tag != null && tag.contains(DisplayEntityPlugin.tagPrefix)){
                tag1 = tag;
                break;
            }
        }*/
        this.tag = DisplayUtils.getGroupTag(masterDisplay);
        addDisplayEntity(masterDisplay).setMaster();
        for(Entity entity : masterDisplay.getPassengers()){
            if (entity instanceof Display){
                addDisplayEntity((Display) entity);
            }
        }
        DisplayGroupManager.storeNewSpawnedGroup(this);
    }

    public long getCreationTime() {
        return creationTime;
    }

    public SpawnedDisplayEntityGroup addSpawnedDisplayEntityPart(SpawnedDisplayEntityPart part){
        part.setGroup(this);
        return this;
    }


    public SpawnedDisplayEntityPart addDisplayEntity(@Nonnull Display displayEntity){
        SpawnedDisplayEntityPart existing = SpawnedDisplayEntityPart.getPart(displayEntity);
        if (existing != null && existing.group != this){
            return existing.setGroup(this);
        }

        SpawnedDisplayEntityPart part = new SpawnedDisplayEntityPart(this, displayEntity, partUUIDRandom);
        if (masterPart != null){
            if (!part.isMaster()){
                masterPart.getEntity().addPassenger(displayEntity);
            }
            else if (!spawnedParts.isEmpty()){
                for (SpawnedDisplayEntityPart spawnedPart : spawnedParts){
                    if (!spawnedPart.getEntity().equals(part.getEntity())){
                        masterPart.getEntity().addPassenger(spawnedPart.getEntity());
                    }
                }
            }
        }

        return part;
    }




    public SpawnedDisplayEntityPart addInteractionEntity(@Nonnull Interaction interactionEntity){
        SpawnedDisplayEntityPart existing = SpawnedDisplayEntityPart.getPart(interactionEntity);
        if (existing == null){
            return new SpawnedDisplayEntityPart(this, interactionEntity, partUUIDRandom);
        }
        else{
            existing.setGroup(this);
            return existing;
        }
    }

    public SpawnedDisplayEntityPart addPartEntity(@Nonnull Entity entity){
        if (entity instanceof Interaction interaction){
            return addInteractionEntity(interaction);
        }
        else if (entity instanceof Display display){
            return addDisplayEntity(display);
        }
        else{
            return null;
        }
    }

    public boolean isPartOfGroup(Display display){
        PersistentDataContainer container = display.getPersistentDataContainer();
        if (!container.has(creationTimeKey, PersistentDataType.LONG)){
            return false;
        }

        return container.get(creationTimeKey, PersistentDataType.LONG) == creationTime;
    }

    public boolean isPartOfGroup(Interaction interaction){
        PersistentDataContainer container = interaction.getPersistentDataContainer();
        if (!container.has(creationTimeKey, PersistentDataType.LONG)){
            return false;
        }

        return container.get(creationTimeKey, PersistentDataType.LONG) == creationTime;
    }

    /**
     * Add Interactions that are meant to be a part of this group
     * Usually these Interactions are unadded when a SpawnedDisplayEntityGroup is created during a new play session
     * @return a list of the interaction entities added to the group
     */
    public List<Interaction> addMissingInteractionEntities(double distance){
        List<Interaction> interactions = new ArrayList<>();
        List<Entity> existingInteractions = getSpawnedPartEntities(SpawnedDisplayEntityPart.PartType.INTERACTION);

        for(Entity e : getMasterPart().getEntity().getNearbyEntities(distance, distance, distance)) {
            if ((e instanceof Interaction i)){
                if (!existingInteractions.contains(i) && isPartOfGroup(i)){
                    SpawnedDisplayEntityPart part = SpawnedDisplayEntityPart.getPart(i);
                    if (part == null){
                        new SpawnedDisplayEntityPart(this, (Interaction) e, partUUIDRandom);
                    }
                    else{
                        part.setGroup(this);
                    }
                    interactions.add((Interaction) e);
                }
            }
        }
        return interactions;
    }

    /**
     * Randomize the part uuids of all parts in this group with a given seed.
     * Useful when wanting to use the same animation on similar SpawnedDisplayEntityGroups.
     * Animations are not guaranteed to work properly if the order of parts are changed or if there is a difference in the number of parts.
     * @param seed The seed to use for the part randomization
     */
    public void seedPartUUIDs(long seed){
        byte[] byteArray;
        Random random = new Random(seed);
        for (SpawnedDisplayEntityPart part : spawnedParts){
            byteArray = new byte[16];
            random.nextBytes(byteArray);
            part.setPartUUID(UUID.nameUUIDFromBytes(byteArray));
        }
    }

    /**
     * Reveal a SpawnedDisplayEntityGroup that is spawned hidden (or hidden in another way) to a player
     * @param player The player to reveal this group to
     */
    public void showToPlayer(Player player){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            part.showToPlayer(player);
        }
    }

    /**
     * Hide a SpawnedDisplayEntityGroup that is spawned hidden (or hidden in another way) from a player
     * @param player The player to hide this group from
     */
    public void hideFromPlayer(Player player){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            part.hideFromPlayer(player);

        }
    }

    /**
     * Get whether this group is visible to players by default
     * If not, use showToPlayer() to reveal this group to the player
     * and hideFromPlayer() to hide it
     * @return a boolean value
     */
    public boolean isVisibleByDefault(){
        return isVisibleByDefault;
    }




    /**
     * Get Interactions that are not part of this SpawnedDisplayEntityGroup
     * @param distance Distance to serach for Interactions from the location of the master entity
     * @param addToGroup Whether to add the found Interactions to the group automatically
     * @return List of the found Interactions
     */
    public List<Interaction> getUnaddedInteractionEntitiesInRange(double distance, boolean addToGroup){
        if (distance <= 0){
            return new ArrayList<>();
        }
        List<Interaction> interactions = new ArrayList<>();
        if (getMasterPart() != null){
            List<Entity> existingInteractions = getSpawnedPartEntities(SpawnedDisplayEntityPart.PartType.INTERACTION);
            for(Entity e : getMasterPart().getEntity().getNearbyEntities(distance, distance, distance)) {
                if ((e instanceof Interaction interaction)){
                    if (!existingInteractions.contains(e)){
                        if (addToGroup){
                            addInteractionEntity(interaction);

                        }
                        interactions.add((Interaction) e);
                    }
                }
            }
        }
        return interactions;
    }

    /**
     * Remove all Interaction Entities that are part of this SpawnedDisplayEntityGroup
     * @return List of removed Interactions
     */
    public List<Interaction> removeInteractionEntities(){
        List<Interaction> interactions = new ArrayList<>();
        for (SpawnedDisplayEntityPart part : getSpawnedParts(SpawnedDisplayEntityPart.PartType.INTERACTION)){
            part.remove(false);
        }
        return interactions;
    }

    /**
     * Get the location of this SpawnedDisplayEntityGroup.
     * @return Location of this group's master part. Null if the group is not spawned
     */
    public Location getLocation(){
        if (!this.isSpawned()){
            return null;
        }
        return masterPart.entity.getLocation();
    }

    /**
     * Get a list of all this group's parts.
     * @return a list of all {@link SpawnedDisplayEntityPart} in this group
     */
    public List<SpawnedDisplayEntityPart> getSpawnedParts(){
        return new ArrayList<>(spawnedParts);
    }

    /**
     * Get a list of all parts of a certain type within this group.
     * @return a list of all {@link SpawnedDisplayEntityPart} in this group of a certain part type
     */
    public List<SpawnedDisplayEntityPart> getSpawnedParts(SpawnedDisplayEntityPart.PartType partType){
        List<SpawnedDisplayEntityPart> partList = new ArrayList<>();
        for (SpawnedDisplayEntityPart part : spawnedParts){
            if (partType == part.getType()){
                partList.add(part);
            }
        }
        return partList;
    }


    /**
     * Get a list of all display entity parts within this group
     * @return a list of only display entity {@link SpawnedDisplayEntityPart}
     */
    public List<SpawnedDisplayEntityPart> getSpawnedDisplayParts(){
        List<SpawnedDisplayEntityPart> partList = new ArrayList<>();
        for (SpawnedDisplayEntityPart part : spawnedParts){
            if (part.getType() != SpawnedDisplayEntityPart.PartType.INTERACTION){
                partList.add(part);
            }
        }
        return partList;
    }

    /**
     * Get a list of all display entity parts within this group
     * @return a list of all {@link SpawnedDisplayEntityPart} with a specified tag
     */
    public List<SpawnedDisplayEntityPart> getSpawnedParts(@NotNull String partTag){
        List<SpawnedDisplayEntityPart> partList = new ArrayList<>();
        for (SpawnedDisplayEntityPart part : spawnedParts){
            if (part.hasPartTag(partTag)){
                partList.add(part);
            }
        }
        return partList;
    }

    /**
     * Get a list of parts specified by a part type as entities
     * @return a list of entities
     */
    public List<Entity> getSpawnedPartEntities(SpawnedDisplayEntityPart.PartType partType){
        List<Entity> partList = new ArrayList<>();
        for (SpawnedDisplayEntityPart part : spawnedParts){
            if (partType == part.getType()){
                partList.add(part.getEntity());
            }
        }
        return partList;
    }


    /**
     * Make a player select this SpawnedDisplayEntityGroup
     * @param player The player to give the selection to
     * @return this
     */
    @ApiStatus.Internal
    public SpawnedDisplayEntityGroup addPlayerSelection(Player player){
        DisplayGroupManager.setSelectedSpawnedGroup(player, this);
        return this;
    }

    public float getScaleMultiplier(){
        return scaleMultiplier;
    }

    public boolean scale(float newScaleMultiplier, int durationInTicks){
        if (newScaleMultiplier <= 0){
            throw new IllegalArgumentException("New Scale Multiplier cannot be <= 0");
        }
        GroupScaleEvent event = new GroupScaleEvent(this, newScaleMultiplier, this.scaleMultiplier);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            return false;
        }
        for (SpawnedDisplayEntityPart part : spawnedParts){
        //Displays
            if (part.type != SpawnedDisplayEntityPart.PartType.INTERACTION){
                Display d = (Display) part.entity;
                Transformation transformation = d.getTransformation();

                //Reset Scale then multiply by newScaleMultiplier
                Vector3f scaleVector = transformation.getScale();
                scaleVector.x = (scaleVector.x/scaleMultiplier)*newScaleMultiplier;
                scaleVector.y = (scaleVector.y/scaleMultiplier)*newScaleMultiplier;
                scaleVector.z = (scaleVector.z/scaleMultiplier)*newScaleMultiplier;

                //Reset Translation then multiply by newScaleMultiplier
                Vector3f translationVector = transformation.getTranslation();
                translationVector.x = (translationVector.x/scaleMultiplier)*newScaleMultiplier;
                translationVector.y = (translationVector.y/scaleMultiplier)*newScaleMultiplier;
                translationVector.z = (translationVector.z/scaleMultiplier)*newScaleMultiplier;

                //Transformation newTransform = new Transformation(translationVector, transformation.getLeftRotation(), scaleVector, transformation.getRightRotation());
                if (!transformation.equals(d.getTransformation())){
                    d.setInterpolationDuration(durationInTicks);
                    d.setInterpolationDelay(-1);
                    d.setTransformation(transformation);
                }
            }
        //Interactions
            else{
                Interaction i = (Interaction) part.entity;


                //Reset Scale then multiply by newScaleMultiplier
                i.setInteractionHeight((i.getInteractionHeight()/scaleMultiplier)*newScaleMultiplier);
                i.setInteractionWidth((i.getInteractionWidth()/scaleMultiplier)*newScaleMultiplier);

                //Reset Translation then multiply by newScaleMultiplier
                Vector translationVector = DisplayUtils.getInteractionTranslation(i);
                if (translationVector == null){
                    continue;
                }
                Vector oldVector = new Vector(translationVector.getX(), translationVector.getY(), translationVector.getZ());
                translationVector.setX((translationVector.getX()/scaleMultiplier)*newScaleMultiplier);
                translationVector.setY((translationVector.getY()/scaleMultiplier)*newScaleMultiplier);
                translationVector.setZ((translationVector.getZ()/scaleMultiplier)*newScaleMultiplier);

                Vector moveVector = oldVector.subtract(translationVector);
                part.translate((float) moveVector.length(), durationInTicks, 0, moveVector);
            }

        }


        PersistentDataContainer pdc = masterPart.entity.getPersistentDataContainer();
        pdc.set(scaleKey, PersistentDataType.FLOAT, newScaleMultiplier);
        scaleMultiplier = newScaleMultiplier;
        return true;
    }

    /**
     * Change the actual location of all the SpawnedDisplayEntityParts with normal teleportation.
     * @param location The location to teleport this SpawnedDisplayEntityGroup
     * @param respectGroupDirection Whether to respect this group's pitch and yaw or the location's pitch and yaw
     * @return The success status of the teleport, false if the teleport was cancelled
     */
    public boolean teleport(Location location, boolean respectGroupDirection){
        GroupTranslateEvent event = new GroupTranslateEvent(this, GroupTranslateEvent.GroupTranslateType.TELEPORT, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            return false;
        }
        teleportWithoutEvent(location, respectGroupDirection);
        return true;
    }

    private void teleportWithoutEvent(Location location, boolean respectGroupDirection){
        Entity master = masterPart.getEntity();
        Location oldMasterLoc = master.getLocation().clone();
        if (respectGroupDirection){
            location.setPitch(oldMasterLoc.getPitch());
            location.setYaw(oldMasterLoc.getYaw());
        }

        master.teleport(location, TeleportFlag.EntityState.RETAIN_PASSENGERS);

        for (SpawnedDisplayEntityPart part : getSpawnedParts()){
            part.getEntity().setRotation(location.getYaw(), location.getPitch());
            if (part.getType() == SpawnedDisplayEntityPart.PartType.INTERACTION){
                Interaction interaction = (Interaction) part.getEntity();
                Vector vector = oldMasterLoc.toVector().subtract(interaction.getLocation().toVector());
                Location tpLocation = location.clone().subtract(vector);
                part.getEntity().teleport(tpLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS);
            }

        }
        worldName = location.getWorld().getName();
    }

    /**
     * Move the actual location of all the SpawnedDisplayEntityParts in this group through smooth teleportation.
     * Doing this multiple times in a short amount of time may bring unexpected results.
     * @param direction The direction to translate the group
     * @param distance How far the group should be translated
     * @param durationInTicks How long it should take for the translation to complete
     */
    public void teleportMove(Vector direction, double distance, int durationInTicks){
        Location destination = masterPart.getEntity().getLocation().clone().add(direction.clone().normalize().multiply(distance));
        GroupTranslateEvent event = new GroupTranslateEvent(this, GroupTranslateEvent.GroupTranslateType.TELEPORTMOVE, destination);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            return;
        }
        if (durationInTicks <= 0){
            durationInTicks = 1;
        }
        direction.normalize();
        double movementIncrement = distance/(double) durationInTicks;
        direction.multiply(movementIncrement);
        Entity master = masterPart.getEntity();

        new BukkitRunnable(){
            double currentDistance = 0;
            @Override
            public void run() {
                currentDistance+=Math.abs(movementIncrement);
                Location tpLoc = master.getLocation().clone().add(direction);
                teleportWithoutEvent(tpLoc, false);
                if (currentDistance >= distance){
                    new GroupTeleportMoveEndEvent(SpawnedDisplayEntityGroup.this, GroupTranslateEvent.GroupTranslateType.TELEPORTMOVE, destination).callEvent();
                    cancel();
                }
            }
        }.runTaskTimer(DisplayEntityPlugin.getInstance(), 0, 1);
    }

    /**
     * Move the actual location of all the SpawnedDisplayEntityParts in this group through smooth teleportation.
     * Doing this multiple times in a short amount of time may bring unexpected results.
     * @param direction The direction to translate the group
     * @param distance How far the group should be translated
     * @param durationInTicks How long it should take for the translation to complete
     */
    public void teleportMove(@Nonnull Direction direction, double distance, int durationInTicks){
        teleportMove(direction.getVector(masterPart), distance, durationInTicks);
    }

    /**
     * Get the locations this SpawnedDisplayEntityGroup would teleport to if it was translated with {@link #teleportMove(Direction, double, int)}
     * or {@link #teleportMove(Vector, double, int)}.
     * @param direction The direction the group would be moved
     * @param distance How far the group would be translated
     * @return A list of locations this group would teleport to
     */
    public List<Location> getTeleportMoveLocations(Vector direction, double distance, int durationInTicks){
        return getTeleportMoveLocations(direction, distance, durationInTicks, 1);
    }

    /**
     * Get the locations this SpawnedDisplayEntityGroup would teleport to if it was translated with {@link #teleportMove(Direction, double, int)}
     * or {@link #teleportMove(Vector, double, int)}.
     * @param direction The direction the group would be moved
     * @param distance How far the group would be translated
     * @param divisions Number of times the space should be divided (returning x times the number of locations)
     * @return A list of locations this group would teleport to
     */
    public List<Location> getTeleportMoveLocations(Vector direction, double distance, int durationInTicks, int divisions){
        if (durationInTicks <= 0){
            durationInTicks = 1;
        }
        direction.normalize();
        double movementIncrement = distance/(double) durationInTicks;
        movementIncrement/=divisions;
        direction.multiply(movementIncrement);
        Entity master = masterPart.getEntity();
        List<Location> locations = new ArrayList<>();
        Location loc = master.getLocation().clone();
        for (double currentDistance = 0; currentDistance <= distance; currentDistance+=Math.abs(movementIncrement)){
            locations.add(loc.clone());
            loc.add(direction);
        }
        return locations;
    }

    /**
     * Check if this group's master part has this specified scoreboard tag
     * @param tag The tag to check for
     * @return whether this group has the specified tag
     */
    public boolean hasScoreboardTag(String tag){
        return masterPart.entity.getScoreboardTags().contains(tag);
    }

    /**
     * Add a scoreboard tag to this group's master part
     * @param tag The tag to add
     */
    public void addScoreboardTag(String tag){
        if (hasScoreboardTag(tag)){
            return;
        }
        masterPart.entity.addScoreboardTag(tag);
    }

    /**
     * Remove a scoreboard tag from this group's master part
     * @param tag The tag to remove
     */
    public void removeScoreboardTag(String tag){
        if (!hasScoreboardTag(tag)){
            return;
        }
        masterPart.entity.removeScoreboardTag(tag);
    }

    /**
     * Change the yaw of this group
     * @param yaw The yaw to set for this group
     */
    public void setYaw(float yaw, boolean pivotInteractions){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            part.setYaw(yaw, pivotInteractions);
        }
    }

    /**
     * Change the pitch of this group
     * @param pitch The pitch to set for this group
     */
    public void setPitch(float pitch){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            part.setPitch(pitch);
        }
    }

    /**
     * Set the brightness of this group
     * @param brightness the brightness to set
     */
    public void setBrightness(Display.Brightness brightness){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            part.setBrightness(brightness);
        }
    }

    /**
     * Set the view range of this Spawned Display Entity Group
     * @param range The color to set
     */
    public void setViewRange(float range){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            part.setViewRange(range);
        }
    }

    /**
     * Set the glow color of this Spawned Display Entity Group
     * @param color The color to set
     */
    public void setGlowColor(Color color){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            part.setGlowColor(color);
        }
    }

    /**
     * Get the glow color of this Spawned Display Entity Group
     */
    public Color getGlowColor(){
        return ((Display) masterPart.getEntity()).getGlowColorOverride();
    }



    /**
     * Change the translation of all the SpawnedDisplayEntityParts in this group.
     * Parts that are Interaction entities will attempt to translate similar to Display Entities, through smooth teleportation.
     * Doing multiple translations on an Interaction entity at the same time may have unexpected results
     * @param distance How far the part should be translated
     * @param durationInTicks How long it should take for the translation to complete
     * @param direction The direction to translate the part
     */
    public void translate(@Nonnull Vector direction, float distance, int durationInTicks, int delayInTicks){
        Location destination = masterPart.getEntity().getLocation().clone().add(direction.clone().normalize().multiply(distance));
        GroupTranslateEvent event = new GroupTranslateEvent(this, GroupTranslateEvent.GroupTranslateType.VANILLATRANSLATE, destination);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            return;
        }
        for(SpawnedDisplayEntityPart part : spawnedParts){
            part.translate(distance, durationInTicks, delayInTicks, direction);
        }
    }

    /**
     * Change the translation of all the SpawnedDisplayEntityParts in this group.
     * Parts that are Interaction entities will attempt to translate similar to Display Entities, through smooth teleportation.
     * Doing multiple translations on an Interaction entity at the same time may have unexpected results
     * @param direction The direction to translate the part
     * @param distance How far the part should be translated
     * @param durationInTicks How long it should take for the translation to complete
     */
    public void translate(@Nonnull Direction direction, float distance, int durationInTicks, int delayInTicks){
        Location destination = masterPart.getEntity().getLocation().clone().add(direction.getVector(masterPart.getEntity()).normalize().multiply(distance));
        GroupTranslateEvent event = new GroupTranslateEvent(this, GroupTranslateEvent.GroupTranslateType.VANILLATRANSLATE, destination);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            return;
        }
        for(SpawnedDisplayEntityPart part : spawnedParts){
            part.translate(distance, durationInTicks, delayInTicks, direction);
        }
    }

    /**
     * Set this group's tag
     * @param tag What to set this group's tag to
     * @return this
     */
    public SpawnedDisplayEntityGroup setTag(String tag){
        if (tag == null){
            return this;
        }
        for (SpawnedDisplayEntityPart part : spawnedParts){
            Entity entity = part.getEntity();
            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            pdc.set(DisplayEntityPlugin.groupTagKey, PersistentDataType.STRING, tag);
        }
        this.tag = tag;
        return this;
    }


    /**
     * Get the name of this group's world
     * @return name of group's world
     */
    public String getWorldName(){
        return worldName;
    }

    /**
     * Get this group's tag
     * @return This group's tag. Null if it was not set
     */
    public @Nullable String getTag() {
        return tag;
    }

    /**
     * Get whether this group has a tag set
     * @return true if a tag has been set for this group
     */
    public boolean hasTag(){
        return tag != null;
    }



    /**
     * Get this group's master part
     * @return This group's master part. Null if it could not be found
     */
    public SpawnedDisplayEntityPart getMasterPart(){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            if (part.isMaster()){
                return part;
            }
        }
        return null;
    }

    /**
     * Check if a Display is the master part of this group
     * @param display The Display to check
     * @return Whether the display is the master part
     */
    public boolean isMasterPart(@Nonnull Display display){
        return display.getPersistentDataContainer().has(new NamespacedKey(DisplayEntityPlugin.getInstance(), "ismaster"), PersistentDataType.BOOLEAN);
    }



    /**
     * Adds the glow effect to all the parts in this group
     * @return this
     */
    public SpawnedDisplayEntityGroup glow(boolean ignoreUnglowable){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            if (ignoreUnglowable && (part.type == SpawnedDisplayEntityPart.PartType.INTERACTION || part.type == SpawnedDisplayEntityPart.PartType.TEXT_DISPLAY || part.getMaterial() == Material.AIR)){
                continue;
            }
            part.glow();
        }
        return this;
    }

    /**
     * Adds the glow effect to all the parts in this group
     * @param durationInTicks How long to highlight this selection
     * @return this
     */
    public SpawnedDisplayEntityGroup glow(long durationInTicks, boolean ignoreUnglowable){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            if (ignoreUnglowable && (part.type == SpawnedDisplayEntityPart.PartType.INTERACTION || part.type == SpawnedDisplayEntityPart.PartType.TEXT_DISPLAY || part.getMaterial() == Material.AIR)){
                continue;
            }
            part.glow(durationInTicks);
        }
        return this;
    }

    /**
     * Removes the glow effect from all the parts in this group
     * @return this
     */
    public SpawnedDisplayEntityGroup unglow(){
        for (SpawnedDisplayEntityPart part : spawnedParts){
            part.unglow();
        }
        return this;
    }

    /**
     * Put a SpawnedDisplayEntityGroup on top of an entity
     * Calls the GroupMountEntityEvent if successful
     * @param mount The entity for the SpawnedDisplayEntityGroup to ride
     * @return Whether the mount was successful or not
     */
    public boolean rideEntity(Entity mount){
        try{
            Entity masterEntity = masterPart.getEntity();
            GroupRideEntityEvent event = new GroupRideEntityEvent(this, mount);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()){
                return false;

            }
            mount.addPassenger(masterEntity);
            return true;
        }
        catch(NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Force the SpawnedDisplayEntityGroup to look in the same direction as a specified entity
     * @param entity The entity with the directions to follow
     * @param followType The follow type
     * @param unregisterOnEntityDeath Determines if this group should be despawned after the entity's death
     */
    public void followEntityDirection(@Nonnull Entity entity, @Nonnull FollowType followType, boolean unregisterOnEntityDeath, boolean pivotInteractions){
        followEntityDirection(entity, followType, unregisterOnEntityDeath, pivotInteractions, getTeleportDuration());
    }

    /**
     * Force the SpawnedDisplayEntityGroup to look in the same direction as a specified entity
     * @param entity The entity with the directions to follow
     * @param followType The follow type
     * @param unregisterOnEntityDeath Determines if this group should be despawned after the entity's death
     * @param teleportationDuration Set the teleportationDuration of all parts within this group
     */
    public void followEntityDirection(@Nonnull Entity entity, @Nonnull FollowType followType, boolean unregisterOnEntityDeath, boolean pivotInteractions, int teleportationDuration){
        if (!(entity instanceof LivingEntity) && followType == FollowType.BODY){
            throw new IllegalArgumentException("Only living entities can have a follow type of \"BODY\"");
        }

        if (teleportationDuration < 0){
            teleportationDuration = 0;
        }
        setTeleportDuration(teleportationDuration);
        followedEntity = entity.getUniqueId();
        SpawnedDisplayEntityGroup group = this;

        new BukkitRunnable(){
            @Override
            public void run() {
                if (!group.isSpawned() || followedEntity != entity.getUniqueId() || entity.isDead()){
                    if (entity.isDead() && unregisterOnEntityDeath){
                        group.unregister(true);
                    }
                    cancel();
                    return;
                }
                switch(followType){
                    case BODY-> {
                        LivingEntity e = (LivingEntity) entity;
                        group.setYaw(e.getBodyYaw(), pivotInteractions);
                    }
                    case PITCH_AND_YAW, PITCH, YAW -> {
                        if (followType == FollowType.PITCH || followType == FollowType.PITCH_AND_YAW){
                            group.setPitch(entity.getPitch());
                        }
                        if (followType == FollowType.YAW || followType == FollowType.PITCH_AND_YAW){
                            group.setYaw(entity.getYaw(), pivotInteractions);
                        }
                    }
                }

            }
        }.runTaskTimer(DisplayEntityPlugin.getInstance(), 0, 1);
    }

    /**
     * Stop following an entity's direction after using {@link SpawnedDisplayEntityGroup#followEntityDirection(Entity, FollowType, boolean, boolean)}
     * or {@link SpawnedDisplayEntityGroup#followEntityDirection(Entity, FollowType, boolean, boolean, int)}
     */
    public void stopFollowingEntity(){
        followedEntity = null;
    }

    /**
     * Get the entity being followed after using {@link SpawnedDisplayEntityGroup#followEntityDirection(Entity, FollowType, boolean, boolean)}
     * or {@link SpawnedDisplayEntityGroup#followEntityDirection(Entity, FollowType, boolean, boolean, int)}
     */
    public Entity getFollowedEntity(){
        return followedEntity != null ? Bukkit.getEntity(followedEntity) : null;
    }

    /**
     * Set the teleportation Duration of all SpawnedDisplayEntityParts in this SpawnedDisplayEntityGroup
     * Useful when using methods such as {@link #teleportMove(Vector, double, int)}, {@link #teleportMove(Direction, double, int)}, {@link #teleport(Location, boolean)}
     * , or {@link #followEntityDirection(Entity, FollowType, boolean, boolean, int)}
     * This makes the teleportation of the group visually smoother
     */
    public void setTeleportDuration(int teleportDuration){
        for (SpawnedDisplayEntityPart part : getSpawnedDisplayParts()){
            ((Display) part.getEntity()).setTeleportDuration(teleportDuration);
        }
    }

    /**
     * Get the teleport duration of this SpawnedDisplayEntityGroup
     * @return the group's teleport duration value
     */
    public int getTeleportDuration(){
        return ((Display) masterPart.getEntity()).getTeleportDuration();
    }


    /**
     * Put an Entity on top of an SpawnedDisplayEntityGroup
     * Calls the EntityMountGroupEvent when successful
     * @param passenger The entity to ride the SpawnedDisplayEntityGroup
     * @return Whether the mount was successful or not
     */
    public boolean addPassenger(Entity passenger){
        try{
            Entity masterEntity = masterPart.getEntity();
            EntityRideGroupEvent event = new EntityRideGroupEvent(this, passenger);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()){
                return false;
            }
            masterEntity.addPassenger(passenger);
            return true;
        }
        catch(NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if this SpawnedDisplayEntityGroup is mounted
     * to an entity
     * @param entity the entity to check
     * @return Whether this SpawnedDisplayEntityGroup is mounted to the entity or not
     */
    public boolean isMountedToEntity(Entity entity){
        return entity.getPassengers().contains(masterPart.getEntity());
    }

    /**
     * Merge the parts of two groups together into one group
     * @param mergingGroup the group to merge
     * @return This display entity group with the other group merged
     */
    public SpawnedDisplayEntityGroup merge(SpawnedDisplayEntityGroup mergingGroup){
        mergingGroup.masterPart.remove(true);
        for (SpawnedDisplayEntityPart part : mergingGroup.getSpawnedParts()){
            part.setGroup(this);
        }
        mergingGroup.removeAllPartSelections();
        mergingGroup.spawnedParts.clear();
        mergingGroup.unregister(false);
        return this;
    }


    /**Attempt to copy the transformations of one group to another.
     * Both groups must have the same parts (or same amount of display entity parts)
     * @param copyGroup the group to copy from
     */
    public void copyTransformation(SpawnedDisplayEntityGroup copyGroup){
        List<SpawnedDisplayEntityPart> displayParts = getSpawnedDisplayParts();
        List<SpawnedDisplayEntityPart> copyParts = copyGroup.getSpawnedDisplayParts();
        for (int i = 0; i < displayParts.size(); i++){
            if (copyParts.size() < i+1){
                return;
            }
            SpawnedDisplayEntityPart part = displayParts.get(i);
            SpawnedDisplayEntityPart copyPart = copyParts.get(i);
            part.setTransformation(((Display)copyPart.getEntity()).getTransformation());
        }
    }

    public void animate(@Nonnull SpawnedDisplayAnimation animation){
        DisplayAnimator.play(this, animation);
    }

    public void animateLooping(@Nonnull SpawnedDisplayAnimation animation){
        DisplayAnimator animator = new DisplayAnimator(animation, DisplayAnimator.AnimationType.LOOP);
        animator.play(this);
    }

    public void setToFrame(SpawnedDisplayAnimationFrame frame) {
        for (SpawnedDisplayEntityPart part : spawnedParts){
            if (part.getType() == SpawnedDisplayEntityPart.PartType.INTERACTION){
                Vector oldVector = DisplayUtils.getInteractionTranslation((Interaction) part.getEntity());
                if (oldVector == null){
                    continue;
                }

                Vector translationVector = frame.interactionTranslations.get(part.getPartUUID());
                if (translationVector == null){
                    continue;
                }

                if (oldVector.equals(translationVector)) {
                    continue;
                }

                Vector moveVector = oldVector.subtract(translationVector);
                part.translate((float) moveVector.length(), frame.duration, 0, moveVector);

            }
            else{
                Transformation transformation = frame.displayTransformations.get(part.getPartUUID());
                if (transformation == null){
                    continue;
                }

                Display display = ((Display) part.getEntity());

                if (display.getTransformation().equals(transformation)){ //Prevents jittering of parts
                    continue;
                }

                display.setInterpolationDelay(-1);
                display.setInterpolationDuration(frame.duration);
                display.setTransformation(transformation);
            }
        }
    }




    /**
     * Creates a copy of this SpawnedDisplayEntityGroup at a location
     * @param location Where to spawn the clone
     * @return Cloned SpawnedDisplayEntityGroup
     */
    public SpawnedDisplayEntityGroup clone(Location location){
        DisplayEntityGroup group = toDisplayEntityGroup();
        return group.spawn(location, GroupSpawnedEvent.SpawnReason.CLONE);
    }

    /**
     * Get a DisplayEntityGroup representative of this SpawnedDisplayEntityGroup
     * @return DisplayEntityGroup representing this
     */
    public DisplayEntityGroup toDisplayEntityGroup(){
        return new DisplayEntityGroup(this);
    }

    /**
     * Removes all part selections from this group and from any player(s) using this part selection.
     * ALL SpawnedPartSelections in this group will become unusable afterwards.
     */
    public void removeAllPartSelections(){
        for (SpawnedPartSelection selection : new ArrayList<>(partSelections)){
            selection.remove();
        }
    }


    /**
     * Removes a part selection from this group and from any player(s) using this part selection.
     * The SpawnedPartSelection will not be usable afterwards.
     * @param partSelection The part selection to remove
     */
    public void removePartSelection(SpawnedPartSelection partSelection){
        if (partSelections.contains(partSelection)){
            partSelections.remove(partSelection);
            partSelection.removeNoManager();
        }
    }

    /**
     * Removes all stored SpawnedPartSelections and SpawnedDisplayEntityParts
     * @param despawnParts Decides whether the parts should be despawned or not
     * This unregisters anything related to the group within the DisplayEntityUtils Plugin
     * This will be unusable as a SpawnedDisplayEntityGroup afterwards
     */
    public void unregister(boolean despawnParts){
        if (masterPart == null){
            return;
        }
        DisplayStateAnimator.unregisterStateAnimator(this);
        DisplayGroupManager.removeSpawnedGroup(this, despawnParts);

        masterPart = null;
        followedEntity = null;
    }

    public boolean isSpawned(){
        return DisplayGroupManager.isGroupSpawned(this);
    }
}
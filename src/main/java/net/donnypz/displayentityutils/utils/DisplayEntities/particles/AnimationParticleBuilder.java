package net.donnypz.displayentityutils.utils.DisplayEntities.particles;

import com.destroystokyo.paper.ParticleBuilder;
import net.donnypz.displayentityutils.DisplayEntityPlugin;
import net.donnypz.displayentityutils.managers.DisplayGroupManager;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayAnimationFrame;
import net.donnypz.displayentityutils.utils.DisplayEntities.SpawnedDisplayEntityGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

@ApiStatus.Internal
public class AnimationParticleBuilder extends ParticleBuilder{

    private static final HashMap<UUID, AnimationParticleBuilder> builders = new HashMap<>();

    Player player;
    SpawnedDisplayAnimationFrame frame;
    final boolean isStartAdd;
    Step step;
    int delayInTicks = 0;
    AnimationParticle editParticle = null;

    Component prefix = DisplayEntityPlugin.pluginPrefix;
    Component particleMSG = prefix.append(Component.text("Enter the name of the particle to use", NamedTextColor.YELLOW));
    Component amountMSG = prefix.append(Component.text("Enter the amount of particles to spawn", NamedTextColor.YELLOW));
    Component colorMSG = prefix.append(Component.text("Enter the color to set for the particle(s) and a particle size", NamedTextColor.YELLOW));
    Component colorTransitionMSG = prefix.append(Component.text("Enter the color transition to set for the particle(s) and a particle size", NamedTextColor.YELLOW));
    Component extraMSG = prefix.append(Component.text("Enter the extra value for the particle(s)", NamedTextColor.YELLOW));
    Component blockMSG = prefix.append(Component.text("Enter the block to use for the particle(s).\nType \"-held\" to use your held block item, \"-target\" for your targeted block, or the block's id.", NamedTextColor.YELLOW));
    Component itemMSG = prefix.append(Component.text("Enter the item to use for the particle(s)", NamedTextColor.YELLOW));
    Component offsetMSG = prefix.append(Component.text("Enter the x, y, and z offset for the particle(s)", NamedTextColor.YELLOW));
    Component delayMSG = prefix.append(Component.text("Enter the amount of delay (in ticks) before the particle should be shown", NamedTextColor.YELLOW));
    Component separatedMSG = Component.text("All values should be entered separated by spaces.", NamedTextColor.GRAY, TextDecoration.ITALIC);

    @ApiStatus.Internal
    public AnimationParticleBuilder(@NotNull Player player, @NotNull SpawnedDisplayAnimationFrame frame, boolean isStartAdd){
        super(Particle.FLAME);
        this.player = player;
        this.frame = frame;
        this.isStartAdd = isStartAdd;
        builders.put(player.getUniqueId(), this);
        advanceStep(Step.PARTICLE);
    }

    @ApiStatus.Internal
    public AnimationParticleBuilder(@NotNull Player player, boolean isStartAdd, AnimationParticle editParticle, Step step){
        super(Particle.FLAME);
        this.player = player;
        this.isStartAdd = isStartAdd;
        builders.put(player.getUniqueId(), this);
        advanceStep(step);
        this.editParticle = editParticle;
    }

    public void delay(int delayInTicks){
        this.delayInTicks = delayInTicks;
    }

    public void advanceStep(Step nextStep){
        if (editParticle != null){
            Player player = this.player;
            if (updateParticle()){
                player.sendMessage(prefix.append(Component.text("Particle Changes applied successfully!", NamedTextColor.GREEN)));
            }
            else{
                player.sendMessage(prefix.append(Component.text("Particle Changed failed to apply!", NamedTextColor.RED)));
            }
            return;
        }
        if (nextStep == null){
            SpawnedDisplayEntityGroup selectedGroup = DisplayGroupManager.getSelectedSpawnedGroup(player);
            if (selectedGroup == null){
                player.sendMessage(Component.text("Failed to create animation particle! You do not have a group selected!", NamedTextColor.RED));
                return;
            }
            else{
                build(selectedGroup, player.getLocation());
                player.sendMessage(prefix.append(Component.text("Successfully created an animation particle!", NamedTextColor.GREEN)));
            }
            remove();
            return;
        }

        this.step = nextStep;
        switch(nextStep){
            case PARTICLE -> {
                player.sendMessage(particleMSG);
            }
            case COUNT -> {
                player.sendMessage(amountMSG);
            }
            case COLOR -> {
                player.sendMessage(colorMSG);
                player.sendMessage(separatedMSG);
            }
            case COLOR_TRANSITION -> {
                player.sendMessage(colorTransitionMSG);
                player.sendMessage(separatedMSG);
            }
            case EXTRA -> {
                player.sendMessage(extraMSG);
            }
        //Block
            case BLOCK -> {
                player.sendMessage(blockMSG);
            }
        //Item
            case ITEM -> {
                player.sendMessage(itemMSG);
            }
            case OFFSETS -> {
                player.sendMessage(offsetMSG);
                player.sendMessage(separatedMSG);
            }
            case DELAY -> {
                player.sendMessage(delayMSG);
            }
        }
    }


    public boolean isBlockDataParticle(){
        return this.particle().getDataType().isAssignableFrom(BlockData.class);
    }

    public boolean isItemParticle(){
        return this.particle() == Particle.ITEM;
    }

    public boolean isDustOptionParticle(){
        return this.particle().getDataType().isAssignableFrom(Particle.DustOptions.class);
    }

    public boolean isDustTransitionParticle(){
        return this.particle() == Particle.DUST_COLOR_TRANSITION;
    }

    public Step getStep() {
        return step;
    }

    public static AnimationParticleBuilder getBuilder(Player player){
        return builders.get(player.getUniqueId());
    }

    public static void removeParticleBuilder(Player player){
        AnimationParticleBuilder builder = builders.get(player.getUniqueId());
        if (builder != null){
            builder.remove();
        }
    }

    void remove(){
        builders.remove(player.getUniqueId());
        editParticle = null;
        frame = null;
        player = null;
    }

    public AnimationParticle build(SpawnedDisplayEntityGroup group, Location targetLocation){
        AnimationParticle animParticle;
        if (isBlockDataParticle()){
            animParticle = new BlockAnimationParticle(this, data());
        }
        else if (isItemParticle()){
            animParticle = new ItemStackAnimationParticle(this, data());
        }
        else if (isDustOptionParticle()){
            animParticle = new DustOptionAnimationParticle(this, data());
        }
        else if (isDustTransitionParticle()){
            animParticle = new DustTransitionAnimationParticle(this, data());
        }
        else if (particle() == Particle.ENTITY_EFFECT){
            animParticle = new EntityEffectAnimationParticle(this, data());
        }
        else{
            animParticle = new GeneralAnimationParticle(this, particle());
        }
        Location groupLoc = group.getLocation();
        Vector v = targetLocation.toVector().subtract(groupLoc.toVector());

        animParticle.setVectorFromOrigin(v, groupLoc.getYaw(), groupLoc.getPitch());
        animParticle.setDelayInTicks(delayInTicks);
        if (isStartAdd){
            frame.addFrameStartParticle(animParticle);
        }
        else{
            frame.addFrameEndParticle(animParticle);
        }

        return animParticle;
    }

    private boolean updateParticle(){
        boolean result = editParticle.editParticle(this);
        remove();
        return result;
    }


    public enum Step{
        PARTICLE,
        COUNT,
        COLOR,
        COLOR_ENTITY_EFFECT,
        COLOR_TRANSITION,
        EXTRA,
        ITEM,
        BLOCK,
        OFFSETS,
        DELAY
    }
}

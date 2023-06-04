package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.InlineSerializer;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ChanceSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.PlayerEffectMechanicList;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.NumberUtil;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Mechanic is the most powerful tool available to server-admins through
 * config. A Mechanic is an action, basically a block of code, that the admin
 * writes in YAML format. These actions can be executed conditionally using
 * {@link Condition} and can get specifically targeted using {@link me.deecaad.core.mechanics.targeters.Targeter}.
 */
public abstract class Mechanic implements InlineSerializer<Mechanic> {

    // package-private so the serializer can set them phase
    public Targeter targeter;
    public List<Condition> conditions;
    private int repeatAmount;
    private int repeatInterval;
    private int delayBeforePlay;
    private double chance;

    /**
     * Default constructor for serializer.
     */
    public Mechanic() {
    }

    public Targeter getTargeter() {
        return targeter;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public int getRepeatAmount() {
        return repeatAmount;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public int getDelayBeforePlay() {
        return delayBeforePlay;
    }

    public double getChance() {
        return chance;
    }

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/Mechanics#mechanics";
    }

    /**
     * This method will trigger this Mechanic.
     *
     * <p>This method <i>probably shouldn't</i> be overridden. It handles the
     * global repeat, delay, targeter, and condition code. The behavior of the
     * specific Mechanic is handled by the protected {@link #use0(CastData)}
     * method.
     *
     * @param cast The non-null cast data.
     */
    public final void use(CastData cast) {

        // Chance to execute mechanic
        if (!NumberUtil.chance(chance))
            return;

        // If there is no need to schedule event, skip the event process.
        if (repeatAmount == 1 && repeatInterval == 1 && delayBeforePlay == 0) {
            handleTargetersAndConditions(cast);
            return;
        }

        // Schedule a repeating event to trigger the mechanic multiple times.
        int task = new BukkitRunnable() {
            int runs = 0;

            @Override
            public void run() {
                if (runs++ >= repeatAmount) {
                    cancel();
                    return;
                }

                handleTargetersAndConditions(cast);
            }
        }.runTaskTimer(MechanicsCore.getPlugin(), delayBeforePlay, repeatInterval - 1).getTaskId();

        // This allows developers to consume task ids from playing a Mechanic.
        // Good for canceling tasks early.
        if (cast.getTaskIdConsumer() != null)
            cast.getTaskIdConsumer().accept(task);
    }

    private void handleTargetersAndConditions(CastData cast) {

        // This Mechanic is a special Mechanic that stores a list of mechanics that
        // can have their targeters cached. This improves performance. Of course, that
        // means that this mechanic SHOULD NOT use targeters.
        if (this instanceof PlayerEffectMechanicList) {
            use0(cast);
            return;
        }

        OUTER:
        for (CastData target : targeter.getTargets(cast)) {
            for (Condition condition : conditions)
                if (!condition.isAllowed(target))
                    continue OUTER;

            use0(target);
        }
    }

    /**
     * This method should be overridden to define the behavior of the Mechanic.
     * For example, a Potion mechanic may use the {@link CastData#getTarget()}
     * method to apply a potion effect.
     *
     * @param cast The non-null data including source/target information.
     */
    protected abstract void use0(CastData cast);

    public Mechanic applyParentArgs(SerializeData data, Mechanic mechanic) throws SerializerException {
        mechanic.repeatAmount = data.of("Repeat_Amount").getInt(1);
        mechanic.repeatInterval = data.of("Repeat_Interval").getInt(1);
        mechanic.delayBeforePlay = data.of("Delay_Before_Play").getInt(0);
        Double chance = data.of("Chance").serialize(new ChanceSerializer());
        mechanic.chance = chance == null ? 1.0 : chance;
        return mechanic;
    }
}
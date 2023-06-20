package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class ProjectileSettings implements Serializer<ProjectileSettings>, Cloneable {

    private EntityType projectileDisguise;
    private Object disguiseData;

    private double gravity;

    private boolean removeAtMinimumSpeed;
    private double minimumSpeed;
    private boolean removeAtMaximumSpeed;
    private double maximumSpeed;

    private double decrease;
    private double decreaseInWater;
    private double decreaseWhenRainingOrSnowing;

    private boolean disableEntityCollisions;
    private int maximumAliveTicks;
    private double maximumTravelDistance;
    private double size;

    /**
     * Empty constructor to be used as serializer
     */
    public ProjectileSettings() {
    }

    public ProjectileSettings(EntityType projectileDisguise, Object disguiseData, double gravity,
                              boolean removeAtMinimumSpeed, double minimumSpeed, boolean removeAtMaximumSpeed, double maximumSpeed,
                              double decrease, double decreaseInWater, double decreaseWhenRainingOrSnowing, boolean disableEntityCollisions,
                              int maximumAliveTicks, double maximumTravelDistance, double size) {
        this.projectileDisguise = projectileDisguise;
        this.disguiseData = disguiseData;
        this.gravity = gravity;
        this.removeAtMinimumSpeed = removeAtMinimumSpeed;
        this.minimumSpeed = minimumSpeed;
        this.removeAtMaximumSpeed = removeAtMaximumSpeed;
        this.maximumSpeed = maximumSpeed;
        this.decrease = decrease;
        this.decreaseInWater = decreaseInWater;
        this.decreaseWhenRainingOrSnowing = decreaseWhenRainingOrSnowing;
        this.disableEntityCollisions = disableEntityCollisions;
        this.maximumAliveTicks = maximumAliveTicks;
        this.maximumTravelDistance = maximumTravelDistance;
        this.size = size;
    }

    /**
     * @return the entity type this projectile should be disguised as
     */
    @Nullable
    public EntityType getProjectileDisguise() {
        return this.projectileDisguise;
    }

    public void setProjectileDisguise(EntityType projectileDisguise) {
        this.projectileDisguise = projectileDisguise;
    }

    /**
     * Only certain entities need this. For example falling block, entity item and so on.
     * FALLING_BLOCK -> Material
     * ENTITY_ITEM -> ItemStack
     * FIREWORK -> ItemStack with FireworkMeta
     *
     * @return the item stack which may be used when spawning projectile disguise
     */
    @Nullable
    public Object getDisguiseData() {
        return disguiseData;
    }

    public void setDisguiseData(Object disguiseData) {
        this.disguiseData = disguiseData;
    }

    /**
     * @return gravity of projectile
     */
    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    /**
     * @return minimum speed of projectile
     */
    public double getMinimumSpeed() {
        return minimumSpeed;
    }

    public void setMinimumSpeed(double minimumSpeed) {
        this.minimumSpeed = minimumSpeed;
    }

    /**
     * @return whether to remove projectile when minimum speed is reached
     */
    public boolean isRemoveAtMinimumSpeed() {
        return this.removeAtMinimumSpeed;
    }

    public void setRemoveAtMinimumSpeed(boolean removeAtMinimumSpeed) {
        this.removeAtMinimumSpeed = removeAtMinimumSpeed;
    }

    /**
     * @return maximum speed of projectile
     */
    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    /**
     * @return whether to remove projectile when maximum speed is reached
     */
    public boolean isRemoveAtMaximumSpeed() {
        return this.removeAtMaximumSpeed;
    }

    public void setRemoveAtMaximumSpeed(boolean removeAtMaximumSpeed) {
        this.removeAtMaximumSpeed = removeAtMaximumSpeed;
    }

    /**
     * @return base speed decreasing
     */
    public double getDecrease() {
        return decrease;
    }

    public void setDecrease(double decrease) {
        this.decrease = decrease;
    }

    /**
     * @return speed decreasing in water
     */
    public double getDecreaseInWater() {
        return decreaseInWater;
    }

    public void setDecreaseInWater(double decreaseInWater) {
        this.decreaseInWater = decreaseInWater;
    }

    /**
     * @return speed decreasing when raining or snowing
     */
    public double getDecreaseWhenRainingOrSnowing() {
        return decreaseWhenRainingOrSnowing;
    }

    public void setDecreaseWhenRainingOrSnowing(double decreaseWhenRainingOrSnowing) {
        this.decreaseWhenRainingOrSnowing = decreaseWhenRainingOrSnowing;
    }

    /**
     * @return whether to skip entity collision checks
     */
    public boolean isDisableEntityCollisions() {
        return disableEntityCollisions;
    }

    public void setDisableEntityCollisions(boolean disableEntityCollisions) {
        this.disableEntityCollisions = disableEntityCollisions;
    }

    /**
     * @return the maximum amount of ticks projectile can be alive
     */
    public int getMaximumAliveTicks() {
        return maximumAliveTicks;
    }

    public void setMaximumAliveTicks(int maximumAliveTicks) {
        this.maximumAliveTicks = maximumAliveTicks;
    }

    /**
     * @return the maximum travel distance of projectile, -1 if not used
     */
    public double getMaximumTravelDistance() {
        return maximumTravelDistance;
    }

    public void setMaximumTravelDistance(double maximumTravelDistance) {
        this.maximumTravelDistance = maximumTravelDistance;
    }

    /**
     * @return the projectile size, 0.1 if not used
     */
    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    @Override
    public String getKeyword() {
        return "Projectile_Settings";
    }

    @Override
    @Nonnull
    public ProjectileSettings serialize(SerializeData data) throws SerializerException {

        String type = data.of("Type").assertExists().get().toString().trim().toUpperCase(Locale.ROOT);
        boolean isInvisible = type.equals("INVISIBLE");

        Object disguiseData = null;
        EntityType projectileType = null;

        if (!isInvisible) {
            projectileType = data.of("Type").assertExists().getEnum(EntityType.class);
            ItemStack projectileItem = data.of("Projectile_Item_Or_Block").serialize(new ItemSerializer());
            if ((projectileType == EntityType.DROPPED_ITEM
                    || projectileType == EntityType.FALLING_BLOCK)
                    && projectileItem == null) {

                throw data.exception(null, "When using " + projectileType + ", you MUST use Projectile_Item_Or_Block");
            }

            if (projectileItem != null) {
                if (projectileType == EntityType.FIREWORK && !(projectileItem.getItemMeta() instanceof FireworkMeta)) {

                    throw data.exception(null, "When using " + projectileType + ", the item must be a firework",
                            SerializerException.forValue(projectileItem));
                }

                if (projectileType == EntityType.FALLING_BLOCK) {
                    disguiseData = projectileItem.getType();
                } else {
                    disguiseData = projectileItem;
                }

                if (projectileType != EntityType.DROPPED_ITEM
                        && projectileType != EntityType.FALLING_BLOCK
                        && projectileType != EntityType.FIREWORK
                        && projectileType != EntityType.ARMOR_STAND) {
                    throw data.exception(null, "When using " + projectileType + ", you CAN'T use Projectile_Item_Or_Block",
                            SerializerException.forValue(projectileItem));
                }
            }
        }

        double gravity = 2.0 * data.of("Gravity").getDouble(10) / 400.0;

        // -1 so that CustomProjectile#tick() can understand that minimum or maximum speed isn't used
        double minimumSpeed = data.of("Minimum.Speed").assertPositive().getDouble(-20.0) / 20.0;
        boolean removeAtMinimumSpeed = data.of("Minimum.Remove_Projectile_On_Speed_Reached").getBool(false);
        double maximumSpeed = data.of("Maximum.Speed").assertPositive().getDouble(-20.0) / 20.0;
        boolean removeAtMaximumSpeed = data.of("Maximum.Remove_Projectile_On_Speed_Reached").getBool(false);

        double decrease = data.of("Drag.Base").assertRange(0.0, 3.0).getDouble(0.99);
        double decreaseInWater = data.of("Drag.In_Water").assertRange(0.0, 3.0).getDouble(0.96);
        double decreaseWhenRainingOrSnowing = data.of("Drag.When_Raining_Or_Snowing").assertRange(0.0, 3.0).getDouble(0.98);

        boolean disableEntityCollisions = data.of("Disable_Entity_Collisions").getBool(false);
        int maximumAliveTicks = data.of("Maximum_Alive_Ticks").assertPositive().getInt(600);
        double maximumTravelDistance = data.of("Maximum_Travel_Distance").assertPositive().getDouble(-1);
        double size = data.of("Size").assertPositive().getDouble(0.1);

        return new ProjectileSettings(projectileType, disguiseData, gravity, removeAtMinimumSpeed, minimumSpeed,
                removeAtMaximumSpeed, maximumSpeed, decrease, decreaseInWater, decreaseWhenRainingOrSnowing,
                disableEntityCollisions, maximumAliveTicks, maximumTravelDistance, size);
    }

    @Override
    public ProjectileSettings clone() {
        try {
            return (ProjectileSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
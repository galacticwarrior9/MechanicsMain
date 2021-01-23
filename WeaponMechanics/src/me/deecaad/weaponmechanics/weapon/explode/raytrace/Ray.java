package me.deecaad.weaponmechanics.weapon.explode.raytrace;

import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponcompatibility.projectile.HitBox;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Predicate;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Ray {

    private final Vector origin;
    private final Vector direction;
    private final double directionLength;
    private final World world;

    public Ray(@Nonnull Vector origin, @Nonnull Vector direction, @Nonnull World world) {
        this.origin = origin;
        this.direction = direction;
        this.directionLength = direction.length();
        this.world = world;

        this.direction.multiply(1.0 / directionLength);
    }

    /**
     * Gets the point on the ray "at" block away
     * from the origin.
     *
     * Note: If "at" > "directionLength", there is likely a logic error
     *
     * @param at How far away from the origin to get the vector
     * @return Vector at the point
     */
    private Vector getPoint(double at) {
        if (at > directionLength) debug.warn("at > directionLength in class Ray. at: " + at);

        return origin.clone().add(direction.clone().multiply(at));
    }

    public TraceResult trace(@Nonnull TraceCollision collision, @Nonnegative double accuracy) {
        return trace(collision, accuracy, null, null);
    }

    public TraceResult trace(@Nonnull TraceCollision collision,
                             @Nonnegative double accuracy,
                             @Nullable Predicate<Block> blockFilter,
                             @Nullable Predicate<Entity> entityFilter
    ) {

        Location loc = origin.toLocation(world);
        Map<Entity, HitBox> availableEntities = null;

        // Fill map with entities within radius
        if (collision.isEntity()) {
            availableEntities = new HashMap<>();

            for (Entity entity : world.getNearbyEntities(loc, directionLength, directionLength, directionLength)) {
                HitBox box = WeaponCompatibilityAPI.getProjectileCompatibility().getHitBox(entity);

                if (box == null) {
                    continue;
                } else if (entityFilter != null && !entityFilter.test(entity)) {
                    continue;
                }

                availableEntities.put(entity, box);
            }
        }

        // Check if the map is empty
        if (collision.isEntity() && !collision.isBlock() && availableEntities.isEmpty()) {
            return new TraceResult(new LinkedHashSet<>(0), null);
        }

        LinkedHashSet<Entity> entities = new LinkedHashSet<>();
        LinkedHashSet<Block> blocks = new LinkedHashSet<>();

        main:
        for (double i = 0; i < directionLength; i += accuracy) {

            Vector point = getPoint(i);

            // Only do block calculations if needed
            if (collision.isBlock()) {
                Block block = world.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ());

                // Filter out air blocks
                if (!block.isEmpty() && (blockFilter != null && !blockFilter.test(block))) {

                    // Check to see if the point is inside the block's hitbox
                    HitBox hitBox = WeaponCompatibilityAPI.getProjectileCompatibility().getHitBox(block);
                    if (contains(hitBox, point)) {
                        blocks.add(block);

                        // Break if the trace only needs to find 1 block
                        if (collision.isFirst()) {
                            break;
                        }
                    }
                }
            }

            // Only do entity calculations if needed
            if (collision.isEntity()) {
                for (Map.Entry<Entity, HitBox> entry: availableEntities.entrySet()) {
                    Entity entity = entry.getKey();
                    HitBox hitbox = entry.getValue();

                    if (contains(hitbox, point)) {
                        entities.add(entity);

                        if (collision.isFirst()) {
                            break main;
                        }
                    }
                }
            }
        }

        // Return the data
        return new TraceResult(entities, blocks);
    }

    private boolean contains(HitBox hitbox, Vector point) {
        if (hitbox == null) return false;

        double minX = hitbox.min.getX();
        double maxX = hitbox.max.getX();
        double minY = hitbox.min.getY();
        double maxY = hitbox.max.getY();
        double minZ = hitbox.min.getZ();
        double maxZ = hitbox.max.getZ();

        return point.getX() >= minX
                && point.getX() <= maxX
                && point.getY() >= minY
                && point.getY() <= maxY
                && point.getZ() >= minZ
                && point.getZ() <= maxZ;
    }
}

package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class WorldTargeter extends Targeter {

    private String worldName;
    private World worldCache;

    /**
     * Default constructor for serializer.
     */
    public WorldTargeter() {
    }

    public WorldTargeter(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public List<CastData> getTargets0(CastData cast) {
        if (worldCache == null || worldName == null)
            worldCache = worldName == null ? cast.getSource().getWorld() : Bukkit.getWorld(worldName);

        // User may have typed the name of the world wrong... It is case-sensitive
        if (worldCache == null) {
            MechanicsCore.debug.warn("There was an error getting the world for '" + worldName  + "'");
            return List.of();
        }

        // Loop through every living entity in the world
        List<CastData> targets = new LinkedList<>();
        for (LivingEntity target : worldCache.getPlayers()) {
            CastData copy = cast.clone();
            copy.setTargetEntity(target);
            targets.add(copy);
        }

        return targets;
    }

    @Override
    public String getKeyword() {
        return "World";
    }

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/WorldTargeter";
    }

    @NotNull
    @Override
    public Targeter serialize(SerializeData data) throws SerializerException {
        String worldName = data.of("World").assertType(String.class).get(null);
        return applyParentArgs(data, new WorldTargeter(worldName));
    }
}

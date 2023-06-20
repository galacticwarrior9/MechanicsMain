package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.Scope_1_20_R1;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class v1_20_R1 implements IWeaponCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 20) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + v1_20_R1.class + " when not using Minecraft 20",
                    new InternalError()
            );
        }
    }

    private final Set<RelativeMovement> RELATIVE_FLAGS = new HashSet<>(Arrays.asList(
            RelativeMovement.X,
            RelativeMovement.Y,
            RelativeMovement.Z,
            RelativeMovement.X_ROT,
            RelativeMovement.Y_ROT));

    private final Set<RelativeMovement> ABSOLUTE_FLAGS = new HashSet<>(Arrays.asList(
            RelativeMovement.X,
            RelativeMovement.Y,
            RelativeMovement.Z));

    private final IScopeCompatibility scopeCompatibility;

    public v1_20_R1() {
        this.scopeCompatibility = new Scope_1_20_R1();
    }

    @Nonnull
    @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility;
    }

    @Override
    public void modifyCameraRotation(Player player, float yaw, float pitch, boolean absolute) {
        pitch *= -1;
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundPlayerPositionPacket(0, 0, 0, yaw, pitch, absolute ? ABSOLUTE_FLAGS : RELATIVE_FLAGS, 0));
    }

    @Override
    public void logDamage(org.bukkit.entity.LivingEntity victim, org.bukkit.entity.LivingEntity source, double health, double damage, boolean isMelee) {
        DamageSources factory = ((CraftLivingEntity) source).getHandle().damageSources();
        DamageSource damageSource;

        if (isMelee) {
            if (source instanceof CraftPlayer player) {
                damageSource = factory.playerAttack(player.getHandle());
            } else {
                damageSource = factory.mobAttack(((CraftLivingEntity) source).getHandle());
            }
        } else {
            damageSource = factory.thrown(null, ((CraftLivingEntity) source).getHandle());
        }

        LivingEntity nms = ((CraftLivingEntity) victim).getHandle();
        nms.combatTracker.recordDamage(damageSource, (float) health);
        nms.setLastHurtByMob(((CraftLivingEntity) source).getHandle());
        if (source instanceof Player) nms.setLastHurtByPlayer(((CraftPlayer) source).getHandle());
    }

    @Override
    public void setKiller(org.bukkit.entity.LivingEntity victim, Player killer) {
        ((CraftLivingEntity) victim).getHandle().lastHurtByMob = ((CraftPlayer) killer).getHandle();
    }

    @Override
    public void playHurtAnimation(org.bukkit.entity.LivingEntity victim) {
        LivingEntity handle = ((CraftLivingEntity) victim).getHandle();
        handle.level().getServer().getPlayerList().broadcast(null, handle.getX(), handle.getY(), handle.getZ(), 50.0, handle.level().dimension(), new ClientboundHurtAnimationPacket(handle));
    }
}
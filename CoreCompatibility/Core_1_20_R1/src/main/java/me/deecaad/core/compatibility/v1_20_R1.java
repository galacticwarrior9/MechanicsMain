package me.deecaad.core.compatibility;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.compatibility.block.Block_1_20_R1;
import me.deecaad.core.compatibility.command.CommandCompatibility;
import me.deecaad.core.compatibility.command.Command_1_20_R1;
import me.deecaad.core.compatibility.entity.EntityCompatibility;
import me.deecaad.core.compatibility.entity.Entity_1_20_R1;
import me.deecaad.core.compatibility.nbt.NBTCompatibility;
import me.deecaad.core.compatibility.nbt.NBT_1_20_R1;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class v1_20_R1 implements ICompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 20) {
            MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + v1_20_R1.class + " when not using Minecraft 20",
                    new InternalError()
            );
        }
    }

    private final EntityCompatibility entityCompatibility;
    private final BlockCompatibility blockCompatibility;
    private final NBTCompatibility nbtCompatibility;
    private final CommandCompatibility commandCompatibility;

    public v1_20_R1() {
        entityCompatibility = new Entity_1_20_R1();
        blockCompatibility = new Block_1_20_R1();
        nbtCompatibility = new NBT_1_20_R1();
        commandCompatibility = new Command_1_20_R1();
    }

    @Override
    public Entity getEntityById(@NotNull World world, int entityId) {
        net.minecraft.world.entity.Entity e = ((CraftWorld) world).getHandle().getEntity(entityId);
        return e == null ? null : e.getBukkitEntity();
    }

    @Override
    public void sendPackets(Player player, Object packet) {
        getEntityPlayer(player).connection.send((Packet<?>) packet);
    }

    @Override
    public void sendPackets(Player player, Object... packets) {
        ServerGamePacketListenerImpl playerConnection = getEntityPlayer(player).connection;
        for (Object packet : packets) {
            playerConnection.send((Packet<?>) packet);
        }
    }

    @Override
    public @NotNull NBTCompatibility getNBTCompatibility() {
        return nbtCompatibility;
    }

    @Nonnull
    @Override
    public EntityCompatibility getEntityCompatibility() {
        return entityCompatibility;
    }

    @Nonnull
    @Override
    public BlockCompatibility getBlockCompatibility() {
        return blockCompatibility;
    }

    @NotNull
    @Override
    public CommandCompatibility getCommandCompatibility() {
        return commandCompatibility;
    }

    @Override
    public @NotNull ServerPlayer getEntityPlayer(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle();
    }
}
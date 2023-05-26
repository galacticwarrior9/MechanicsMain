package me.deecaad.core.compatibility.nbt;

import com.google.common.collect.Lists;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NBT_1_17_R1 extends NBT_Persistent {

    static {
        if (ReflectionUtil.getMCVersion() != 17) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + NBT_1_17_R1.class + " when not using Minecraft 17",
                    new InternalError()
            );
        }
    }

    @Override
    public void copyTagsFromTo(@NotNull ItemStack fromItem, @NotNull ItemStack toItem, @Nullable String path) {
        net.minecraft.world.item.ItemStack nms = getNMSStack(toItem);
        CompoundTag from = getNMSStack(fromItem).getTag();
        CompoundTag to = nms.getTag();

        if (path == null) {
            nms.setTag(from.copy());
            toItem.setItemMeta(nms.getBukkitStack().getItemMeta());
            return;
        }

        to.put(path, from.getCompound(path).copy());
        toItem.setItemMeta(nms.getBukkitStack().getItemMeta());
    }

    @Nonnull
    @Override
    public net.minecraft.world.item.ItemStack getNMSStack(@Nonnull ItemStack bukkitStack) {
        return CraftItemStack.asNMSCopy(bukkitStack);
    }

    @Nonnull
    @Override
    public ItemStack getBukkitStack(@Nonnull Object nmsStack) {
        return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack) nmsStack);
    }

    @Nonnull
    @Override
    public String getNBTDebug(@Nonnull ItemStack bukkitStack) {
        CompoundTag nbt = getNMSStack(bukkitStack).getTag();
        return nbt == null ? "null" : new TagColorVisitor().visit(nbt);
    }

    private static class TagColorVisitor extends StringTagVisitor {

        private static final String BRACE_COLORS = "f780"; // grayscale colors
        private static final String VALUE_COLORS = "6abcdef"; // bright colors
        private final StringBuilder builder;

        // Stores how many nested compound tags there currently are. Used to
        // determine curly brace color, as well as spacing.
        private final int indents;
        private final int colorOffset;

        public TagColorVisitor() {
            this(0, 0);
        }

        public TagColorVisitor(int indents, int colorOffset) {
            Field field = ReflectionUtil.getField(StringTagVisitor.class, StringBuilder.class);
            this.builder = (StringBuilder) ReflectionUtil.invokeField(field, this);
            this.indents = indents;
            this.colorOffset = colorOffset;
        }

        @Override
        public void visitCompound(CompoundTag compound) {
            String braceColor = "&" + BRACE_COLORS.charAt(indents % BRACE_COLORS.length());
            builder.append(braceColor).append("{\n");
            List<String> list = Lists.newArrayList(compound.getAllKeys());
            Collections.sort(list);

            for (int i = 0; i < list.size(); i++) {

                // Add a new line after each element, and indent each line
                // depending on the number of nested CompoundTags.
                if (i != 0)
                    builder.append('\n');

                builder.append(StringUtil.repeat("  ", indents));

                String key = list.get(i);
                Tag value = Objects.requireNonNull(compound.get(key), "This is impossible");
                String color = "&" + VALUE_COLORS.charAt((i + colorOffset) % VALUE_COLORS.length());

                builder.append(color).append(handleEscape(key))
                        .append("&f&l: ").append(color)
                        .append(new TagColorVisitor(value instanceof CompoundTag ? indents + 1 : indents, colorOffset + i).visit(value));
            }

            builder.append(braceColor).append("}\n");
        }
    }
}
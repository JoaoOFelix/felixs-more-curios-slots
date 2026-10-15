package com.felix.morecurios.slot;

import com.felix.morecurios.FelixMoreCurios;
import com.felix.morecurios.network.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/**
 * Stores, per player, how many times each crystal type was already used.
 *
 * Data lives inside the player's persistent data under Player.PERSISTED_NBT_TAG,
 * which Forge keeps across death, relog and server restarts.
 *
 * These counters are independent from the Curios inventory and from the configured limits:
 * changing the config or running /curios reset never edits them by itself.
 */
public final class PlayerSlotData {

    private static final String ROOT = FelixMoreCurios.MODID + "_extra_slots";

    private PlayerSlotData() {
    }

    /** How many times the player already used the crystal of this type. */
    public static int getUsed(Player player, CurioSlotType type) {
        return root(player).getInt(type.getSlotId());
    }

    public static void setUsed(Player player, CurioSlotType type, int value) {
        root(player).putInt(type.getSlotId(), Math.max(0, value));
    }

    /** Zeroes the counter of a single type. */
    public static void clear(Player player, CurioSlotType type) {
        root(player).remove(type.getSlotId());
    }

    /** Zeroes every counter of the player. */
    public static void clearAll(Player player) {
        persisted(player).remove(ROOT);
    }

    /** Copies the counters between two player instances (used on respawn / dimension clone). */
    public static void copy(Player from, Player to) {
        persisted(to).put(ROOT, root(from).copy());
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag forgeData = player.getPersistentData();
        if (!forgeData.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            forgeData.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return forgeData.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static CompoundTag root(Player player) {
        CompoundTag persisted = persisted(player);
        if (!persisted.contains(ROOT, Tag.TAG_COMPOUND)) {
            persisted.put(ROOT, new CompoundTag());
        }
        return persisted.getCompound(ROOT);
    }
}
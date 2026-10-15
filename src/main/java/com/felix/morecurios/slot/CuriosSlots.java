package com.felix.morecurios.slot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.MultiANewArrayForm;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static net.minecraftforge.versions.forge.ForgeVersion.MOD_ID;

/** The only class that talks to the Curios API. */
public final class CuriosSlots {

    public enum GrowResult {
        SUCCESS,
        /** Player has no Curios inventory (capability missing). */
        NO_INVENTORY,
        /** require_existing_slot = true and the player does not have the slot. */
        SLOT_MISSING,
        /** The slot id is not registered in Curios, so it cannot be assigned. */
        SLOT_NOT_REGISTERED,
        /**
         * The slot id is registered, but nothing assigns it to players: it is not in the player's
         * slot list, and Curios can only grow slots that are in that list. Fix = datapack
         * (data/&lt;ns&gt;/curios/entities/*.json) or the mod that owns the slot.
         */
        NOT_ASSIGNED,
        /** Curios did not apply the growth. Nothing should be consumed. */
        FAILED
    }

    private CuriosSlots() {
    }

    /**
     * True if the player currently has at least one slot of this type.
     * A type that is still in the Curios map but has size 0 (e.g. after '/curios remove')
     * does NOT count as owned.
     */
    public static boolean hasSlotType(Player player, String slotId) {
        return getHandler(player)
                .map(handler -> hasSlots(handler.getCurios().get(slotId)))
                .orElse(false);
    }

    private static boolean hasSlots(ICurioStacksHandler stacks) {
        return stacks != null && stacks.getSlots() > 0;
    }

    /**
     * True if this slot id is registered in Curios (data/curios/curios/slots/<id>.json loaded).
     * Only meaningful after datapacks are loaded, so never call it during registration.
     *
     * In this Curios version CuriosApi.getSlots() takes no argument, so {@code client} is unused
     * (kept so callers do not have to change).
     */
    public static boolean isSlotRegistered(String slotId, boolean client) {
        return CuriosApi.getSlots().containsKey(slotId);
    }

    /** True if at least one item is in the curios:<slotId> item tag. */
    public static boolean hasItemsForSlot(String slotId) {
        ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
        if (tags == null) {
            return false;
        }
        TagKey<Item> key = TagKey.create(Registries.ITEM, new ResourceLocation("curios", slotId));
        return !tags.getTag(key).isEmpty();
    }

    /**
     * Permanently grows a slot type by {@code amount} for this player (server side).
     *
     * Curios can only grow slot types that are already in the player's slot list (the types that a
     * datapack/mod assigned to the player, even with size 0). It cannot add a type to that list at
     * runtime: {@code unlockSlotType} just calls {@code growSlotType}, which does nothing for a
     * missing type. That is why this mod ships data/felixmorecurios/curios/entities/player_slots.json.
     *
     * Success is only reported when the slot count really increased, so a crystal is never
     * consumed for nothing.
     */
    @SuppressWarnings("removal") // growSlotType is deprecated in newer Curios, but it is the API that works here
    public static GrowResult growSlot(ServerPlayer player, String slotId, int amount, boolean requireExistingSlot) {
        Optional<ICuriosItemHandler> opt = getHandler(player);
        if (opt.isEmpty()) {
            return GrowResult.NO_INVENTORY;
        }
        ICuriosItemHandler handler = opt.get();

        ICurioStacksHandler stacks = handler.getCurios().get(slotId);
        int before = stacks == null ? 0 : stacks.getSlots();

        if (before <= 0) {
            // Owns none: not in the player's list at all, or in it with size 0.
            if (requireExistingSlot) {
                return GrowResult.SLOT_MISSING;
            }
            if (!isSlotRegistered(slotId, false)) {
                return GrowResult.SLOT_NOT_REGISTERED;
            }
            if (stacks == null && !slotId.equals("curio")) {
                return GrowResult.NOT_ASSIGNED;
            }
        }
        Multimap<String, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(slotId, new AttributeModifier(
                MOD_ID,
                amount,
                AttributeModifier.Operation.ADDITION
        ));
        handler.addPermanentSlotModifiers(modifiers);
        //player.sendSystemMessage(Component.literal(String.valueOf(slotId))); DEBUG
        return slotCount(player, slotId) > before ? GrowResult.SUCCESS : GrowResult.FAILED;
    }

    /** Number of slots of this type the player has right now (0 if the type is not in the list). */
    private static int slotCount(Player player, String slotId) {
        return getHandler(player)
                .map(handler -> {
                    ICurioStacksHandler stacks = handler.getCurios().get(slotId);
                    return stacks == null ? 0 : stacks.getSlots();
                })
                .orElse(0);
    }

    /** Resets the player's Curios inventory to the defaults (removes extra slots). */
    public static boolean resetInventory(ServerPlayer player) {
        Optional<ICuriosItemHandler> handler = getHandler(player);
        handler.ifPresent(ICuriosItemHandler::reset);
        return handler.isPresent();
    }

    private static Optional<ICuriosItemHandler> getHandler(Player player) {
        return player.getCapability(CuriosCapability.INVENTORY).resolve();
    }
}
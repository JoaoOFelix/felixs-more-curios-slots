package com.felix.morecurios.slot;

import com.felix.morecurios.FelixMoreCurios;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

/**
 * Every Curios slot type that has a crystal.
 *
 * slotId        = the Curios slot identifier, EXACTLY as the mod registers it
 *                 (data/curios/curios/slots/<id>.json). ring != rings, belt != waist.
 * itemName      = registry name of the crystal item
 * requiredModId = optional hard filter on a mod id (null = no mod check).
 *                 Availability of the slot itself is decided at runtime by
 *                 CrystalAvailability (slot registered in Curios, optionally with items),
 *                 so mod ids are only filled in when they are certain.
 *
 * The usage limit is NOT stored here: edit the per-slot defaults in SlotLimits.BUILT_IN_LIMITS
 * (a config number still overrides them, see FelixMoreCuriosConfig).
 *
 * To support a new slot: add an entry here, a texture, an item model, the lang keys
 * (slot.felixmorecurios.<id> and item.felixmorecurios.<itemName>) and a recipe.
 * The config entry (limits.per_slot.<id>) is generated automatically.
 */
public enum CurioSlotType {


    /** PARA ADICIONAR UM NOVO SLOT ADICIONE AQUI, NO DATA/FELIXMORECURIOS/CURIOS/ENTITIES/MODDED_SLOTS.JSON */
    /** E CRIE UM ARQUIVO COM O NOME DO SLOTID EM: DATA/FELIXMORECURIOS/CURIOS/SLOTS/MODID.JSON */
    /** ALEM DE CRIAR OS JSONs                                                                  */
    /**                                                                                         */

    CURIO("curio", "curio_slot_crystal", null, false),
    CHARM("charm", "charm_slot_crystal", null, false),
    HEAD("head", "head_slot_crystal", null, false),
    BACK("back", "back_slot_crystal", null, false),
    NECKLACE("necklace", "necklace_slot_crystal", null, false),
    RING("ring", "ring_slot_crystal", null, false),
    BRACELET("bracelet", "bracelet_slot_crystal", null, false),
    BELT("belt", "belt_slot_crystal", null, false),
    BODY("body", "body_slot_crystal", null, false),
    HANDS("hands", "hands_slot_crystal", null, false),
    // Feet is not a default Curios slot, but it is common across mods, so isModded is set to false.
    FEET("feet", "feet_slot_crystal", null, false),

    // OTHER MODS CURIOS SLOTS (already in the project)
    SPELLBOOK("spellbook", "spellbook_slot_crystal", null, true),
    ARS_FOCUS("an_focus", "ars_focus_slot_crystal", null, true),
    DEEP_LEARNER("deep_learner", "deep_learner_slot_crystal", null, true),
    ADV_PATTERN_ENCODER("adv_pattern_encoder", "adv_pattern_encoder_slot_crystal", null, true), // Advanced AE
    BUNDLE("bundle", "bundle_slot_crystal", null, true), // Ars Elemental
    CRAFTING_ON_A_STICK("crafting_on_a_stick", "crafting_on_a_stick_slot_crystal", null, true), // Crafting on a Stick
    HEART_AMULET("heart_amulet", "heart_amulet_slot_crystal", null, true), // Baubley Heart Canisters

    // ONLY IN 1.21
    //REFINED_STORAGE("refinedstorage_curios_integration", "refinedstorage_slot_crystal", null, true), // RS Curios Integration
    PSD("psd", "psd_slot_crystal", null, true), // Compact Machines
    RINGS("rings", "rings_slot_crystal", null, true), // Ender Cataclysm
    SENTIENT_ARMOUR_SOCKET("sentient_armour_socket", "sentient_armour_socket_slot_crystal", null, true), // Neo Vitae
    SHOULDERS("shoulders", "shoulders_slot_crystal", null, true), // Extended Industrialization
    TALISMAN("talisman", "talisman_slot_crystal", null, true), // Ender Cataclysm
    WAIST("waist", "waist_slot_crystal", null, true); // Ender Cataclysm                      // Ender Cataclysm

    private final String slotId;
    private final String itemName;
    private final String requiredModId;
    private final boolean isModded;

    CurioSlotType(String slotId, String itemName, String requiredModId, boolean isModded) {
        this.slotId = slotId;
        this.itemName = itemName;
        this.requiredModId = requiredModId;
        this.isModded = isModded;
    }

    public String getSlotId() {
        return slotId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getRequiredModId() {
        return requiredModId;
    }

    /** Effective limit from the config (individual > global). 0 = disabled. */
    public int getMaxExtraSlots() {
        return SlotLimits.getLimit(this);
    }

    /** True when this crystal has no mod requirement, or the required mod is loaded. */
    public boolean isModAvailable() {
        return requiredModId == null || ModList.get().isLoaded(requiredModId);
    }

    /** True when the curios slot is not from curios default slots. */
    public boolean isModded() {return isModded;}

    public Component getDisplayName() {
        return Component.translatable("slot." + FelixMoreCurios.MODID + "." + slotId);
    }
}

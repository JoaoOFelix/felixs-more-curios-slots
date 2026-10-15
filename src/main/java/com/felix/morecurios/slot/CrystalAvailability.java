package com.felix.morecurios.slot;

import com.felix.morecurios.config.FelixMoreCuriosConfig;
import com.felix.morecurios.config.FelixMoreCuriosConfig.AvailabilityMode;

/**
 * Decides whether a crystal makes sense in the installed environment.
 * All crystal items are ALWAYS registered (registries must match between client and server,
 * and slot/tag data is not loaded yet at registration time). Availability is therefore a
 * runtime rule used to (a) block use, (b) hide the crystal from the creative tab / tooltips.
 */
public final class CrystalAvailability {

    private CrystalAvailability() {
    }

    /** @param client true when called from client code (tooltip, creative tab). */
    public static boolean isAvailable(CurioSlotType type, boolean client) {
        AvailabilityMode availabilityMode = currentAvailabilityMode();


        //SlotMode slotMode = currentSlotMode();
        /* Slots personalizados de mods podem ser desativados.
        if (type.isModded() && slotMode == SlotMode.DISABLED) {
            return false;
        } */


        // Exige que o mod associado esteja disponível, quando aplicável.
        if (availabilityMode == AvailabilityMode.MOD_REGISTERED) {
            return type.isModAvailable();
        }

        // Os modos restantes exigem que o tipo de slot esteja registrado.
        if (!CuriosSlots.isSlotRegistered(type.getSlotId(), client)) {
            return false;
        }

        // Exige que exista pelo menos um item na tag curios:<slot>.
        if (availabilityMode == AvailabilityMode.SLOT_REGISTERED_AND_USED) {
            return CuriosSlots.hasItemsForSlot(type.getSlotId());
        }

        // SLOT_REGISTERED: basta o tipo de slot estar registrado.
        return true;
    }



    private static AvailabilityMode currentAvailabilityMode() {
        try {
            return FelixMoreCuriosConfig.AVAILABILITY_MODE.get();
        } catch (IllegalStateException notLoaded) {
            return AvailabilityMode.SLOT_REGISTERED;
        }
    }
    /*
    private static SlotMode currentSlotMode() {
        try {
            return FelixMoreCuriosConfig.SLOT_MODE.get();
        } catch (IllegalStateException notLoaded) {
            return SlotMode.ONLY_IF_MOD_LOADED;
        }
    } */
}
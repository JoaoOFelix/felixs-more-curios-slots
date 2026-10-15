package com.felix.morecurios.registry;

import com.felix.morecurios.FelixMoreCurios;
import com.felix.morecurios.item.SlotCrystalItem;
import com.felix.morecurios.slot.CurioSlotType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FelixMoreCurios.MODID);

    private static final Map<CurioSlotType, RegistryObject<Item>> CRYSTALS = new EnumMap<>(CurioSlotType.class);

    // Every item is always registered (keeps registries identical between clients and servers).
    // Items tied to another mod are only hidden from the creative tab / have a conditional recipe.
    static {
        for (CurioSlotType type : CurioSlotType.values()) {
            CRYSTALS.put(type, ITEMS.<Item>register(type.getItemName(),
                    () -> new SlotCrystalItem(type, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON))));
        }
    }

    private ModItems() {
    }

    public static RegistryObject<Item> get(CurioSlotType type) {
        return CRYSTALS.get(type);
    }
}

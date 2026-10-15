package com.felix.morecurios;

import com.felix.morecurios.config.FelixMoreCuriosConfig;
import com.felix.morecurios.network.ModNetwork;
import com.felix.morecurios.registry.ModCreativeTabs;
import com.felix.morecurios.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Felix's More Curios Slots
 *
 * Consumable crystals that permanently unlock extra Curios slots, per player,
 * with a configurable limit for each slot type (see SlotLimits).
 */
@Mod(FelixMoreCurios.MODID)
public class FelixMoreCurios {

    public static final String MODID = "felixmorecurios";

    public FelixMoreCurios() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, FelixMoreCuriosConfig.SPEC, "felixmorecurios.toml");
        ModNetwork.register();
    }
}

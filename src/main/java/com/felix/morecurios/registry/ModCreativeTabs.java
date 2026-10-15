package com.felix.morecurios.registry;

import com.felix.morecurios.FelixMoreCurios;
import com.felix.morecurios.slot.CurioSlotType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FelixMoreCurios.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + FelixMoreCurios.MODID))
                    .icon(() -> new ItemStack(ModItems.get(CurioSlotType.CURIO).get()))
                    .displayItems((parameters, output) -> {
                        for (CurioSlotType type : CurioSlotType.values()) {
                            // Crystals tied to another mod only show up if that mod is installed.
                            if (type.isModAvailable()) {
                                output.accept(ModItems.get(type).get());
                            }
                        }
                    })
                    .build());

    private ModCreativeTabs() {
    }
}

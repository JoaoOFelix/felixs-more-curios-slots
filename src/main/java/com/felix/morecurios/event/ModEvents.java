package com.felix.morecurios.event;

import com.felix.morecurios.FelixMoreCurios;
import com.felix.morecurios.network.ModNetwork;
import com.felix.morecurios.slot.PlayerSlotData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FelixMoreCurios.MODID)
public final class ModEvents {

    private ModEvents() {
    }

    /** Keeps the per-player slot counters when the player entity is recreated (death, End portal...). */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        PlayerSlotData.copy(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) ModNetwork.sync(p);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) ModNetwork.sync(p);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) ModNetwork.sync(p);
    }
}

package com.felix.morecurios.network;

import com.felix.morecurios.client.ClientUsedCache;
import com.felix.morecurios.slot.CurioSlotType;
import com.felix.morecurios.slot.PlayerSlotData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record UsedCountsPacket(Map<String, Integer> used) {

    public static UsedCountsPacket of(ServerPlayer player) {
        Map<String, Integer> map = new HashMap<>();
        for (CurioSlotType type : CurioSlotType.values()) {
            map.put(type.getSlotId(), PlayerSlotData.getUsed(player, type));
        }
        return new UsedCountsPacket(map);
    }

    public static UsedCountsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readUtf(), buf.readVarInt());
        }
        return new UsedCountsPacket(map);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(used.size());
        used.forEach((id, count) -> {
            buf.writeUtf(id);
            buf.writeVarInt(count);
        });
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientUsedCache.set(used));
        ctx.get().setPacketHandled(true);
    }
}
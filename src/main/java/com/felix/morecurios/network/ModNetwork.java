package com.felix.morecurios.network;

import com.felix.morecurios.FelixMoreCurios;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FelixMoreCurios.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, UsedCountsPacket.class,
                UsedCountsPacket::encode, UsedCountsPacket::decode, UsedCountsPacket::handle);
    }

    /** Envia os contadores de uso do jogador para o cliente dele. */
    public static void sync(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), UsedCountsPacket.of(player));
    }
}
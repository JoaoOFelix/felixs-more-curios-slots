package com.felix.morecurios.client;

import com.felix.morecurios.slot.CurioSlotType;

import java.util.Map;

public final class ClientUsedCache {
    private static volatile Map<String, Integer> used = Map.of();

    private ClientUsedCache() {}

    public static void set(Map<String, Integer> map) { used = Map.copyOf(map); }

    public static int get(CurioSlotType type) { return used.getOrDefault(type.getSlotId(), 0); }
}
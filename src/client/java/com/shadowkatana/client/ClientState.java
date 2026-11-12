package com.shadowkatana.client;

import com.shadowkatana.katana.KatanaPhase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Estado recebido do servidor (fase da katana de cada jogador e recargas do HUD). */
public final class ClientState {
    public static final Map<Integer, Integer> PHASES = new ConcurrentHashMap<>();
    public static long tick;
    public static long teleportReady;
    public static long shadowReady;

    public static int phase(int entityId) {
        return PHASES.getOrDefault(entityId, KatanaPhase.NONE);
    }

    private ClientState() {}
}

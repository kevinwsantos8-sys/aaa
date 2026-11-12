package com.shadowkatana.network;

import com.shadowkatana.katana.KatanaServer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(Payloads.Action.ID, Payloads.Action.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.Phase.ID, Payloads.Phase.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.Cooldown.ID, Payloads.Cooldown.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Payloads.Action.ID,
                (payload, context) -> KatanaServer.handleAction(context.player(), payload.id()));
    }

    private ModNetworking() {}
}

package com.shadowkatana;

import com.shadowkatana.item.ModItems;
import com.shadowkatana.katana.KatanaServer;
import com.shadowkatana.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ShadowKatanaMod implements ModInitializer {
    public static final String MOD_ID = "shadowkatana";

    @Override
    public void onInitialize() {
        ModSounds.init();
        ModItems.register();
        ModNetworking.register();
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerManager().getPlayerList().forEach(KatanaServer::tick));
    }
}

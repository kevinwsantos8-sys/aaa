package com.shadowkatana.client;

import com.shadowkatana.client.render.BackKatanaFeatureRenderer;
import com.shadowkatana.client.screen.KatanaUpgradeScreen;
import com.shadowkatana.katana.KatanaData;
import com.shadowkatana.network.Payloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ShadowKatanaClient implements ClientModInitializer {
    public static KeyBinding DRAW_KEY, MENU_KEY, TELEPORT_KEY, INVIS_KEY;

    @Override
    public void onInitializeClient() {
        DRAW_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowkatana.draw", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.shadowkatana"));
        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowkatana.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.shadowkatana"));
        TELEPORT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowkatana.teleport", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category.shadowkatana"));
        INVIS_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowkatana.invisibility", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.shadowkatana"));

        // Katana nas costas
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                helper.register(new BackKatanaFeatureRenderer(playerRenderer));
            }
        });

        // Dados do servidor
        ClientPlayNetworking.registerGlobalReceiver(Payloads.Phase.ID,
                (payload, context) -> ClientState.PHASES.put(payload.entityId(), payload.phase()));
        ClientPlayNetworking.registerGlobalReceiver(Payloads.Cooldown.ID, (payload, context) -> {
            ClientState.teleportReady = ClientState.tick + payload.teleport();
            ClientState.shadowReady = ClientState.tick + payload.shadow();
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> ShadowHud.render(context));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientState.tick++;
            if (client.player == null) return;
            while (DRAW_KEY.wasPressed()) {
                if (client.currentScreen == null) ClientPlayNetworking.send(new Payloads.Action(0));
            }
            while (TELEPORT_KEY.wasPressed()) {
                if (client.currentScreen == null) ClientPlayNetworking.send(new Payloads.Action(1));
            }
            while (INVIS_KEY.wasPressed()) {
                if (client.currentScreen == null) ClientPlayNetworking.send(new Payloads.Action(2));
            }
            while (MENU_KEY.wasPressed()) {
                if (client.currentScreen != null) continue;
                if (KatanaData.findKatana(client.player) == null) {
                    client.player.sendMessage(Text.translatable("message.shadowkatana.no_katana"), true);
                } else {
                    client.setScreen(new KatanaUpgradeScreen());
                }
            }
        });
    }
}

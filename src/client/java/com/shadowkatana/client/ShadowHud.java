package com.shadowkatana.client;

import com.shadowkatana.katana.KatanaPhase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Recargas dos poderes (canto inferior esquerdo), so aparece com a katana. */
public final class ShadowHud {
    public static void render(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        if (ClientState.phase(mc.player.getId()) == KatanaPhase.NONE) return;

        int x = 8;
        int y = context.getScaledWindowHeight() - 40;
        line(context, mc, x, y, "hud.shadowkatana.teleport", ClientState.teleportReady);
        line(context, mc, x, y + 12, "hud.shadowkatana.shadow", ClientState.shadowReady);
    }

    private static void line(DrawContext context, MinecraftClient mc, int x, int y, String key, long readyAt) {
        long remaining = readyAt - ClientState.tick;
        Text status = remaining > 0
                ? Text.translatable("hud.shadowkatana.cooldown", (remaining + 19) / 20)
                : Text.translatable("hud.shadowkatana.ready");
        int color = remaining > 0 ? 0xFF6060 : 0xB080FF;
        context.drawTextWithShadow(mc.textRenderer, Text.translatable(key).append(": ").append(status), x, y, color);
    }

    private ShadowHud() {}
}

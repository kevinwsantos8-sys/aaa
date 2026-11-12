package com.shadowkatana.client.render;

import com.shadowkatana.client.ClientState;
import com.shadowkatana.katana.KatanaPhase;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Animacao de sacar/guardar. progress(): 0 = katana nas costas, 1 = na mao; guardar toca ao contrario.
 * Para mudar o movimento do BRACO, edite a tabela ARM; o da katana fica em KatanaPose.
 */
public final class KatanaAnim {
    /** Parte inicial da animacao em que so o braco se mexe (katana ainda nas costas). */
    public static final float REACH = 0.25f;

    /** Quadros-chave do braco direito: {t, pitch, yaw, roll} em radianos. Inicio e fim = pose normal do jogo. */
    private static final float[][] ARM = {
            {0.25f, -2.75f, 0.35f, 0.45f},  // levanta e alcanca o cabo atras do ombro
            {0.55f, -2.45f, 0.10f, 0.30f},  // puxa a lamina
            {0.80f, -1.45f, -0.35f, 0.00f}, // corta para a frente
    };

    private static final class Anim { int phase = -1; float start; }
    private static final Map<Integer, Anim> ANIMS = new HashMap<>();

    /** Progresso 0..1; -1 = nao desenhar nas costas (sem katana ou katana na mao). */
    public static float progress(PlayerEntity player, float age) {
        int id = player.getId();
        int phase = ClientState.phase(id);
        if (phase != KatanaPhase.DRAWING && phase != KatanaPhase.SHEATHING) {
            ANIMS.remove(id);
            return phase == KatanaPhase.SHEATHED ? 0f : -1f;
        }
        Anim anim = ANIMS.computeIfAbsent(id, k -> new Anim());
        if (anim.phase != phase) {
            anim.phase = phase;
            anim.start = age;
        }
        float elapsed = age - anim.start;
        if (phase == KatanaPhase.DRAWING) {
            return MathHelper.clamp(elapsed / KatanaPhase.DRAW_TICKS, 0f, 1f);
        }
        return 1f - MathHelper.clamp(elapsed / KatanaPhase.SHEATH_TICKS, 0f, 1f);
    }

    /** Aplica a pose animada no braco direito (depois da pose normal do jogo). */
    public static void applyArm(ModelPart arm, float t) {
        if (t <= 0f || t >= 1f) return;
        float[] prev = {0f, arm.pitch, arm.yaw, arm.roll};
        float[] next = null;
        for (float[] k : ARM) {
            if (t <= k[0]) { next = k; break; }
            prev = k;
        }
        if (next == null) next = new float[]{1f, arm.pitch, arm.yaw, arm.roll};
        float span = next[0] - prev[0];
        float u = span <= 0f ? 1f : (t - prev[0]) / span;
        u = u * u * (3f - 2f * u);
        arm.pitch = MathHelper.lerp(u, prev[1], next[1]);
        arm.yaw = MathHelper.lerp(u, prev[2], next[2]);
        arm.roll = MathHelper.lerp(u, prev[3], next[3]);
    }

    private KatanaAnim() {}
}

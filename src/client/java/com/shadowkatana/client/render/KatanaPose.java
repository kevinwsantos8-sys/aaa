package com.shadowkatana.client.render;

/**
 * >>> AJUSTE AQUI <<<  Posicao/animacao da katana (espaco do corpo do jogador, em blocos).
 * y positivo = para baixo a partir do ombro, z positivo = costas, z negativo = frente. Angulos em graus.
 */
public final class KatanaPose {
    // Nas COSTAS (guardada)
    public static final float BACK_X = 0.02f, BACK_Y = 0.42f, BACK_Z = 0.17f;
    public static final float BACK_ROLL = 145f;
    public static final float BACK_PITCH = 0f;

    // Na MAO direita (fim do saque)
    public static final float HAND_X = -0.36f, HAND_Y = 0.58f, HAND_Z = -0.50f;
    public static final float HAND_ROLL = 0f;
    public static final float HAND_PITCH = -70f;

    public static final float ARC_HEIGHT = 0.65f;                       // arco por cima do ombro
    public static final float FLOURISH_ROLL = 50f, FLOURISH_PITCH = 25f; // giro extra de "corte"
    public static final float SCALE = 0.55f;

    private KatanaPose() {}
}

package com.shadowkatana.client.render;

import com.shadowkatana.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** Desenha a katana nas costas do jogador e durante o saque/guarda. */
public class BackKatanaFeatureRenderer
        extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private ItemStack katana;

    public BackKatanaFeatureRenderer(
            FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {
        if (player.isInvisible()) return;

        float t = KatanaAnim.progress(player, animationProgress);
        if (t < 0f) return; // sem katana, ou katana na mao (o jogo desenha)

        // a katana so sai das costas depois que o braco chega no cabo
        float k = MathHelper.clamp((t - KatanaAnim.REACH) / (1f - KatanaAnim.REACH), 0f, 1f);
        float e = k * k * (3f - 2f * k);
        float swing = MathHelper.sin((float) Math.PI * e);

        float x = MathHelper.lerp(e, KatanaPose.BACK_X, KatanaPose.HAND_X);
        float y = MathHelper.lerp(e, KatanaPose.BACK_Y, KatanaPose.HAND_Y) - KatanaPose.ARC_HEIGHT * swing;
        float z = MathHelper.lerp(e, KatanaPose.BACK_Z, KatanaPose.HAND_Z);
        float roll = MathHelper.lerp(e, KatanaPose.BACK_ROLL, KatanaPose.HAND_ROLL) + KatanaPose.FLOURISH_ROLL * swing;
        float pitch = MathHelper.lerp(e, KatanaPose.BACK_PITCH, KatanaPose.HAND_PITCH) - KatanaPose.FLOURISH_PITCH * swing;

        if (katana == null) katana = new ItemStack(ModItems.SHADOW_KATANA);

        matrices.push();
        getContextModel().body.rotate(matrices); // segue o corpo (inclusive agachado)
        matrices.translate(x, y, z);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f)); // desfaz o espelhamento do modelo
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        matrices.scale(KatanaPose.SCALE, KatanaPose.SCALE, KatanaPose.SCALE);
        MinecraftClient.getInstance().getItemRenderer().renderItem(katana, ModelTransformationMode.NONE,
                light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, player.getWorld(), player.getId());
        matrices.pop();
    }
}

package com.shadowkatana.client.screen;

import com.shadowkatana.katana.KatanaData;
import com.shadowkatana.network.Payloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Menu da katana (tecla K): sobe o nivel dos 3 upgrades gastando niveis de XP. */
public class KatanaUpgradeScreen extends Screen {
    private static final String[] NAMES = {
            "screen.shadowkatana.sword", "screen.shadowkatana.teleport", "screen.shadowkatana.shadow"};
    private static final String[] EFFECTS = {
            "screen.shadowkatana.effect.sword", "screen.shadowkatana.effect.teleport", "screen.shadowkatana.effect.shadow"};

    private final ButtonWidget[] buttons = new ButtonWidget[3];

    public KatanaUpgradeScreen() {
        super(Text.translatable("screen.shadowkatana.title"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 2 - 70;
        for (int i = 0; i < 3; i++) {
            final int type = i;
            buttons[i] = ButtonWidget.builder(Text.literal("+"),
                            b -> ClientPlayNetworking.send(new Payloads.Action(3 + type)))
                    .dimensions(cx - 100, top + 30 + i * 45, 200, 20)
                    .build();
            addDrawableChild(buttons[i]);
        }
    }

    private static double value(int type, int level) {
        return switch (type) {
            case KatanaData.SWORD -> KatanaData.swordBonus(level);
            case KatanaData.TELEPORT -> KatanaData.teleportDamage(level);
            default -> KatanaData.shadowBonus(level);
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Screen.render ja desenha o fundo e os botoes
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int top = this.height / 2 - 70;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, top - 10, 0xFFFFFF);

        ItemStack katana = this.client != null && this.client.player != null
                ? KatanaData.findKatana(this.client.player) : null;
        int xp = this.client != null && this.client.player != null ? this.client.player.experienceLevel : 0;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("screen.shadowkatana.xp", xp), cx, top + 6, 0xA0FFA0);

        for (int i = 0; i < 3; i++) {
            int level = katana == null ? 0 : KatanaData.getLevel(katana, i);
            int y = top + 30 + i * 45;
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("screen.shadowkatana.level_line", Text.translatable(NAMES[i]), level, KatanaData.MAX_LEVEL),
                    cx, y - 12, 0xFFFFFF);
            if (level >= KatanaData.MAX_LEVEL) {
                buttons[i].setMessage(Text.translatable("screen.shadowkatana.max"));
                buttons[i].active = false;
            } else {
                buttons[i].setMessage(Text.translatable("screen.shadowkatana.upgrade", KatanaData.cost(level)));
                buttons[i].active = katana != null;
                context.drawCenteredTextWithShadow(this.textRenderer,
                        Text.translatable(EFFECTS[i], String.format("%.1f", value(i, level)), String.format("%.1f", value(i, level + 1))),
                        cx, y + 24, 0xB0B0B0);
            }
        }
    }
}

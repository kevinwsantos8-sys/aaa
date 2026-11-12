package com.shadowkatana.katana;

import com.shadowkatana.item.ModItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/** Niveis de upgrade (guardados na propria katana) e formulas de balanceamento. Usado por cliente e servidor. */
public final class KatanaData {
    public static final int SWORD = 0, TELEPORT = 1, SHADOW = 2;
    public static final int MAX_LEVEL = 50;
    private static final String[] KEYS = {"sk_sword", "sk_teleport", "sk_shadow"};

    public static int getLevel(ItemStack stack, int type) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        return data == null ? 0 : data.copyNbt().getInt(KEYS[type]);
    }

    public static void setLevel(ItemStack stack, int type, int level) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putInt(KEYS[type], level));
    }

    /** Custo em niveis de XP para subir do nivel atual. */
    public static int cost(int level) { return 1 + level / 10; }

    /** Dano extra de cada golpe da katana. */
    public static double swordBonus(int level) { return 0.5 * level; }

    /** Dano do teleporte. */
    public static double teleportDamage(int level) { return 2.0 + 0.5 * level; }

    /** Dano extra do primeiro golpe depois da invisibilidade. */
    public static double shadowBonus(int level) { return 2.0 + 0.75 * level; }

    public static ItemStack findKatana(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(ModItems.SHADOW_KATANA)) return stack;
        }
        return null;
    }

    private KatanaData() {}
}

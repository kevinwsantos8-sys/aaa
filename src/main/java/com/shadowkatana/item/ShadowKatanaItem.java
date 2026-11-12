package com.shadowkatana.item;

import com.shadowkatana.katana.KatanaServer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShadowKatanaItem extends SwordItem {

    public ShadowKatanaItem(Settings settings) {
        super(ToolMaterials.NETHERITE,
                settings.fireproof().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 3, -2.2f)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.postHit(stack, target, attacker);
        if (attacker instanceof ServerPlayerEntity player) {
            KatanaServer.onHit(player, target, stack);
        }
        return result;
    }
}

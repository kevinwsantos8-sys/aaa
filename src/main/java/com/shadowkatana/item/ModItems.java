package com.shadowkatana.item;

import com.shadowkatana.ShadowKatanaMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item SHADOW_KATANA = Registry.register(
            Registries.ITEM,
            Identifier.of(ShadowKatanaMod.MOD_ID, "shadow_katana"),
            new ShadowKatanaItem(new Item.Settings()));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(SHADOW_KATANA));
    }

    private ModItems() {}
}

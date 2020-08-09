package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class PlayerEquipmentModule {
    private static final UUID[] UUID_BY_SLOT = new UUID[]{
            UUID.fromString("536049db-3699-4cff-831c-52fe99b24269"),
            UUID.fromString("75a8a55f-13de-400f-a823-444e71729fd5"),
            UUID.fromString("c787ae8b-6cc1-4b72-ac00-e047f5005c32"),
            UUID.fromString("d598564a-84be-46fe-ac46-3028c6e45dd1"),
            UUID.fromString("38e5df08-9bd6-446e-a75d-f0b2aa150a73"),
            UUID.fromString("9b444ef7-5020-483e-b355-7b975958634a")
    };

    private final MKPlayerData playerData;

    public PlayerEquipmentModule(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    public void onEquipmentChange(EquipmentSlotType slot, ItemStack from, ItemStack to) {
        if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            handleArmorChange(slot, from, to);
        }
    }

    public UUID getSlotUUID(EquipmentSlotType slot) {
        return UUID_BY_SLOT[slot.ordinal()];
    }

    private void handleArmorChange(EquipmentSlotType slot, ItemStack from, ItemStack to) {
        if (!from.isEmpty() && from.getItem() instanceof ArmorItem) {
            ArmorClass itemClass = ArmorClass.getItemArmorClass((ArmorItem) from.getItem());
            if (itemClass != null) {
                removeSlot(slot, itemClass);
            }
        }
        if (!to.isEmpty() && to.getItem() instanceof ArmorItem) {
            ArmorClass armorClass = ArmorClass.getItemArmorClass((ArmorItem) to.getItem());
            if (armorClass != null) {
                addSlot(slot, armorClass);
            }
        }
    }

    private AttributeModifier createSlotModifier(EquipmentSlotType slot, AttributeModifier mod) {
        return new AttributeModifier(getSlotUUID(slot), mod::getName, mod.getAmount(), mod.getOperation()).setSaved(false);
    }

    private void addSlot(EquipmentSlotType slot, ArmorClass itemClass) {
        itemClass.getPositiveModifierMap(slot).forEach((attr, mod) -> {
            AttributeModifier dup = createSlotModifier(slot, mod);
            playerData.getEntity().getAttribute(attr).applyModifier(dup);
        });
        itemClass.getNegativeModifierMap(slot).forEach((attr, mod) -> {
            AttributeModifier dup = createSlotModifier(slot, mod);
            playerData.getEntity().getAttribute(attr).applyModifier(dup);
        });
    }

    private void removeSlot(EquipmentSlotType slot, ArmorClass itemClass) {
        UUID uuid = getSlotUUID(slot);
        itemClass.getPositiveModifierMap(slot).keySet()
                .forEach(attr -> playerData.getEntity().getAttribute(attr).removeModifier(uuid));
        itemClass.getNegativeModifierMap(slot).keySet()
                .forEach(attr -> playerData.getEntity().getAttribute(attr).removeModifier(uuid));
    }
}

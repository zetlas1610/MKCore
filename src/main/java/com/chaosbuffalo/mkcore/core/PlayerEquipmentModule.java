package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
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
    private final Map<EquipmentSlotType, Map<IAttribute, AttributeModifier>> positiveModifierMap = new EnumMap<>(EquipmentSlotType.class);
    private final Map<EquipmentSlotType, Map<IAttribute, AttributeModifier>> negativeModifierMap = new EnumMap<>(EquipmentSlotType.class);

    public PlayerEquipmentModule(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    public void onEquipmentChange(EquipmentSlotType slot, ItemStack from, ItemStack to) {
        MKCore.LOGGER.info("PlayerEquipmentModule.onEquipmentChange({}, {}, {})", slot, from, to);
        if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            handleArmorChange(slot, from, to);
        }
    }

    private void handleArmorChange(EquipmentSlotType slot, ItemStack from, ItemStack to) {
        if (!from.isEmpty()) {
            removeSlot(slot);
        }
        checkSlot(slot);
    }

    private void checkSlot(EquipmentSlotType slot) {
        ItemStack to = playerData.getEntity().getItemStackFromSlot(slot);
        if (!to.isEmpty() && to.getItem() instanceof ArmorItem) {
            ArmorClass armorClass = ArmorClass.getItemArmorClass((ArmorItem) to.getItem());
            if (armorClass != null) {
                addSlot(slot, armorClass);
            }
        }
    }

    private AttributeModifier createSlotModifier(EquipmentSlotType slot, AttributeModifier mod) {
        return new AttributeModifier(UUID_BY_SLOT[slot.ordinal()], mod::getName, mod.getAmount(), mod.getOperation()).setSaved(false);
    }

    private void addSlot(EquipmentSlotType slot, ArmorClass itemClass) {
        MKCore.LOGGER.info("PlayerEquipmentModule.addSlot({}, {})", slot, itemClass.getLocation());
        removeSlot(slot);

        applyMap(slot, itemClass.getPositiveModifierMap(slot), positiveModifierMap);
        applyMap(slot, itemClass.getNegativeModifierMap(slot), negativeModifierMap);
    }

    private void applyMap(EquipmentSlotType slot,
                          Map<IAttribute, AttributeModifier> toApply,
                          Map<EquipmentSlotType, Map<IAttribute, AttributeModifier>> modifierMap) {
        Map<IAttribute, AttributeModifier> existing = modifierMap.computeIfAbsent(slot, (s) -> new HashMap<>());
        existing.clear();

        MKCore.LOGGER.info("\tapplying {} effects {}", toApply.size(), slot);
        toApply.forEach((attr, mod) -> {
            AttributeModifier dup = createSlotModifier(slot, mod);
            playerData.getEntity().getAttribute(attr).applyModifier(dup);
            existing.put(attr, dup);
        });
    }

    private void removeSlot(EquipmentSlotType slot) {
//        MKCore.LOGGER.info("PlayerEquipmentModule.removeSlot({})", slot);
        Map<IAttribute, AttributeModifier> posMod = positiveModifierMap.remove(slot);
        if (posMod != null) {
            MKCore.LOGGER.info("\tRemoving {} positive effects from {}", posMod.size(), slot);
            posMod.forEach((attr, mod) -> playerData.getEntity().getAttribute(attr).removeModifier(mod));
        }
        Map<IAttribute, AttributeModifier> negMod = negativeModifierMap.remove(slot);
        if (negMod != null) {
            MKCore.LOGGER.info("\tRemoving {} negative effects from {}", negMod.size(), slot);
            negMod.forEach((attr, mod) -> playerData.getEntity().getAttribute(attr).removeModifier(mod));
        }
    }
}

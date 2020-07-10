package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.init.ModTags;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;


public class ArmorClass {

    public static final ArmorClass LIGHT = new ArmorClass(MKCore.makeRL("armor_class.light"), ModTags.Items.LIGHT_ARMOR)
            .addPositiveEffect(SharedMonsterAttributes.MOVEMENT_SPEED, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.CASTING_SPEED, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.MANA_REGEN, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(SharedMonsterAttributes.ARMOR, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final ArmorClass MEDIUM = new ArmorClass(MKCore.makeRL("armor_class.medium"), ModTags.Items.MEDIUM_ARMOR)
            .addPositiveEffect(MKAttributes.MELEE_CRIT, 0.03, AttributeModifier.Operation.ADDITION)
            .addPositiveEffect(SharedMonsterAttributes.ATTACK_SPEED, 0.03, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.COOLDOWN, -0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.02, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final ArmorClass HEAVY = new ArmorClass(MKCore.makeRL("armor_class.heavy"), ModTags.Items.HEAVY_ARMOR)
            .addPositiveEffect(SharedMonsterAttributes.ATTACK_DAMAGE, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.ARCANE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.FIRE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.FROST_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.NATURE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.POISON_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(SharedMonsterAttributes.ARMOR_TOUGHNESS, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(SharedMonsterAttributes.MOVEMENT_SPEED, -0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.COOLDOWN, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(SharedMonsterAttributes.ATTACK_SPEED, -0.025, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final List<ArmorClass> CHECK_ORDER = Arrays.asList(LIGHT, MEDIUM, HEAVY);

    private final ResourceLocation location;
    private final Map<IAttribute, AttributeModifier> positiveModifierMap = new HashMap<>();
    private final Map<IAttribute, AttributeModifier> negativeModifierMap = new HashMap<>();
    private final Set<IArmorMaterial> materials = new HashSet<>();
    private final Tag<Item> tag;

    private static ArmorClass getArmorClassForMaterial(IArmorMaterial material) {
        return CHECK_ORDER.stream()
                .filter(armorClass -> armorClass.hasMaterial(material))
                .findFirst()
                .orElse(null);
    }

    public static ArmorClass getItemArmorClass(ArmorItem item) {
        return CHECK_ORDER.stream()
                .filter(armorClass -> armorClass.containsItem(item))
                .findFirst()
                .orElseGet(() -> getArmorClassForMaterial(item.getArmorMaterial()));
    }

    public ArmorClass(ResourceLocation location, Tag<Item> tag) {
        this.location = location;
        this.tag = tag;
    }

    public ArmorClass addNegativeEffect(IAttribute attributeIn, double amount, AttributeModifier.Operation operation) {
        AttributeModifier attributemodifier = new AttributeModifier(getTranslationKey(), amount, operation).setSaved(false);
        this.negativeModifierMap.put(attributeIn, attributemodifier);
        return this;
    }

    public ArmorClass addPositiveEffect(IAttribute attributeIn, double amount, AttributeModifier.Operation operation) {
        AttributeModifier attributemodifier = new AttributeModifier(getTranslationKey(), amount, operation).setSaved(false);
        this.positiveModifierMap.put(attributeIn, attributemodifier);
        return this;
    }

    public Map<IAttribute, AttributeModifier> getPositiveModifierMap(EquipmentSlotType slot) {
        return this.positiveModifierMap;
    }

    public Map<IAttribute, AttributeModifier> getNegativeModifierMap(EquipmentSlotType slot) {
        return this.negativeModifierMap;
    }

    private String getTranslationKey() {
        return String.format("%s.%s.name", location.getNamespace(), location.getPath());
    }

    public ITextComponent getName() {
        return new TranslationTextComponent(getTranslationKey());
    }

    public ResourceLocation getLocation() {
        return location;
    }

    private boolean hasMaterial(IArmorMaterial material) {
        return materials.contains(material);
    }

    private boolean containsItem(ArmorItem item) {
        return tag == null || tag.contains(item);
    }

    public ArmorClass register(IArmorMaterial material) {
        materials.add(material);
        return this;
    }
}

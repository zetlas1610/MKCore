package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistryEntry;


public class MKDamageType extends ForgeRegistryEntry<MKDamageType> {
    private final RangedAttribute damageAttribute;
    private final RangedAttribute resistanceAttribute;
    private final RangedAttribute critAttribute;
    private final RangedAttribute critMultiplierAttribute;
    private final ResourceLocation iconLoc;
    private float critMultiplier;
    private boolean shouldDisplay;

    public MKDamageType(ResourceLocation name, RangedAttribute damageAttribute,
                        RangedAttribute resistanceAttribute, RangedAttribute critAttribute,
                        RangedAttribute critMultiplierAttribute) {
        setRegistryName(name);
        this.damageAttribute = damageAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.critMultiplierAttribute = critMultiplierAttribute;
        this.critAttribute = critAttribute;
        this.critMultiplier = 1.0f;
        this.shouldDisplay = true;
        iconLoc = new ResourceLocation(name.getNamespace(), String.format("textures/damage_types/%s.png",
                name.getPath().substring(7)));
    }

    public MKDamageType setCritMultiplier(float value) {
        this.critMultiplier = value;
        return this;
    }

    public MKDamageType setShouldDisplay(boolean shouldDisplay) {
        this.shouldDisplay = shouldDisplay;
        return this;
    }

    public boolean shouldDisplay() {
        return shouldDisplay;
    }

    public String getDisplayName() {
        return I18n.format(String.format("%s.%s.name", getRegistryName().getNamespace(),
                getRegistryName().getPath()));
    }

    public ResourceLocation getIcon() {
        return iconLoc;
    }

    public RangedAttribute getDamageAttribute() {
        return damageAttribute;
    }

    public RangedAttribute getCritChanceAttribute() {
        return critAttribute;
    }

    public RangedAttribute getCritMultiplierAttribute() {
        return critMultiplierAttribute;
    }

    public RangedAttribute getResistanceAttribute() {
        return resistanceAttribute;
    }

    public void addAttributes(AbstractAttributeMap attributeMap) {
        attributeMap.registerAttribute(getDamageAttribute());
        attributeMap.registerAttribute(getResistanceAttribute());
    }

    public ITextComponent getCritMessage(LivingEntity source, LivingEntity target, float damage,
                                         MKAbility ability, boolean isSelf) {
        Style messageStyle = new Style();
        messageStyle.setColor(TextFormatting.AQUA);
        String msg;
        if (isSelf) {
            msg = String.format("Your %s spell just crit %s for %s",
                    ability.getAbilityName(),
                    target.getDisplayName().getFormattedText(),
                    Math.round(damage));
        } else {
            msg = String.format("%s's %s spell just crit %s for %s",
                    source.getDisplayName().getFormattedText(),
                    ability.getAbilityName(),
                    target.getDisplayName().getFormattedText(),
                    Math.round(damage));
        }
        return new StringTextComponent(msg).setStyle(messageStyle);
    }

    public float applyDamage(LivingEntity source, LivingEntity target, float originalDamage, float modifierScaling) {
        return applyDamage(source, target, source, originalDamage, modifierScaling);
    }

    public float applyDamage(LivingEntity source, LivingEntity target, Entity immediate, float originalDamage, float modifierScaling) {
        return (float) (originalDamage + source.getAttribute(getDamageAttribute()).getValue() * modifierScaling);
    }

    public float applyResistance(LivingEntity target, float originalDamage) {
        return (float) (originalDamage - (originalDamage * target.getAttribute(getResistanceAttribute()).getValue()));
    }

    public boolean rollCrit(LivingEntity source, LivingEntity target) {
        return rollCrit(source, target, source);
    }

    public boolean rollCrit(LivingEntity source, LivingEntity target, Entity immediate) {
        float critChance = getCritChance(source, target, immediate);
        return MKCombatFormulas.checkCrit(source, critChance);
    }

    public float applyCritDamage(LivingEntity source, LivingEntity target, float originalDamage) {
        return applyCritDamage(source, target, source, originalDamage);
    }

    public float applyCritDamage(LivingEntity source, LivingEntity target, Entity immediate, float originalDamage) {
        return originalDamage * getCritMultiplier(source, target, immediate);
    }

    public float getCritMultiplier(LivingEntity source, LivingEntity target) {
        return getCritMultiplier(source, target, source);
    }

    public float getCritMultiplier(LivingEntity source, LivingEntity target, Entity immediate) {
        return (float) source.getAttribute(getCritMultiplierAttribute()).getValue();
    }

    public float getCritChance(LivingEntity source, LivingEntity target) {
        return getCritChance(source, target, source);
    }

    public float getCritChance(LivingEntity source, LivingEntity target, Entity immediate) {
        return (float) source.getAttribute(getCritChanceAttribute()).getValue() * critMultiplier;
    }
}

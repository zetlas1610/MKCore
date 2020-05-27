package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
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
    private float critMultiplier;

    public MKDamageType(ResourceLocation name, RangedAttribute damageAttribute,
                        RangedAttribute resistanceAttribute, RangedAttribute critAttribute,
                        RangedAttribute critMultiplierAttribute){
        setRegistryName(name);
        this.damageAttribute = damageAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.critMultiplierAttribute = critMultiplierAttribute;
        this.critAttribute = critAttribute;
        this.critMultiplier = 1.0f;
    }

    public MKDamageType setCritMultiplier(float value){
        this.critMultiplier = value;
        return this;
    }


    public void addAttributes(AbstractAttributeMap attributeMap){
        attributeMap.registerAttribute(getDamageAttribute());
        attributeMap.registerAttribute(getResistanceAttribute());
    }

    public RangedAttribute getDamageAttribute() {
        return damageAttribute;
    }

    public float scaleDamage(LivingEntity source, LivingEntity target, float originalDamage, float modifierScaling){
        return (float) (originalDamage + source.getAttribute(getDamageAttribute()).getValue() * modifierScaling);
    }

    public RangedAttribute getCritChanceAttribute() { return critAttribute; }

    public ITextComponent getCritMessage(LivingEntity source, LivingEntity target, float damage,
                                         PlayerAbility ability, boolean isSelf){
        Style messageStyle = new Style();
        messageStyle.setColor(TextFormatting.AQUA);
        String msg;
        if (isSelf){
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

    public float applyResistance(LivingEntity target, float originalDamage){
        return (float) (originalDamage - (originalDamage * target.getAttribute(getResistanceAttribute()).getValue()));

    }

    public RangedAttribute getCritMultiplierAttribute() {
        return critMultiplierAttribute;
    }

    public boolean shouldCrit(LivingEntity source, LivingEntity target){
        float critChance = getCritChance(source, target);
        return MKCombatFormulas.checkCrit(source, critChance);
    }

    public float applyCrit(LivingEntity source, LivingEntity target, float originalDamage) {
        return originalDamage * getCritMultiplier(source, target);
    }

    public float getCritMultiplier(LivingEntity source, LivingEntity target){
        return (float) source.getAttribute(getCritMultiplierAttribute()).getValue();
    }

    public float getCritChance(LivingEntity source, LivingEntity target){
        return (float) source.getAttribute(getCritChanceAttribute()).getValue() * critMultiplier;
    }

    public RangedAttribute getResistanceAttribute(){
        return resistanceAttribute;
    }
}

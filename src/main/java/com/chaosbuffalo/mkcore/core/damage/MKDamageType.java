package com.chaosbuffalo.mkcore.core.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;


public class MKDamageType extends ForgeRegistryEntry<MKDamageType> {
    private final RangedAttribute damageAttribute;
    private final RangedAttribute resistanceAttribute;

    public MKDamageType(ResourceLocation name, RangedAttribute damageAttribute, RangedAttribute resistanceAttribute){
        setRegistryName(name);
        this.damageAttribute = damageAttribute;
        this.resistanceAttribute = resistanceAttribute;
    }

    public RangedAttribute getDamageAttribute() {
        return damageAttribute;
    }

    public float scaleDamage(LivingEntity source, float originalDamage, float modifierScaling){
        IAttributeInstance attributeInstance = source.getAttribute(getDamageAttribute());
        if (attributeInstance != null){
            return (float) (originalDamage + attributeInstance.getValue() * modifierScaling);
        } else {
            return originalDamage;
        }

    }

    public float applyResistance(LivingEntity target, float originalDamage){
        IAttributeInstance attributeInstance = target.getAttribute(getResistanceAttribute());
        if (attributeInstance != null){
            return (float) (originalDamage - (originalDamage * attributeInstance.getValue()));
        } else {
            return originalDamage;
        }

    }

    public RangedAttribute getResistanceAttribute(){
        return resistanceAttribute;
    }
}

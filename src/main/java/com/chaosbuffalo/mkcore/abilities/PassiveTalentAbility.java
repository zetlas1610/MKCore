package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.PassiveTalentEffect;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class PassiveTalentAbility extends MKAbility {
    public PassiveTalentAbility(ResourceLocation abilityId) {
        super(abilityId);
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Passive;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    public abstract PassiveTalentEffect getPassiveEffect();

    @Override
    public boolean isPoolAbility() {
        return false;
    }

    @Override
    public List<ITextComponent> getDescriptionsForEntity(IMKEntityData entityData) {
        List<ITextComponent> descriptions = new ArrayList<>();
        descriptions.add(new TranslationTextComponent("mkcore.ability.description.passive"));
        descriptions.add(AbilityDescriptions.getTargetTypeDescription(this));
        descriptions.add(AbilityDescriptions.getAbilityDescription(this, entityData, this::getDescriptionArgs));
        descriptions.addAll(AbilityDescriptions.getEffectModifiers(getPassiveEffect(), entityData, false));
        return descriptions;
    }

    @Override
    public void executeWithContext(IMKEntityData entityData, AbilityContext context) {
        LivingEntity entity = entityData.getEntity();
        if (entity instanceof PlayerEntity) {
            if (entity.getActivePotionEffect(getPassiveEffect()) == null) {
                entity.addPotionEffect(getPassiveEffect().createSelfCastEffectInstance(entity, 0));
            }
        }
    }
}

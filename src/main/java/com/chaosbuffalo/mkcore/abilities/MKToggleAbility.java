package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public abstract class MKToggleAbility extends MKAbility {

    private static final ResourceLocation TOGGLE_EFFECT = MKCore.makeRL("textures/class/abilities/ability_toggle.png");

    public MKToggleAbility(ResourceLocation abilityId) {
        super(abilityId);
    }

    public ResourceLocation getToggleGroupId() {
        return getAbilityId();
    }

    public abstract Effect getToggleEffect();

    @Override
    public float getManaCost(IMKEntityData entityData) {
        if (entityData.getEntity().isPotionActive(getToggleEffect())) {
            return 0f;
        }
        return super.getManaCost(entityData);
    }

    @Override
    public List<ITextComponent> getDescriptionsForEntity(IMKEntityData entityData) {
        List<ITextComponent> ret = super.getDescriptionsForEntity(entityData);
        ret.addAll(AbilityDescriptions.getEffectDescription(getToggleEffect(), entityData, false));
        return ret;
    }

    @Override
    public int getCastTime(IMKEntityData entityData) {
        // Active effects can be disabled instantly
        if (entityData.getEntity().isPotionActive(getToggleEffect())) {
            return 0;
        }
        return super.getCastTime(entityData);
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Active;
    }

    public void applyEffect(LivingEntity entity, IMKEntityData entityData) {
        entityData.getAbilityExecutor().setToggleGroupAbility(getToggleGroupId(), this);
    }

    public void removeEffect(LivingEntity entity, IMKEntityData entityData) {
        entityData.getAbilityExecutor().clearToggleGroupAbility(getToggleGroupId());
        if (entity.isPotionActive(getToggleEffect())) {
            entity.removePotionEffect(getToggleEffect());
        }
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData entityData, AbilityContext context) {
        if (entity.getActivePotionEffect(getToggleEffect()) != null) {
            removeEffect(entity, entityData);
        } else {
            applyEffect(entity, entityData);
        }
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SELF;
    }

    @Override
    public void drawAbilityBarEffect(Minecraft mc, int slotX, int slotY) {
        if (mc.player != null && mc.player.isPotionActive(getToggleEffect())) {
            int iconSize = MKOverlay.ABILITY_ICON_SIZE + 2;
            mc.getTextureManager().bindTexture(TOGGLE_EFFECT);
            AbstractGui.blit(slotX - 1, slotY - 1, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }
    }
}

package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.Map;

public abstract class MKToggleAbility extends MKAbility {

    private static final Map<Effect, ResourceLocation> toggleAbilityMap = new IdentityHashMap<>();
    private static final ResourceLocation TOGGLE_EFFECT = MKCore.makeRL("textures/class/abilities/ability_toggle.png");

    public static ResourceLocation getToggleAbilityIdForPotion(Effect potion) {
        return toggleAbilityMap.get(potion);
    }

    public MKToggleAbility(String domain, String id) {
        this(new ResourceLocation(domain, id));
    }

    public MKToggleAbility(ResourceLocation abilityId) {
        super(abilityId);
        toggleAbilityMap.put(getToggleEffect(), abilityId);
    }

    public ResourceLocation getToggleGroupId() {
        return getAbilityId();
    }

    public abstract Effect getToggleEffect();

    @Override
    public AbilityType getType() {
        return AbilityType.Active;
    }

    public void applyEffect(LivingEntity entity, IMKEntityData entityData) {
        entityData.getAbilityExecutor().setToggleGroupAbility(getToggleGroupId(), this);
    }

    public void removeEffect(LivingEntity entity, IMKEntityData entityData) {
        entityData.getAbilityExecutor().clearToggleGroupAbility(getToggleGroupId());
        entity.removePotionEffect(getToggleEffect());
    }

    @Override
    public void execute(LivingEntity entity, IMKEntityData entityData) {
        entityData.startAbility(this);
        if (entity.getActivePotionEffect(getToggleEffect()) != null) {
            removeEffect(entity, entityData);
        } else {
            applyEffect(entity, entityData);
        }
    }

    @Override
    public void drawAbilityBarEffect(Minecraft mc, int slotX, int slotY) {
        if (mc.player != null && mc.player.isPotionActive(getToggleEffect())) {
            int iconSize = MKOverlay.ABILITY_ICON_SIZE;
            mc.getTextureManager().bindTexture(TOGGLE_EFFECT);
            AbstractGui.blit(slotX, slotY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }
    }
}

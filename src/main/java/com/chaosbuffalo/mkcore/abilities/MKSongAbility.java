package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SongCasterEffect;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.Map;

public abstract class MKSongAbility extends MKToggleAbility {

    private static final Map<Effect, MKSongAbility> toggleAbilityMap = new IdentityHashMap<>();

    public MKSongAbility(ResourceLocation abilityId) {
        super(abilityId);
        toggleAbilityMap.put(getToggleEffect(), this);
    }

    public static MKSongAbility getAbilityForCasterEffect(SongCasterEffect potion) {
        return toggleAbilityMap.get(potion);
    }

    public int getCasterEffectSustainCost(IMKEntityData entityData) {
        return 1;
    }

    @Override
    public float getManaCost(IMKEntityData entityData) {
        // Songs cost nothing to activate, but the CasterEffect will try to drain getCasterEffectSustainCost() on the first tick
        return 0;
    }
}

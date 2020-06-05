package com.chaosbuffalo.mkcore.abilities;

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
}

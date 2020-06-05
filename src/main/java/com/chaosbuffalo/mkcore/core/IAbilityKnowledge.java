package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IAbilityKnowledge {
    @Nullable
    MKAbilityInfo getAbilityInfo(ResourceLocation abilityId);

    Collection<MKAbilityInfo> getAbilities();

    boolean learnAbility(MKAbility ability);

    boolean unlearnAbility(ResourceLocation abilityId);

    boolean knowsAbility(ResourceLocation abilityId);

    @Nullable
    MKAbilityInfo getKnownAbilityInfo(ResourceLocation abilityId);
}

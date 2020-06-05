package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityAbilityKnowledge implements IAbilityKnowledge {
    private final MKEntityData entityData;
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();

    public EntityAbilityKnowledge(MKEntityData entityData) {
        this.entityData = entityData;
    }

    @Nullable
    @Override
    public MKAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        return abilityInfoMap.get(abilityId);
    }

    @Override
    public Collection<MKAbilityInfo> getAbilities() {
        return Collections.unmodifiableCollection(abilityInfoMap.values());
    }

    @Override
    public boolean learnAbility(MKAbility ability) {
        MKAbilityInfo info = getAbilityInfo(ability.getAbilityId());
        if (info == null) {
            info = ability.createAbilityInfo();
        } else if (info.isCurrentlyKnown()) {
            return true;
        }

        if (info == null) {
            MKCore.LOGGER.error("Failed to create AbilityInfo for ability {} for player {}",
                    ability.getAbilityId(), entityData.getEntity());
            return false;
        }
        info.setKnown(true);
        abilityInfoMap.put(ability.getAbilityId(), info);
        return true;
    }

    @Override
    public boolean unlearnAbility(ResourceLocation abilityId) {
        abilityInfoMap.remove(abilityId);
        return true;
    }

    @Override
    public boolean knowsAbility(ResourceLocation abilityId) {
        return abilityInfoMap.containsKey(abilityId);
    }

    @Nullable
    @Override
    public MKAbilityInfo getKnownAbilityInfo(ResourceLocation abilityId) {
        MKAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null)
            return null;
        return info;
    }
}

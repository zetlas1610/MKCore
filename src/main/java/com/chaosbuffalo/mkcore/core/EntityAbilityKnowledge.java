package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

public class EntityAbilityKnowledge implements IAbilityKnowledge, IMKSerializable<CompoundNBT> {
    private final MKEntityData entityData;
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final Map<ResourceLocation, Integer> abilityPriorities = new HashMap<>();
    private List<MKAbilityInfo> priorityOrder = new ArrayList<>();

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

    public void updatePriorityOrder() {
        priorityOrder = new ArrayList<>(getAbilities());
        priorityOrder.sort(Comparator.comparingInt((x) -> abilityPriorities.getOrDefault(x.getId(), 1)));
    }

    public List<MKAbilityInfo> getAbilitiesPriorityOrder() {
        return priorityOrder;
    }

    private boolean learnAbilityInternal(MKAbility ability) {
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

    public boolean learnAbility(MKAbility ability, int priority) {
        boolean ret = learnAbilityInternal(ability);
        if (ret) {
            abilityPriorities.put(ability.getAbilityId(), priority);
            updatePriorityOrder();
        }
        return ret;
    }

    @Override
    public boolean learnAbility(MKAbility ability) {
        return learnAbility(ability, 1);
    }

    @Override
    public boolean unlearnAbility(ResourceLocation abilityId) {
        abilityInfoMap.remove(abilityId);
        abilityPriorities.remove(abilityId);
        updatePriorityOrder();
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

    @Override
    public void serialize(CompoundNBT tag) {
        CompoundNBT abilityInfos = new CompoundNBT();
        for (Entry<ResourceLocation, MKAbilityInfo> entry : abilityInfoMap.entrySet()) {
            CompoundNBT entryNbt = new CompoundNBT();
            entry.getValue().serialize(entryNbt);
            abilityInfos.put(entry.getKey().toString(), entryNbt);
        }
        tag.put("abilities", abilityInfos);
        CompoundNBT priorities = new CompoundNBT();
        for (Entry<ResourceLocation, Integer> priortyEntry : abilityPriorities.entrySet()) {
            priorities.putInt(priortyEntry.getKey().toString(), priortyEntry.getValue());
        }
        tag.put("priorities", priorities);
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo();
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        if (tag.contains("abilities")) {
            CompoundNBT abilityInfo = tag.getCompound("abilities");
            for (String key : abilityInfo.keySet()) {
                ResourceLocation loc = new ResourceLocation(key);
                MKAbilityInfo info = createAbilityInfo(loc);
                if (info != null) {
                    if (info.deserialize(abilityInfo.getCompound(key))) {
                        abilityInfoMap.put(loc, info);
                    }
                }
            }
        }
        if (tag.contains("priorities")) {
            CompoundNBT priorityInfo = tag.getCompound("priorities");
            for (String key : priorityInfo.keySet()) {
                ResourceLocation loc = new ResourceLocation(key);
                abilityPriorities.put(loc, priorityInfo.getInt(key));
            }
            updatePriorityOrder();
        }

        return true;
    }
}

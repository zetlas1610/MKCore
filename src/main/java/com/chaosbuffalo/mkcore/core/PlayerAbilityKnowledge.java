package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class PlayerAbilityKnowledge extends PlayerSyncComponent implements IAbilityKnowledge {
    private final MKPlayerData playerData;
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final SyncMapUpdater<ResourceLocation, MKAbilityInfo> abilityUpdater =
            new SyncMapUpdater<>("known", () -> abilityInfoMap, ResourceLocation::toString,
                    ResourceLocation::new, PlayerAbilityKnowledge::createAbilityInfo);

    public PlayerAbilityKnowledge(MKPlayerData playerData) {
        super("abilities");
        this.playerData = playerData;
        addPrivate(abilityUpdater);
    }

    @Override
    @Nullable
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
            MKCore.LOGGER.warn("Player {} tried to learn already-known ability {}", playerData.getEntity(), ability.getAbilityId());
            return true;
        }

        if (info == null) {
            MKCore.LOGGER.error("Failed to create PlayerAbilityInfo for ability {} for player {}", ability.getAbilityId(), playerData.getEntity());
            return false;
        }

        info.setKnown(true);

        abilityInfoMap.put(ability.getAbilityId(), info);
        markDirty(info);
        return true;
    }


    @Override
    public boolean unlearnAbility(ResourceLocation abilityId) {
        MKAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", playerData.getEntity(), abilityId);
            return false;
        }

        info.setKnown(false);

        markDirty(info);
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
        if (info == null || !info.isCurrentlyKnown())
            return null;
        return info;
    }

    public void markDirty(MKAbilityInfo info) {
        abilityUpdater.markDirty(info.getId());
    }

    public void serialize(CompoundNBT tag) {
        ListNBT tagList = new ListNBT();
        for (MKAbilityInfo info : abilityInfoMap.values()) {
            CompoundNBT sk = new CompoundNBT();
            info.serialize(sk);
            tagList.add(sk);
        }

        tag.put("abilities", tagList);
    }

    public void deserialize(CompoundNBT tag) {
        if (tag.contains("abilities")) {
            ListNBT tagList = tag.getList("abilities", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT abilityTag = tagList.getCompound(i);
                ResourceLocation abilityId = new ResourceLocation(abilityTag.getString("id"));
                MKAbility ability = MKCoreRegistry.getAbility(abilityId);
                if (ability == null) {
                    continue;
                }

                MKAbilityInfo info = ability.createAbilityInfo();
                if (info == null)
                    continue;
                if (info.deserialize(abilityTag))
                    abilityInfoMap.put(abilityId, info);
            }
        } else {
            abilityInfoMap.clear();
        }
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo();
    }
}

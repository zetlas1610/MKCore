package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerKnownAbilities implements ISyncObject {
    private final PlayerKnowledge knowledge;
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final List<MKAbilityInfo> dirtyList = new ArrayList<>();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public PlayerKnownAbilities(PlayerKnowledge knowledge) {
        this.knowledge = knowledge;
    }

    @Nullable
    public MKAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        return abilityInfoMap.get(abilityId);
    }

    public Collection<MKAbilityInfo> getAbilities() {
        return Collections.unmodifiableCollection(abilityInfoMap.values());
    }


    public boolean learn(MKAbility ability) {
        MKAbilityInfo info = getAbilityInfo(ability.getAbilityId());
        if (info == null) {
            info = ability.createAbilityInfo();
        } else if (info.isCurrentlyKnown()) {
            MKCore.LOGGER.warn("Player {} tried to learn already-known ability {}", knowledge.getPlayer(), ability.getAbilityId());
            return true;
        }

        if (info == null) {
            MKCore.LOGGER.error("Failed to create PlayerAbilityInfo for ability {} for player {}", ability.getAbilityId(), knowledge.getPlayer());
            return false;
        }

        info.setKnown(true);

        abilityInfoMap.put(ability.getAbilityId(), info);
        markDirty(info);
        return true;
    }


    public boolean unlearn(ResourceLocation abilityId) {
        MKAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", knowledge.getPlayer(), abilityId);
            return false;
        }

        info.setKnown(false);

        markDirty(info);
        return true;
    }

    public boolean knowsAbility(ResourceLocation abilityId) {
        return abilityInfoMap.containsKey(abilityId);
    }


    public void markDirty(MKAbilityInfo info) {
        dirtyList.add(info);
        parentNotifier.markDirty(this);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirtyList.size() > 0;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (tag.contains("knownAbilities")) {
            CompoundNBT abilities = tag.getCompound("knownAbilities");

            for (String id : abilities.keySet()) {
                ResourceLocation abilityId = new ResourceLocation(id);
                MKAbilityInfo current = abilityInfoMap.computeIfAbsent(abilityId, newAbilityId -> {
                    MKAbility ability = MKCoreRegistry.getAbility(newAbilityId);
                    if (ability == null)
                        return null;

                    return ability.createAbilityInfo();
                });
                if (current == null)
                    continue;
                if (!current.deserialize(abilities.getCompound(id))) {
                    MKCore.LOGGER.error("Failed to deserialize ability update for {}", id);
                    continue;
                }
                abilityInfoMap.put(abilityId, current);
            }
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirtyList.size() > 0) {
            CompoundNBT abilities = new CompoundNBT();
            for (MKAbilityInfo info : dirtyList) {
                CompoundNBT ability = new CompoundNBT();
                info.serialize(ability);
                abilities.put(info.getId().toString(), ability);
            }

            tag.put("knownAbilities", abilities);
            dirtyList.clear();
        }
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT abilities = new CompoundNBT();
        for (MKAbilityInfo info : abilityInfoMap.values()) {
            CompoundNBT ability = new CompoundNBT();
            info.serialize(ability);
            abilities.put(info.getId().toString(), ability);
        }

        tag.put("knownAbilities", abilities);
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
                if (info.deserialize(abilityTag))
                    abilityInfoMap.put(abilityId, info);
            }
        } else {
            abilityInfoMap.clear();
        }
    }
}

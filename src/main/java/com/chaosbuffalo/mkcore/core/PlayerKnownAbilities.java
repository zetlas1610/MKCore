package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerKnownAbilities implements ISyncObject {
    private final PlayerKnowledge knowledge;
    private final Map<ResourceLocation, PlayerAbilityInfo> abilityInfoMap = new HashMap<>();
    private final List<PlayerAbilityInfo> dirtyList = new ArrayList<>();

    public PlayerKnownAbilities(PlayerKnowledge knowledge) {
        this.knowledge = knowledge;
    }

    @Nullable
    public PlayerAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        return abilityInfoMap.get(abilityId);
    }

    public Collection<PlayerAbilityInfo> getAbilities() {
        return Collections.unmodifiableCollection(abilityInfoMap.values());
    }


    public boolean learn(PlayerAbility ability) {
        PlayerAbilityInfo info = getAbilityInfo(ability.getAbilityId());
        if (info == null) {
            info = ability.createAbilityInfo();
        }

        if (!info.upgrade()) {
            MKCore.LOGGER.info("PlayerKnownAbilities.learn({}) - failed to upgrade", ability.getAbilityId());
            return false;
        }

        abilityInfoMap.put(ability.getAbilityId(), info);
        markDirty(info);
        return true;
    }


    public boolean unlearn(ResourceLocation abilityId) {
        PlayerAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", knowledge.getPlayer(), abilityId);
            return false;
        }

        if (!info.downgrade())
            return false;

        markDirty(info);
        return true;
    }

    public boolean knowsAbility(ResourceLocation abilityId) {
        return abilityInfoMap.containsKey(abilityId);
    }


    public void markDirty(PlayerAbilityInfo info) {
        dirtyList.add(info);
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
                PlayerAbilityInfo current = abilityInfoMap.computeIfAbsent(abilityId, newAbilityId -> {
                    PlayerAbility ability = MKCoreRegistry.getAbility(newAbilityId);
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
            for (PlayerAbilityInfo info : dirtyList) {
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
        for (PlayerAbilityInfo info : abilityInfoMap.values()) {
            CompoundNBT ability = new CompoundNBT();
            info.serialize(ability);
            abilities.put(info.getId().toString(), ability);
        }

        tag.put("knownAbilities", abilities);
    }

    public void serialize(CompoundNBT tag) {
        ListNBT tagList = new ListNBT();
        for (PlayerAbilityInfo info : abilityInfoMap.values()) {
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
                PlayerAbility ability = MKCoreRegistry.getAbility(abilityId);
                if (ability == null) {
                    continue;
                }

                PlayerAbilityInfo info = ability.createAbilityInfo();
                if (info.deserialize(abilityTag))
                    abilityInfoMap.put(abilityId, info);
            }
        } else {
            abilityInfoMap.clear();
        }
    }
}

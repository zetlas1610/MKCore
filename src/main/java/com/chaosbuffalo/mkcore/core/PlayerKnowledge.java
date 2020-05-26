package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerKnowledge implements ISyncObject {

    private final MKPlayerData playerData;

    private final Map<ResourceLocation, PlayerAbilityInfo> abilityInfoMap = new HashMap<>();
    private final PlayerActionBar actionBar;
    private final KnownAbilityUpdater abilityUpdater = new KnownAbilityUpdater();

    private final CompositeUpdater privateUpdater = new CompositeUpdater(abilityUpdater);

    public PlayerKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        actionBar = new PlayerActionBar(this);
        privateUpdater.add(actionBar);
    }

    private PlayerEntity getPlayer() {
        return playerData.getPlayer();
    }

    public PlayerActionBar getActionBar() {
        return actionBar;
    }

    @Nullable
    public PlayerAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        return abilityInfoMap.get(abilityId);
    }

    public void learnAbility(PlayerAbility ability) {
        ResourceLocation abilityId = ability.getAbilityId();
        PlayerAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null) {
            info = ability.createAbilityInfo();
        }

        if (!info.upgrade())
            return;

        actionBar.tryPlaceOnBar(abilityId);

        abilityInfoMap.put(abilityId, info);
        abilityUpdater.markDirty(info);
    }

    public void unlearnAbility(ResourceLocation abilityId) {
        PlayerAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", getPlayer(), abilityId);
            return;
        }

        if (!info.downgrade())
            return;

        abilityUpdater.markDirty(info);
        // FIXME: ugly
        playerData.getAbilityExecutor().onAbilityUnlearned(info.getAbility());
        actionBar.onAbilityUnlearned(info.getAbility());
    }

    @Override
    public boolean isDirty() {
        return privateUpdater.isDirty();
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        privateUpdater.deserializeUpdate(tag);
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        privateUpdater.serializeUpdate(tag);
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        privateUpdater.serializeFull(tag);
    }


    class KnownAbilityUpdater implements ISyncObject {
        private final List<PlayerAbilityInfo> list = new ArrayList<>();

        public KnownAbilityUpdater() {
        }

        public void markDirty(PlayerAbilityInfo info) {
            list.add(info);
        }

        @Override
        public boolean isDirty() {
            return list.size() > 0;
        }

        @Override
        public void deserializeUpdate(CompoundNBT tag) {
            if (tag.contains("abilityUpdates")) {
                CompoundNBT abilities = tag.getCompound("abilityUpdates");

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
            if (list.size() > 0) {
                CompoundNBT abilities = new CompoundNBT();
                for (PlayerAbilityInfo info : list) {
                    CompoundNBT ability = new CompoundNBT();
                    info.serialize(ability);
                    abilities.put(info.getId().toString(), ability);
                }

                tag.put("abilityUpdates", abilities);

                list.clear();
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

            tag.put("abilityUpdates", abilities);
        }
    }
}

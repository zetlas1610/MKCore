package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class PlayerKnownAbilities extends PlayerSyncComponent {
    private final MKPlayerData playerData;
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final SyncMapUpdater<ResourceLocation, MKAbilityInfo> abilityUpdater =
            new SyncMapUpdater<>("known",
                    () -> abilityInfoMap,
                    MKAbilityInfo::encodeId,
                    MKAbilityInfo::decodeId,
                    PlayerKnownAbilities::createAbilityInfo
            );

    public PlayerKnownAbilities(MKPlayerData playerData) {
        super("abilities");
        this.playerData = playerData;
        addPrivate(abilityUpdater);
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


    public boolean unlearn(ResourceLocation abilityId) {
        MKAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", playerData.getEntity(), abilityId);
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
        abilityUpdater.markDirty(info.getId());
    }

    public void serialize(CompoundNBT tag) {
        abilityUpdater.serializeStorage(tag, "abilities");
    }

    public void deserialize(CompoundNBT tag) {
        abilityUpdater.deserializeStorage(tag, "abilities");
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo();
    }
}

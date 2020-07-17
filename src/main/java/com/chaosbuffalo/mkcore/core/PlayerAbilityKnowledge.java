package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PlayerAbilityKnowledge implements IPlayerSyncComponentProvider {
    private final MKPlayerData playerData;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("abilities");
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final SyncInt poolSize = new SyncInt("poolSize", GameConstants.DEFAULT_ABILITY_POOL_SIZE);
    private final SyncMapUpdater<ResourceLocation, MKAbilityInfo> knownAbilityUpdater =
            new SyncMapUpdater<>("known",
                    () -> abilityInfoMap,
                    ResourceLocation::toString,
                    ResourceLocation::tryCreate,
                    PlayerAbilityKnowledge::createAbilityInfo
            );

    public PlayerAbilityKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        addSyncPrivate(knownAbilityUpdater);
        addSyncPrivate(poolSize);
    }

    public int getAbilityPoolSize() {
        return poolSize.get();
    }

    public void modifyAbilityPoolSize(int delta) {
        poolSize.add(delta);
    }

    public void setAbilityPoolSize(int count) {
        poolSize.set(MathHelper.clamp(count, GameConstants.DEFAULT_ABILITY_POOL_SIZE, GameConstants.MAX_ABILITY_POOL_SIZE));
    }

    private Stream<ResourceLocation> getPoolAbilityStream() {
        // This can be cached easily if it ever becomes a problem
        return getKnownStream()
                .filter(info -> info.getAbility().getType().isPoolAbility())
                .map(MKAbilityInfo::getId);
    }

    public List<ResourceLocation> getPoolAbilities() {
        return getPoolAbilityStream().collect(Collectors.toList());
    }

    public int getCurrentPoolCount() {
        return (int) getPoolAbilityStream().count();
    }

    public boolean isAbilityPoolFull() {
        return getCurrentPoolCount() >= getAbilityPoolSize();
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    @Nullable
    public MKAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        return abilityInfoMap.get(abilityId);
    }

    public Collection<MKAbilityInfo> getAbilities() {
        return Collections.unmodifiableCollection(abilityInfoMap.values());
    }

    public Stream<MKAbilityInfo> getKnownStream() {
        return abilityInfoMap.values().stream().filter(MKAbilityInfo::isCurrentlyKnown);
    }

    public boolean hasRoomForAbility(MKAbility ability) {
        return !ability.getType().isPoolAbility() || !isAbilityPoolFull();
    }

    public boolean learnAbility(MKAbility ability) {
        if (!hasRoomForAbility(ability)) {
            MKCore.LOGGER.warn("Player {} tried to learn pool ability {} with a full pool ({}/{})", playerData::getEntity, ability::getAbilityId, this::getCurrentPoolCount, this::getAbilityPoolSize);
            return false;
        }
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

    public boolean knowsAbility(ResourceLocation abilityId) {
        return getKnownAbilityInfo(abilityId) != null;
    }

    @Nullable
    public MKAbilityInfo getKnownAbilityInfo(ResourceLocation abilityId) {
        MKAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null || !info.isCurrentlyKnown())
            return null;
        return info;
    }

    public void markDirty(MKAbilityInfo info) {
        knownAbilityUpdater.markDirty(info.getId());
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("known", knownAbilityUpdater.serializeStorage());
        tag.putInt("poolSize", poolSize.get());
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
        knownAbilityUpdater.deserializeStorage(tag.get("known"));
        setAbilityPoolSize(tag.getInt("poolSize"));
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo();
    }
}

package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.ResourceListUpdater;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;


public class PlayerAbilityKnowledge implements IPlayerSyncComponentProvider {
    private final MKPlayerData playerData;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("abilities");
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final List<ResourceLocation> abilityPool;
    private final SyncInt poolCount = new SyncInt("poolCount", GameConstants.DEFAULT_ABILITY_POOL_SIZE);
    private final SyncMapUpdater<ResourceLocation, MKAbilityInfo> abilityUpdater =
            new SyncMapUpdater<>("known",
                    () -> abilityInfoMap,
                    MKAbilityInfo::encodeId,
                    MKAbilityInfo::decodeId,
                    PlayerAbilityKnowledge::createAbilityInfo
            );
    private final ResourceListUpdater poolUpdater;

    public PlayerAbilityKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        abilityPool = NonNullList.withSize(GameConstants.MAX_ABILITY_POOL_SIZE, MKCoreRegistry.INVALID_ABILITY);;
        addSyncPrivate(abilityUpdater);
        addSyncPrivate(poolCount);
        poolUpdater = new ResourceListUpdater("abilityPool", () -> abilityPool);
        addSyncPrivate(poolUpdater);
    }

    public int getAbilityPoolSize(){
        return poolCount.get();
    }

    public void addPoolSize(int toAdd){
        poolCount.add(toAdd);
    }

    public void setPoolSize(int count){
        poolCount.set(Math.min(count, GameConstants.MAX_ABILITY_POOL_SIZE));
    }

    public List<ResourceLocation> getAbilityPool() {
        return abilityPool;
    }

    public int getCurrentPoolCount(){
        return (int) abilityPool.stream().filter(x -> !x.equals(MKCoreRegistry.INVALID_ABILITY)).count();
    }

    public boolean isAbilityPoolFull(){
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

    public boolean learnPooledAbility(MKAbility ability, int poolIndex){
        if (poolIndex > GameConstants.MAX_ABILITY_POOL_SIZE || poolIndex > getAbilityPoolSize() - 1){
            return false;
        }
        MKCore.LOGGER.info("Adding {} to the ability pool in slot {}", ability.getAbilityId(), poolIndex);
        if (!abilityPool.get(poolIndex).equals(MKCoreRegistry.INVALID_ABILITY)){
            playerData.getKnowledge().unlearnAbility(abilityPool.get(poolIndex));
        }
        abilityPool.set(poolIndex, ability.getRegistryName());
        poolUpdater.setDirty(poolIndex);
        return learnAbility(ability);
    }

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


    public boolean unlearnAbility(ResourceLocation abilityId) {
        MKAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null) {
            MKCore.LOGGER.error("{} tried to unlearn unknown ability {}", playerData.getEntity(), abilityId);
            return false;
        }
        if (abilityPool.contains(abilityId)){
            int index = abilityPool.indexOf(abilityId);
            abilityPool.set(index, MKCoreRegistry.INVALID_ABILITY);
            poolUpdater.setDirty(index);
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
        abilityUpdater.markDirty(info.getId());
    }

    public void serialize(CompoundNBT tag) {
        abilityUpdater.serializeStorage(tag, "abilities");
        poolUpdater.serializeStorage(tag);
        tag.putInt("poolCount", poolCount.get());
    }

    public void deserialize(CompoundNBT tag) {
        abilityUpdater.deserializeStorage(tag, "abilities");
        poolUpdater.deserializeStorage(tag);
        poolCount.set(tag.getInt("poolCount"));
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo();
    }
}

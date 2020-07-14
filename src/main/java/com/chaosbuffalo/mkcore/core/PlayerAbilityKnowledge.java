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
    private final List<ResourceLocation> abilitySlots;
    private final SyncInt slotCount = new SyncInt("slotCount", GameConstants.DEFAULT_ABILITY_SLOTS);
    private final SyncMapUpdater<ResourceLocation, MKAbilityInfo> abilityUpdater =
            new SyncMapUpdater<>("known",
                    () -> abilityInfoMap,
                    MKAbilityInfo::encodeId,
                    MKAbilityInfo::decodeId,
                    PlayerAbilityKnowledge::createAbilityInfo
            );
    private final ResourceListUpdater slotUpdater;

    public PlayerAbilityKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
        abilitySlots = NonNullList.withSize(GameConstants.MAX_ABILITY_SLOTS, MKCoreRegistry.INVALID_ABILITY);;
        addSyncPrivate(abilityUpdater);
        addSyncPrivate(slotCount);
        slotUpdater = new ResourceListUpdater("abilitySlots", () -> abilitySlots);
        addSyncPrivate(slotUpdater);
    }

    public int getAbilitySlotCount(){
        return slotCount.get();
    }

    public void addSlots(int toAdd){
        slotCount.add(toAdd);
    }

    public void setSlots(int count){
        slotCount.set(Math.min(count, GameConstants.MAX_ABILITY_SLOTS));
    }

    public List<ResourceLocation> getAbilitySlots() {
        return abilitySlots;
    }

    public int getSlottedAbilitiesCount(){
        return (int) abilitySlots.stream().filter(x -> !x.equals(MKCoreRegistry.INVALID_ABILITY)).count();
    }

    public boolean isAbilitySlotsFull(){
        return getSlottedAbilitiesCount() >= getAbilitySlotCount();
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

    public boolean slotAbility(MKAbility ability, int slot){
        if (slot > GameConstants.MAX_ABILITY_SLOTS || slot > getAbilitySlotCount() - 1){
            return false;
        }
        MKCore.LOGGER.info("Slotting {} to {}", ability.getAbilityId(), slot);
        if (!abilitySlots.get(slot).equals(MKCoreRegistry.INVALID_ABILITY)){
            playerData.getKnowledge().unlearnAbility(abilitySlots.get(slot));
        }
        abilitySlots.set(slot, ability.getRegistryName());
        slotUpdater.setDirty(slot);
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
        if (abilitySlots.contains(abilityId)){
            int index = abilitySlots.indexOf(abilityId);
            abilitySlots.set(index, MKCoreRegistry.INVALID_ABILITY);
            slotUpdater.setDirty(index);
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
        slotUpdater.serializeStorage(tag);
        tag.putInt("slotCount", slotCount.get());
    }

    public void deserialize(CompoundNBT tag) {
        abilityUpdater.deserializeStorage(tag, "abilities");
        slotUpdater.deserializeStorage(tag);
        slotCount.set(tag.getInt("slotCount"));
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo();
    }
}

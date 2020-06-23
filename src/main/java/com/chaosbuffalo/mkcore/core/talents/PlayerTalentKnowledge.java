package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.PlayerSyncComponent;
import com.chaosbuffalo.mkcore.sync.ResourceListUpdater;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.sync.SyncListUpdater;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerTalentKnowledge extends PlayerSyncComponent {
    private final MKPlayerData playerData;

    private final SyncInt talentPoints = new SyncInt("points", 0);
    private final SyncInt totalTalentPoints = new SyncInt("totalPoints", 0);
    private final Map<ResourceLocation, TalentTreeRecord> talentTreeRecordMap = new HashMap<>();
    private final List<ResourceLocation> loadedPassives = NonNullList.withSize(GameConstants.MAX_PASSIVES, MKCoreRegistry.INVALID_ABILITY);
    private final List<ResourceLocation> loadedUltimates = NonNullList.withSize(GameConstants.MAX_ULTIMATES, MKCoreRegistry.INVALID_ABILITY);
    private final SyncListUpdater<ResourceLocation> loadedPassivesUpdater = new ResourceListUpdater("passives", () -> loadedPassives);
    private final SyncListUpdater<ResourceLocation> loadedUltimatesUpdater = new ResourceListUpdater("ultimates", () -> loadedUltimates);
    private final KnownTalentCache talentCache = new KnownTalentCache(this);

    public PlayerTalentKnowledge(MKPlayerData playerData) {
        super("talents");
        this.playerData = playerData;
        addPrivate(talentPoints);
        addPrivate(totalTalentPoints);
        addPrivate(loadedPassivesUpdater);
        addPrivate(loadedUltimatesUpdater);
        if (!playerData.isServerSide()) {
            addPrivate(new ClientTreeSyncGroup());
        }
    }

    public int getTotalTalentPoints() {
        return totalTalentPoints.get();
    }

    public int getUnspentTalentPoints() {
        return talentPoints.get();
    }

    public List<ResourceLocation> getActivePassives() {
        return Collections.unmodifiableList(loadedPassives);
    }

    public ResourceLocation getActivePassive(int slot) {
        if (slot < loadedPassives.size()) {
            return loadedPassives.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    public int getAllowedActivePassiveCount() {
        return loadedPassives.size();
    }

    public List<ResourceLocation> getActiveUltimates() {
        return Collections.unmodifiableList(loadedUltimates);
    }

    public ResourceLocation getActiveUltimate(int slot) {
        if (slot < loadedUltimates.size()) {
            return loadedUltimates.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    public int getAllowedActiveUltimateCount() {
        if (talentCache.hasAnyOfType(TalentType.ULTIMATE)) {
            return loadedUltimates.size();
        }
        return 0;
    }

    public Stream<TalentRecord> getKnownTalentsStream() {
        return talentTreeRecordMap.values()
                .stream()
                .flatMap(TalentTreeRecord::getRecordStream)
                .filter(TalentRecord::isKnown);
    }

    public Stream<TalentRecord> getKnownTalentsStream(TalentType<?> type) {
        return getKnownTalentsStream()
                .filter(r -> r.getNode().getTalentType() == type);
    }

    public Collection<ResourceLocation> getKnownTrees() {
        return Collections.unmodifiableCollection(talentTreeRecordMap.keySet());
    }

    public Set<ResourceLocation> getKnownTalentIds(TalentType<?> type) {
        return Collections.unmodifiableSet(talentCache.getKnownTalentIds(type));
    }

    public boolean unlockTree(ResourceLocation treeId) {
        TalentTreeRecord record = unlockTreeInternal(treeId);
        if (record != null) {
            addPrivate(record.getUpdater());
            return true;
        }
        return false;
    }

    private TalentTreeRecord unlockTreeInternal(ResourceLocation treeId) {
        if (talentTreeRecordMap.containsKey(treeId)) {
            MKCore.LOGGER.warn("Player {} tried to unlock already-known talent tree {}", playerData.getEntity(), treeId);
            return null;
        }

        TalentTreeDefinition tree = MKCore.getTalentManager().getTalentTree(treeId);
        if (tree == null) {
            MKCore.LOGGER.warn("Player {} tried to unlock unknown tree {}", playerData.getEntity(), treeId);
            return null;
        }

        TalentTreeRecord record = tree.createRecord();
        talentTreeRecordMap.put(tree.getTreeId(), record);
        return record;
    }

    public boolean knowsTree(ResourceLocation treeId) {
        return talentTreeRecordMap.containsKey(treeId);
    }

    private TalentTreeRecord getTree(ResourceLocation treeId) {
        return talentTreeRecordMap.get(treeId);
    }

    public TalentRecord getRecord(ResourceLocation treeId, String line, int index) {
        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null)
            return null;

        return treeRecord.getNodeRecord(line, index);
    }

    public boolean grantTalentPoints(int amount) {
//        int limit = MKConfig.talentPointLimit.get();
//        if (limit > 0 && totalTalentPoints.get() >= limit) {
//            MKCore.LOGGER.warn("Failed to give {} talent points to player {} - already at server talent limit", amount, playerData.getEntity());
//            return false;
//        }

        if (amount > 0) {
            talentPoints.add(amount);
            totalTalentPoints.add(amount);
            return true;
        }
        return false;
    }

    public boolean removeTalentPoints(int amount) {
        if (amount >= 0 && amount <= talentPoints.get()) {
            talentPoints.add(-amount);
            return true;
        }

        return true;
    }

    public boolean spendTalentPoint(ResourceLocation treeId, String line, int index) {
        if (getUnspentTalentPoints() == 0) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - no unspent points", playerData.getEntity(), treeId, line);
            return false;
        }

        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - tree not known", playerData.getEntity(), treeId, line);
            return false;
        }

        if (!treeRecord.trySpendPoint(line, index)) {
            MKCore.LOGGER.warn("Player {} attempted to spend talent ({}, {}) - prereq not met", playerData.getEntity(), treeId, line);
            return false;
        }

        talentCache.invalidate();
        talentPoints.add(-1);

        TalentRecord record = treeRecord.getNodeRecord(line, index);
        if (record != null) {
            playerData.getTalentHandler().onTalentRecordUpdated(record);
        }
        return true;
    }

    public boolean refundTalentPoint(ResourceLocation treeId, String line, int index) {
        TalentTreeRecord treeRecord = getTree(treeId);
        if (treeRecord == null) {
            MKCore.LOGGER.warn("Player {} attempted to unlearn talent in unknown tree {}", playerData.getEntity(), treeId);
            return false;
        }

        if (!treeRecord.tryRefundPoint(line, index)) {
            MKCore.LOGGER.warn("Player {} attempted to refund talent ({}, {}) - prereq not met", playerData.getEntity(), treeId, line);
            return false;
        }

        talentCache.invalidate();
        talentPoints.add(1);

        TalentRecord record = treeRecord.getNodeRecord(line, index);
        if (record != null) {
            playerData.getTalentHandler().onTalentRecordUpdated(record);
        }
        return true;
    }

    public void setActivePassive(int index, ResourceLocation talentId) {
//        MKCore.LOGGER.info("PlayerTalentKnowledge.setActivePassive {} {}", index, talentId);

        if (talentId.equals(MKCoreRegistry.INVALID_TALENT)) {
            setPassiveSlot(index, MKCoreRegistry.INVALID_ABILITY);
            return;
        }

        if (getKnownTalentIds(TalentType.PASSIVE).contains(talentId)) {
            MKAbility ability = TalentManager.getTalentAbility(talentId);
            if (ability == null) {
                MKCore.LOGGER.error("PlayerTalentKnowledge.setActivePassive {} {} - talent does provide ability!", index, talentId);
                return;
            }

            setActivePassiveAbility(index, ability.getAbilityId());
        } else {
            MKCore.LOGGER.error("PlayerTalentKnowledge.setActivePassive {} {} - player does not know passive!", index, talentId);
            setPassiveSlot(index, MKCoreRegistry.INVALID_ABILITY);
        }
    }

    public void setActivePassiveAbility(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.info("setActivePassiveAbility({}, {})", index, abilityId);

        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            clearPassiveSlot(index);
            return;
        }

        if (!playerData.getKnowledge().knowsAbility(abilityId)) {
            MKCore.LOGGER.info("setActivePassiveAbility({}, {}) - player does not know ability!", index, abilityId);
            return;
        }

        if (index < loadedPassives.size()) {
            for (int i = 0; i < loadedPassives.size(); i++) {
                if (!abilityId.equals(MKCoreRegistry.INVALID_ABILITY) && i != index && abilityId.equals(loadedPassives.get(i))) {
                    setPassiveSlot(i, loadedPassives.get(index));
                }
            }
            setPassiveSlot(index, abilityId);
        }
    }

    public void clearPassive(MKAbility ability) {
//        MKCore.LOGGER.info("PlayerTalentKnowledge.clearPassive {}", ability);
        ResourceLocation abilityId = ability.getAbilityId();
        for (int i = 0; i < loadedPassives.size(); i++) {
            if (abilityId.equals(loadedPassives.get(i))) {
                MKCore.LOGGER.info("PlayerTalentKnowledge.clearPassive found at {}", i);
                setPassiveSlot(i, MKCoreRegistry.INVALID_ABILITY);
                return;
            }
        }
    }

    private void clearPassiveSlot(int index) {
        setPassiveSlot(index, MKCoreRegistry.INVALID_ABILITY);
    }

    private void setPassiveSlot(int index, ResourceLocation abilityId) {
//        MKCore.LOGGER.info("PlayerTalentKnowledge.setPassiveSlot {} {}", index, abilityId);
        ResourceLocation previous = loadedPassives.set(index, abilityId);
        loadedPassivesUpdater.setDirty(index);
        if (playerData.getEntity().isAddedToWorld()) {
            playerData.getTalentHandler().getTypeHandler(TalentType.PASSIVE).onSlotChanged(index, previous, abilityId);
        }
    }

    public void clearUltimate(MKAbility ability) {
//        MKCore.LOGGER.info("PlayerTalentKnowledge.clearUltimate {}", ability);
        ResourceLocation abilityId = ability.getAbilityId();
        for (int i = 0; i < loadedUltimates.size(); i++) {
            if (abilityId.equals(loadedUltimates.get(i))) {
//                MKCore.LOGGER.info("PlayerTalentKnowledge.clearUltimate found at {}", i);
                clearUltimateSlot(i);
                return;
            }
        }
    }

    private void clearUltimateSlot(int slot) {
        setUltimateSlot(slot, MKCoreRegistry.INVALID_ABILITY);
    }

    public void setActiveUltimate(int index, ResourceLocation talentId) {
        if (talentId.equals(MKCoreRegistry.INVALID_TALENT)) {
            clearUltimateSlot(index);
            return;
        }

        if (getKnownTalentIds(TalentType.ULTIMATE).contains(talentId)) {
            MKAbility ability = TalentManager.getTalentAbility(talentId);
            if (ability == null) {
                MKCore.LOGGER.error("PlayerTalentKnowledge.setActiveUltimate {} {} - talent does provide ability!", index, talentId);
                return;
            }
            ResourceLocation abilityId = ability.getAbilityId();

            setActiveUltimateAbility(index, abilityId);
        } else {
            MKCore.LOGGER.error("PlayerTalentKnowledge.setActiveUltimate {} {} - player does not know ultimate!", index, talentId);
            clearUltimateSlot(index);
        }
    }

    public void setActiveUltimateAbility(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.info("setActiveUltimateAbility({}, {})", index, abilityId);

        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            clearUltimateSlot(index);
            return;
        }

        if (!playerData.getKnowledge().knowsAbility(abilityId)) {
            MKCore.LOGGER.info("setActiveUltimateAbility({}, {}) - player does not know ability!", index, abilityId);
            return;
        }

        ResourceLocation currentAbility = loadedUltimates.get(index);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY) && !currentAbility.equals(MKCoreRegistry.INVALID_ABILITY)) {
            // TODO: is this even reachable? UltimateTalent.getAbility would need to return INVALID_ABILITY
            clearUltimateSlot(index);
        } else {
            if (!currentAbility.equals(MKCoreRegistry.INVALID_ABILITY)) {
                clearUltimateSlot(index);
            }
            setUltimateSlot(index, abilityId);
        }
    }

    // TODO: see if this can be addressed another way
    public boolean isActiveUltimate(ResourceLocation abilityId) {
        return loadedUltimates.stream().anyMatch(id -> id.equals(abilityId));
    }

    public boolean isKnownUltimateAbility(ResourceLocation abilityId) {
        return getKnownTalentsStream(TalentType.ULTIMATE)
                .map(r -> ((UltimateTalent) r.getNode().getTalent()).getAbility().getAbilityId())
                .anyMatch(id -> id.equals(abilityId));
    }

    private void setUltimateSlot(int index, ResourceLocation abilityId) {
//        MKCore.LOGGER.info("PlayerTalentKnowledge.setUltimateSlot {} {}", index, abilityId);
        if (index < loadedUltimates.size()) {
            ResourceLocation previous = loadedUltimates.set(index, abilityId);
            loadedUltimatesUpdater.setDirty(index);
            if (playerData.getEntity().isAddedToWorld()) {
                playerData.getTalentHandler().getTypeHandler(TalentType.ULTIMATE).onSlotChanged(index, previous, abilityId);
            }
        }
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("talentPoints"), ops.createInt(talentPoints.get()));
        builder.put(ops.createString("totalPoints"), ops.createInt(totalTalentPoints.get()));
        builder.put(ops.createString("trees"), ops.createMap(talentTreeRecordMap.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                kv -> ops.createString(kv.getKey().toString()),
                                kv -> kv.getValue().serialize(ops)
                        )
                )));

        builder.put(ops.createString("loadedPassives"),
                ops.createList(loadedPassives.stream().map(ResourceLocation::toString).map(ops::createString)));

        builder.put(ops.createString("loadedUltimates"),
                ops.createList(loadedUltimates.stream().map(ResourceLocation::toString).map(ops::createString)));

        T value = ops.createMap(builder.build());
//        MKCore.LOGGER.info("talents serialize {}", value);
        return value;
    }

    public <T> void deserialize(Dynamic<T> dynamic) {
//        MKCore.LOGGER.info("talents deserialize {}", dynamic);
        talentPoints.set(dynamic.get("talentPoints").asInt(0));
        totalTalentPoints.set(dynamic.get("totalPoints").asInt(0));

        dynamic.get("trees")
                .asMap(Dynamic::asString, Function.identity())
                .forEach((idOpt, dyn) -> idOpt.map(ResourceLocation::new).ifPresent(id -> deserializeTree(id, dyn)));

        deserializeAbilityList(dynamic, "loadedPassives", this::setPassiveSlot);
        deserializeAbilityList(dynamic, "loadedUltimates", this::setUltimateSlot);

        talentCache.invalidate();
    }

    private <T> void deserializeAbilityList(Dynamic<T> dynamic, String fieldName, BiConsumer<Integer, ResourceLocation> consumer) {
        List<Optional<String>> passives = dynamic.get(fieldName).asList(Dynamic::asString);
        for (int i = 0; i < passives.size(); i++) {
            int index = i;
            passives.get(i).ifPresent(idString -> {
                ResourceLocation abilityId = new ResourceLocation(idString);
                MKAbility ability = MKCoreRegistry.getAbility(abilityId);
//                MKCore.LOGGER.info("PlayerTalentKnowledge.deserializeAbilityList {} {} {} {}", fieldName, index, abilityId, ability);
                if (ability != null) {
                    consumer.accept(index, abilityId);
                }
            });
        }
    }

    private <T> void deserializeTree(ResourceLocation id, Dynamic<T> dyn) {
        if (unlockTree(id)) {
//            MKCore.LOGGER.info("PlayerTalentKnowledge.deserializeTree unlocked tree {}", id);
            if (!getTree(id).deserialize(dyn)) {
                MKCore.LOGGER.error("Player {} had invalid talent layout. Needs reset.", playerData.getEntity());
            }
        } else {
            MKCore.LOGGER.error("PlayerTalentKnowledge.deserializeTree failed for tree {} {}", id, dyn);
        }
    }

    public INBT serializeNBT() {
        return serialize(NBTDynamicOps.INSTANCE);
    }

    public void deserializeNBT(INBT tag) {
        deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, tag));
    }

    private static class KnownTalentCache {
        private final Map<ResourceLocation, TalentTreeRecord> parent;
        private final Map<TalentType<?>, Set<BaseTalent>> typeCache = new HashMap<>();
        private boolean needsRebuild;

        public KnownTalentCache(PlayerTalentKnowledge knowledge) {
            parent = knowledge.talentTreeRecordMap;
            invalidate();
        }

        public void invalidate() {
            needsRebuild = true;
        }

        public void rebuild() {
            typeCache.clear();
            parent.forEach((treeId, treeRecord) ->
                    treeRecord.getRecordStream().forEach(record -> {
                        TalentNode node = record.getNode();
                        if (record.isKnown()) {
                            typeCache.computeIfAbsent(node.getTalentType(), type -> new HashSet<>()).add(node.getTalent());
                        }
                    }));
        }

        private Map<TalentType<?>, Set<BaseTalent>> getTypeCache() {
            if (needsRebuild) {
                rebuild();
                needsRebuild = false;
            }
            return typeCache;
        }

        public boolean hasAnyOfType(TalentType<?> type) {
            return !getTypeCache().getOrDefault(type, Collections.emptySet()).isEmpty();
        }

        public Set<ResourceLocation> getKnownTalentIds(TalentType<?> type) {
            return Collections.unmodifiableSet(getTypeCache()
                    .getOrDefault(type, Collections.emptySet()).stream()
                    .map(ForgeRegistryEntry::getRegistryName)
                    .collect(Collectors.toSet()));
        }
    }

    class ClientTreeSyncGroup extends SyncGroup {

        @Override
        public void deserializeUpdate(CompoundNBT tag) {
            MKCore.getTalentManager()
                    .getTreeNames()
                    .stream()
                    .filter(treeId -> tag.contains(treeId.toString()))
                    .filter(treeId -> !talentTreeRecordMap.containsKey(treeId))
                    .map(PlayerTalentKnowledge.this::unlockTreeInternal)
                    .filter(Objects::nonNull)
                    .forEach(record -> {
                        record.setUpdateCallback(ignore -> talentCache.invalidate());
                        add(record.getUpdater());
                    });

            super.deserializeUpdate(tag);
        }

        @Override
        public void serializeUpdate(CompoundNBT tag) {
            throw new IllegalStateException("ClientTreeSyncGroup should never call serializeUpdate!");
        }

        @Override
        public void serializeFull(CompoundNBT tag) {
            throw new IllegalStateException("ClientTreeSyncGroup should never call serializeFull!");
        }
    }
}

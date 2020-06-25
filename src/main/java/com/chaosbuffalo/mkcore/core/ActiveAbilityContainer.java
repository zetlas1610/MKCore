package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.sync.ResourceListUpdater;
import com.chaosbuffalo.mkcore.sync.SyncListUpdater;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ActiveAbilityContainer extends PlayerSyncComponent implements IActiveAbilityContainer {
    protected final MKPlayerData playerData;
    protected final String name;
    private final List<ResourceLocation> activeAbilities;
    private final SyncListUpdater<ResourceLocation> activeUpdater;
    protected final MKAbility.AbilityType type;
    protected int currentSize;

    public ActiveAbilityContainer(MKPlayerData playerData, String name, MKAbility.AbilityType type, int defaultSize, int max) {
        super(name);
        this.playerData = playerData;
        this.name = name;
        activeAbilities = NonNullList.withSize(max, MKCoreRegistry.INVALID_ABILITY);
        activeUpdater = new ResourceListUpdater("active", () -> activeAbilities);
        this.type = type;
        this.currentSize = defaultSize;
        addPrivate(activeUpdater);
    }

    public MKAbility.AbilityType getType() {
        return type;
    }

    @Override
    public List<ResourceLocation> getAbilities() {
        return Collections.unmodifiableList(activeAbilities);
    }

    protected boolean canSlotAbility(int slot, ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return false;

        return ability.getType() == type;
    }

    protected int getFirstFreeAbilitySlot() {
        return getSlotForAbility(MKCoreRegistry.INVALID_ABILITY);
    }

    @Override
    public int tryPlaceOnBar(ResourceLocation abilityId) {
        int slot = getSlotForAbility(abilityId);
        if (slot == GameConstants.ACTION_BAR_INVALID_SLOT) {
            // Skill was just learned so let's try to put it on the bar
            slot = getFirstFreeAbilitySlot();
            if (slot != GameConstants.ACTION_BAR_INVALID_SLOT && slot < getCurrentSlotCount()) {
                setAbilityInSlot(slot, abilityId);
            }
        }

        return slot;
    }

    @Override
    public void setAbilityInSlot(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.info("ActiveAbilityContainer.setAbilityInSlot({}, {}, {})", type, index, abilityId);

        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            setSlotInternal(index, MKCoreRegistry.INVALID_ABILITY);
            return;
        }

        if (!playerData.getKnowledge().knowsAbility(abilityId)) {
            MKCore.LOGGER.error("setAbilityInSlot({}, {}, {}) - player does not know ability!", type, index, abilityId);
            return;
        }

        if (!canSlotAbility(index, abilityId)) {
            return;
        }

        if (index < activeAbilities.size()) {
            for (int i = 0; i < activeAbilities.size(); i++) {
                if (i != index && abilityId.equals(activeAbilities.get(i))) {
                    setSlotInternal(i, activeAbilities.get(index));
                }
            }
            setSlotInternal(index, abilityId);
        }
    }

    @Override
    public void clearAbility(ResourceLocation abilityId) {
        int slot = getSlotForAbility(abilityId);
        if (slot != GameConstants.ACTION_BAR_INVALID_SLOT) {
            clearSlot(slot);
        }
    }

    @Override
    public void clearSlot(int slot) {
        setAbilityInSlot(slot, MKCoreRegistry.INVALID_ABILITY);
    }

    protected void setSlotInternal(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.info("ActiveAbilityContainer.setSlotInternal({}, {}, {})", type, index, abilityId);
        ResourceLocation previous = activeAbilities.set(index, abilityId);
        activeUpdater.setDirty(index);
        if (playerData.getEntity().isAddedToWorld()) {
            onSlotChanged(index, previous, abilityId);
        }
    }

    protected void onSlotChanged(int index, ResourceLocation previous, ResourceLocation newAbility) {
        playerData.getAbilityExecutor().onSlotChanged(type, index, previous, newAbility);
    }

    @Override
    public int getCurrentSlotCount() {
        return currentSize;
    }

    @Override
    public int getMaximumSlotCount() {
        return activeAbilities.size();
    }

    public <T> T serialize(DynamicOps<T> ops) {
        return ops.createList(activeAbilities.stream().map(ResourceLocation::toString).map(ops::createString));
    }

    public <T> void deserialize(Dynamic<T> dynamic) {
        deserializeAbilityList(dynamic, this::setSlotInternal);
    }

    private <T> void deserializeAbilityList(Dynamic<T> dynamic, BiConsumer<Integer, ResourceLocation> consumer) {
        List<Optional<String>> passives = dynamic.asList(Dynamic::asString);
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
}

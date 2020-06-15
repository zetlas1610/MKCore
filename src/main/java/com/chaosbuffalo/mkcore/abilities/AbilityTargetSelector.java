package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;

import java.util.Set;
import java.util.function.BiFunction;

public class AbilityTargetSelector {

    private final BiFunction<IMKEntityData, MKAbility, AbilityContext> selector;
    private Set<MemoryModuleType<?>> requiredMemories;
    private String description; // TODO: i18n

    public AbilityTargetSelector(BiFunction<IMKEntityData, MKAbility, AbilityContext> selector) {
        this.selector = selector;
        description = "";
    }

    public AbilityTargetSelector setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AbilityTargetSelector setRequiredMemories(Set<MemoryModuleType<?>> types) {
        requiredMemories = types;
        return this;
    }

    public BiFunction<IMKEntityData, MKAbility, AbilityContext> getSelector() {
        return selector;
    }

    public AbilityContext createContext(IMKEntityData entityData, MKAbility ability) {
        return selector.apply(entityData, ability);
    }

    public boolean validateContext(IMKEntityData entityData, AbilityContext context) {
        MKCore.LOGGER.info("AbilityTargetSelector.validateContext {}", entityData.getEntity());
        return requiredMemories == null || requiredMemories.stream().allMatch(context::hasMemory);
    }
}

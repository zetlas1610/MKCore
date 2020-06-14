package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AbilityContext {
    public static AbilityContext EMPTY = new AbilityContext(ImmutableMap.of());

    private final Map<MemoryModuleType<?>, Optional<?>> memories;

    public AbilityContext() {
        memories = new HashMap<>();
    }

    private AbilityContext(Map<MemoryModuleType<?>, Optional<?>> memories) {
        this.memories = memories;
    }

    public <U> void setMemory(MemoryModuleType<U> memoryType, Optional<U> value) {
        memories.put(memoryType, value);
    }

    public <U> AbilityContext withMemory(MemoryModuleType<U> memoryType, Optional<U> value) {
        setMemory(memoryType, value);
        return this;
    }

    public <U> AbilityContext withBrainMemory(LivingEntity entity, MemoryModuleType<U> memoryType) {
        Optional<U> value = entity.getBrain().getMemory(memoryType);
        setMemory(memoryType, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getMemory(MemoryModuleType<T> memory) {
        return (Optional<T>) memories.get(memory);
    }

    public static AbilityContext singleTarget(LivingEntity target) {
        return new AbilityContext().withMemory(MKMemoryModuleTypes.ABILITY_TARGET, Optional.ofNullable(target));
    }
}

package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;

import java.util.Optional;

public class BrainAbilityContext extends AbilityContext {
    private final Brain<?> brain;

    public BrainAbilityContext(LivingEntity entity) {
        brain = entity.getBrain();
    }

    public <U> void setMemory(MemoryModuleType<U> memoryType,
                              @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<U> value) {
        brain.setMemory(memoryType, value);
    }

    public <T> Optional<T> getMemory(MemoryModuleType<T> memory) {
        return brain.getMemory(memory);
    }
}

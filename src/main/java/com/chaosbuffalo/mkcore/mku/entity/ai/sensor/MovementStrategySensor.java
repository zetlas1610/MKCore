package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKUMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;

public class MovementStrategySensor extends Sensor<CreatureEntity> {


    @Override
    protected void update(ServerWorld worldIn, CreatureEntity entityIn) {
        entityIn.getBrain().getMemory(MKUMemoryModuleTypes.MOVEMENT_STRATEGY).ifPresent(
                movementStrategy -> movementStrategy.update(worldIn, entityIn));
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKUMemoryModuleTypes.THREAT_TARGET, MKUMemoryModuleTypes.VISIBLE_ENEMIES,
                MemoryModuleType.WALK_TARGET, MKUMemoryModuleTypes.MOVEMENT_STRATEGY);
    }
}

package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKUMemoryModuleTypes;
import com.chaosbuffalo.targeting_api.Targeting;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

public class LivingEntitiesSensor extends Sensor<LivingEntity> {

    public LivingEntitiesSensor() {
        super(50);
    }

    protected void update(ServerWorld worldIn, LivingEntity entityIn) {
        List<LivingEntity> entities = worldIn.getEntitiesWithinAABB(LivingEntity.class,
                entityIn.getBoundingBox().grow(16.0D, 16.0D, 16.0D),
                (entity) -> entity != entityIn && entity.isAlive());
        entities.sort(Comparator.comparingDouble(entityIn::getDistanceSq));
        Brain<?> brain = entityIn.getBrain();

        Map<Targeting.TargetRelation, List<LivingEntity>> groups = entities.stream()
                .collect(Collectors.groupingBy(other -> Targeting.getTargetRelation(entityIn, other)));

        List<LivingEntity> enemies = groups.getOrDefault(Targeting.TargetRelation.ENEMY, Collections.emptyList());
        List<LivingEntity> friends = groups.getOrDefault(Targeting.TargetRelation.FRIEND, Collections.emptyList())
                .stream()
                .sorted(this::sortByHealth)
                .collect(Collectors.toList());
        brain.setMemory(MKUMemoryModuleTypes.ENEMIES, enemies);
        brain.setMemory(MKUMemoryModuleTypes.ALLIES, friends);
        brain.setMemory(MKUMemoryModuleTypes.VISIBLE_ENEMIES, enemies.stream().filter(entityIn::canEntityBeSeen)
                .collect(Collectors.toList()));
    }

    private int sortByHealth(LivingEntity friend, LivingEntity other) {
        return Float.compare(friend.getHealth() / friend.getMaxHealth(), other.getHealth() / other.getMaxHealth());
    }

    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKUMemoryModuleTypes.ENEMIES, MKUMemoryModuleTypes.ALLIES,
                MKUMemoryModuleTypes.VISIBLE_ENEMIES);
    }
}

package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.targeting_api.Targeting;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MKLivingEntitiesSensor extends Sensor<LivingEntity> {

    public MKLivingEntitiesSensor(){
        super(20);
    }

    protected void update(ServerWorld worldIn, LivingEntity entityIn) {
        List<LivingEntity> entities = worldIn.getEntitiesWithinAABB(LivingEntity.class,
                entityIn.getBoundingBox().grow(16.0D, 16.0D, 16.0D),
                (entity) -> entity != entityIn && entity.isAlive());
        entities.sort(Comparator.comparingDouble(entityIn::getDistanceSq));
        Brain<?> brain = entityIn.getBrain();
        List<LivingEntity> enemies = entities.stream().filter((x) -> Targeting.isValidEnemy(entityIn, x))
                .collect(Collectors.toList());
        List<LivingEntity> friends = entities.stream().filter((x) -> Targeting.isValidFriendly(entityIn, x))
                .collect(Collectors.toList());
        brain.setMemory(MKMemoryModuleTypes.ENEMIES, enemies);
        brain.setMemory(MKMemoryModuleTypes.ALLIES, friends);
        brain.setMemory(MKMemoryModuleTypes.VISIBLE_ENEMIES, enemies.stream().filter(entityIn::canEntityBeSeen)
                .collect(Collectors.toList()));
    }

    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKMemoryModuleTypes.ENEMIES, MKMemoryModuleTypes.ALLIES,
                MKMemoryModuleTypes.VISIBLE_ENEMIES);
    }
}

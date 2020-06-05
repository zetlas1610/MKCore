package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.ThreatMapEntry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

public class MKThreatSensor extends Sensor<LivingEntity> {

    private static final float THREAT_DISTANCE_2 = 100.0f;
    private static final float MAX_THREAT_FROM_CLOSENESS = 10.0f;

    @Override
    protected void update(ServerWorld worldIn, LivingEntity entityIn) {
        Optional<List<LivingEntity>> enemyOpt = entityIn.getBrain().getMemory(MKMemoryModuleTypes.VISIBLE_ENEMIES);
        Optional<Map<LivingEntity, ThreatMapEntry>> opt = entityIn.getBrain().getMemory(
                MKMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> threatMap = opt.orElse(new HashMap<>());
        if (enemyOpt.isPresent()){
            List<LivingEntity> enemies = enemyOpt.get();
            for (LivingEntity enemy : enemies){
                float dist2 = (float) entityIn.getDistanceSq(enemy);
                if (dist2 < THREAT_DISTANCE_2){
                    ThreatMapEntry entry = threatMap.getOrDefault(enemy, new ThreatMapEntry());
                    threatMap.put(enemy, entry.addThreat(Math.round(
                            (1.0f - dist2 / THREAT_DISTANCE_2) * MAX_THREAT_FROM_CLOSENESS)));
                }
            }
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.THREAT_MAP, threatMap);
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.THREAT_LIST, threatMap.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> -entry.getValue().getCurrentThreat()))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
        }



    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKMemoryModuleTypes.THREAT_MAP, MKMemoryModuleTypes.VISIBLE_ENEMIES,
                MKMemoryModuleTypes.THREAT_LIST);
    }
}

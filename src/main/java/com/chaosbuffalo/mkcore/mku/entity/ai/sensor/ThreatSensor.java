package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.ThreatMapEntry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

public class ThreatSensor extends Sensor<LivingEntity> {

    private static final float THREAT_DISTANCE_2 = 100.0f;
    private static final float MAX_THREAT_FROM_CLOSENESS = 10.0f;


    @Override
    protected void update(ServerWorld worldIn, LivingEntity entityIn) {
        Optional<List<LivingEntity>> enemyOpt = entityIn.getBrain().getMemory(MKMemoryModuleTypes.VISIBLE_ENEMIES);
        Optional<Map<LivingEntity, ThreatMapEntry>> opt = entityIn.getBrain().getMemory(
                MKMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> threatMap = opt.orElse(new HashMap<>());
        Optional<LivingEntity> targetOpt = entityIn.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        if (targetOpt.isPresent() && !targetOpt.get().isAlive()){
            entityIn.getBrain().removeMemory(MKMemoryModuleTypes.THREAT_TARGET);
        }
        if (enemyOpt.isPresent()){
            List<LivingEntity> enemies = enemyOpt.get();
            Map<LivingEntity, ThreatMapEntry> newThreatMap = new HashMap<>();
            for (LivingEntity enemy : enemies){
                float dist2 = (float) entityIn.getDistanceSq(enemy);
                if (dist2 < THREAT_DISTANCE_2){
                    ThreatMapEntry entry = threatMap.getOrDefault(enemy, new ThreatMapEntry());
                    newThreatMap.put(enemy, entry.addThreat(Math.round(
                            (1.0f - dist2 / THREAT_DISTANCE_2) * MAX_THREAT_FROM_CLOSENESS)));
                }
            }
            List<LivingEntity> sortedThreat = newThreatMap.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> -entry.getValue().getCurrentThreat()))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.THREAT_MAP, newThreatMap);
            entityIn.getBrain().setMemory(MKMemoryModuleTypes.THREAT_LIST, sortedThreat);
            if (sortedThreat.size() > 0){
                entityIn.getBrain().setMemory(MKMemoryModuleTypes.THREAT_TARGET, sortedThreat.get(0));
            } else {
                entityIn.getBrain().removeMemory(MKMemoryModuleTypes.THREAT_TARGET);
            }
        }



    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKMemoryModuleTypes.THREAT_MAP, MKMemoryModuleTypes.VISIBLE_ENEMIES,
                MKMemoryModuleTypes.THREAT_LIST);
    }
}

package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.ThreatMapEntry;
import com.chaosbuffalo.mkcore.mku.entity.ai.sensor.MKSensorTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class MKEntity extends CreatureEntity {

    protected MKEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public void addThreat(LivingEntity entity, int value) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).addThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP, newMap);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    public void reduceThreat(LivingEntity entity, int value) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).subtractThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP, newMap);
    }

    @Override
    public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setAttackTarget(entitylivingbaseIn);
        brain.setMemory(MKMemoryModuleTypes.MOVEMENT_TARGET, entitylivingbaseIn);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        this.world.getProfiler().startSection("brain");
        this.getBrain().tick((ServerWorld)this.world, this);
        this.world.getProfiler().endSection();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() instanceof LivingEntity){
            addThreat((LivingEntity) source.getTrueSource(), Math.round(amount));
        }
        return super.attackEntityFrom(source, amount);
    }



    @Override
    public Brain<MKEntity> getBrain() {
        return (Brain<MKEntity>) super.getBrain();
    }

    @Override
    protected Brain<MKEntity> createBrain(Dynamic<?> dynamicIn) {
        return new Brain<>(
                ImmutableList.of(
                        MKMemoryModuleTypes.ALLIES,
                        MKMemoryModuleTypes.ENEMIES,
                        MKMemoryModuleTypes.THREAT_LIST,
                        MKMemoryModuleTypes.THREAT_MAP,
                        MKMemoryModuleTypes.VISIBLE_ENEMIES,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.PATH,
                        MKMemoryModuleTypes.MOVEMENT_STRATEGY,
                        MKMemoryModuleTypes.MOVEMENT_TARGET),
                ImmutableList.of(
                        MKSensorTypes.ENTITIES_SENSOR,
                        MKSensorTypes.THREAT_SENSOR,
                        MKSensorTypes.DESTINATION_SENSOR),
                dynamicIn);
    }

}

package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.ai.MKNearestAttackableTargetGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.ThreatMapEntry;
import com.chaosbuffalo.mkcore.mku.entity.ai.sensor.MKLivingEntitiesSensor;
import com.chaosbuffalo.mkcore.mku.entity.ai.sensor.MKSensorTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class GreenLadyEntity extends ZombieEntity implements IMKEntity {

    public GreenLadyEntity(EntityType<? extends GreenLadyEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected boolean shouldBurnInDay() {
        return false;
    }

    @Override
    public Brain<GreenLadyEntity> getBrain() {
        return (Brain<GreenLadyEntity>) super.getBrain();
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        this.world.getProfiler().startSection("brain");
        this.getBrain().tick((ServerWorld)this.world, this);
        this.world.getProfiler().endSection();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(2, new MKNearestAttackableTargetGoal(this, true,
                true));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
    }

    @Override
    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        super.damageEntity(damageSrc, damageAmount);
        if (damageSrc.getTrueSource() instanceof LivingEntity){
            MKCore.LOGGER.info("Adding threat {} to {}", damageAmount, damageSrc.getTrueSource());
            addThreat((LivingEntity) damageSrc.getTrueSource(), Math.round(damageAmount));
        }
    }

    @Override
    protected Brain<GreenLadyEntity> createBrain(Dynamic<?> dynamicIn) {
        return new Brain<>(ImmutableList.of(MKMemoryModuleTypes.ALLIES, MKMemoryModuleTypes.ENEMIES,
                MKMemoryModuleTypes.THREAT_LIST, MKMemoryModuleTypes.THREAT_MAP, MKMemoryModuleTypes.VISIBLE_ENEMIES),
                ImmutableList.of(MKSensorTypes.ENTITIES_SENSOR, MKSensorTypes.THREAT_SENSOR), dynamicIn);
    }

    @Override
    public void addThreat(LivingEntity entity, int value) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).addThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP, newMap);
    }

    @Override
    public void reduceThreat(LivingEntity entity, int value) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).subtractThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP, newMap);
    }
}

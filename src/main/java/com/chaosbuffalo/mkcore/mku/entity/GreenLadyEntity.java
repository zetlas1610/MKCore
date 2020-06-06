package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.ai.LookAtThreatTargetGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.MovementGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.TargetEnemyGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.controller.MovementStrategyController;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.ThreatMapEntry;
import com.chaosbuffalo.mkcore.mku.entity.ai.sensor.MKSensorTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class GreenLadyEntity extends ZombieEntity implements IMKEntity {
    private int timesDone;
    private boolean isWalkingBackwards;

    public GreenLadyEntity(EntityType<? extends GreenLadyEntity> type, World worldIn) {
        super(type, worldIn);
        timesDone = 0;
        isWalkingBackwards = false;
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
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        if (!player.getEntityWorld().isRemote()){
            if (timesDone % 3 == 0){
                MovementStrategyController.enterMeleeMode(this, 1);
            } else if (timesDone % 3 == 1){
                MovementStrategyController.enterCastingMode(this, 8.0);
            } else {
                MovementStrategyController.enterStationary(this);
            }
            timesDone++;
        }
        return ActionResultType.SUCCESS;
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(7, new LookAtThreatTargetGoal(this));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(2, new TargetEnemyGoal(this, true,
                true));
//        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new MovementGoal(this));
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
        return new Brain<>(
                ImmutableList.of(
                        MKMemoryModuleTypes.ALLIES,
                        MKMemoryModuleTypes.ENEMIES,
                        MKMemoryModuleTypes.THREAT_LIST,
                        MKMemoryModuleTypes.THREAT_MAP,
                        MKMemoryModuleTypes.VISIBLE_ENEMIES,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.PATH,
                        MKMemoryModuleTypes.MOVEMENT_STRATEGY),
                ImmutableList.of(
                        MKSensorTypes.ENTITIES_SENSOR,
                        MKSensorTypes.THREAT_SENSOR,
                        MKSensorTypes.DESTINATION_SENSOR),
                dynamicIn);
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

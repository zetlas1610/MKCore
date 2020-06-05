package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.mku.entity.ai.MKNearestAttackableTargetGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.ThreatMapEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

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
            addThreat((LivingEntity) damageSrc.getTrueSource(), Math.round(damageAmount));
        }
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

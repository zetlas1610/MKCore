package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKUMemoryModuleTypes;
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
    private int castAnimTimer;
    private VisualCastState visualCastState;
    private MKAbility castingAbility;

    public enum VisualCastState {
        NONE,
        CASTING,
        RELEASE,
    }

    protected MKEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
        super(type, worldIn);
        castAnimTimer = 0;
        visualCastState = VisualCastState.NONE;
        castingAbility = null;
        getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent((mkEntityData -> {
            mkEntityData.getAbilityExecutor().setStartCastCallback(this::startCast);
            mkEntityData.getAbilityExecutor().setCompleteAbilityCallback(this::endCast);
        }));
    }

    public void addThreat(LivingEntity entity, int value) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKUMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).addThreat(value));
        this.brain.setMemory(MKUMemoryModuleTypes.THREAT_MAP, newMap);
    }

    protected void updateEntityCastState() {
        if (castAnimTimer > 0) {
            castAnimTimer--;
            if (castAnimTimer == 0) {
                castingAbility = null;
                visualCastState = VisualCastState.NONE;
            }
        }
    }


    @Override
    public void livingTick() {
        updateArmSwingProgress();
        updateEntityCastState();
        super.livingTick();
    }

    public VisualCastState getVisualCastState() {
        return visualCastState;
    }

    public int getCastAnimTimer() {
        return castAnimTimer;
    }

    public MKAbility getCastingAbility() {
        return castingAbility;
    }

    public void startCast(MKAbility ability) {
        visualCastState = VisualCastState.CASTING;
        castingAbility = ability;
    }

    public void endCast(MKAbility ability) {
        castingAbility = ability;
        visualCastState = VisualCastState.RELEASE;
        castAnimTimer = 15;
    }

    public abstract void enterDefaultMovementState(LivingEntity target);

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    public void reduceThreat(LivingEntity entity, int value) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKUMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).subtractThreat(value));
        this.brain.setMemory(MKUMemoryModuleTypes.THREAT_MAP, newMap);
    }

    @Override
    public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setAttackTarget(entitylivingbaseIn);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        this.world.getProfiler().startSection("brain");
        this.getBrain().tick((ServerWorld) this.world, this);
        this.world.getProfiler().endSection();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() instanceof LivingEntity) {
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
                        MKUMemoryModuleTypes.ALLIES,
                        MKUMemoryModuleTypes.ENEMIES,
                        MKUMemoryModuleTypes.THREAT_LIST,
                        MKUMemoryModuleTypes.THREAT_MAP,
                        MKUMemoryModuleTypes.VISIBLE_ENEMIES,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.PATH,
                        MKUMemoryModuleTypes.MOVEMENT_STRATEGY,
                        MKUMemoryModuleTypes.MOVEMENT_TARGET,
                        MKUMemoryModuleTypes.CURRENT_ABILITY,
                        MKAbilityMemories.ABILITY_TARGET),
                ImmutableList.of(
                        MKSensorTypes.ENTITIES_SENSOR,
                        MKSensorTypes.THREAT_SENSOR,
                        MKSensorTypes.DESTINATION_SENSOR,
                        MKSensorTypes.ABILITY_SENSOR),
                dynamicIn);
    }

}

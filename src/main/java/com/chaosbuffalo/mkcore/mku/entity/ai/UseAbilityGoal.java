package com.chaosbuffalo.mkcore.mku.entity.ai;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;
import java.util.Optional;

public class UseAbilityGoal extends Goal {

    private final MKEntity entity;
    private MKAbility currentAbility;
    private LivingEntity target;

    public UseAbilityGoal(MKEntity entity){
        this.entity = entity;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        Optional<MKAbility> abilityOptional = entity.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY);
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        if (abilityOptional.isPresent() && target.isPresent()){
            currentAbility = abilityOptional.get();
            this.target = target.get();
            MKCore.LOGGER.info("In should execute: {}, {}",
                    entity.getEntitySenses().canSee(this.target), canActivate());
            return entity.getEntitySenses().canSee(this.target) && canActivate();
        } else {
            return false;
        }
    }

    public boolean canActivate(){
        return entity.getCapability(Capabilities.ENTITY_CAPABILITY).map((entityData) ->
                entityData.getAbilityExecutor().canActivateAbility(currentAbility))
                .orElse(false);
    }

    public boolean shouldContinueExecuting() {
        boolean shouldContinue = currentAbility != null && entity.getCapability(Capabilities.ENTITY_CAPABILITY).map(
                (entityData) -> entityData.getAbilityExecutor().isCasting()).orElse(false) &&
                target != null && entity.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET).map(
                (ent) -> ent.isEntityEqual(target)).orElse(false);
        MKCore.LOGGER.info("Shold continue: {} ", shouldContinue);
        return shouldContinue;
    }

    @Override
    public void startExecuting() {
        entity.faceEntity(target, 360.0f, 360.0f);
        entity.getLookController().setLookPositionWithEntity(target, 50.0f, 50.0f);
        entity.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent((entityData) ->{
                MKCore.LOGGER.info("Start execute for ability: {}", currentAbility);
                entityData.getAbilityExecutor().executeAbility(currentAbility.getAbilityId());
        });
    }

    @Override
    public void tick() {
        entity.faceEntity(target, 50.0f, 50.0f);
        entity.getLookController().setLookPositionWithEntity(target, 50.0f, 50.0f);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        MKCore.LOGGER.info("Resetting ability use");
        currentAbility = null;
        target = null;
        entity.getBrain().removeMemory(MKMemoryModuleTypes.CURRENT_ABILITY);
    }
}

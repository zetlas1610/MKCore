package com.chaosbuffalo.mkcore.mku.entity.ai;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Optional;

public class UseAbilityGoal extends Goal {

    private final MKEntity entity;
    private MKAbility currentAbility;
    private LivingEntity target;

    public UseAbilityGoal(MKEntity entity) {
        this.entity = entity;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean isPreemptible() {
        return false;
    }

    @Override
    public boolean shouldExecute() {
        Optional<MKAbility> abilityOptional = entity.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY);
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKMemoryModuleTypes.ABILITY_TARGET);
        if (abilityOptional.isPresent() && target.isPresent()) {
            currentAbility = abilityOptional.get();
            this.target = target.get();
            return entity.getEntitySenses().canSee(this.target) && canActivate();
        } else {
            return false;
        }
    }

    public boolean canActivate() {
        return entity.getCapability(Capabilities.ENTITY_CAPABILITY).map((entityData) ->
                entityData.getAbilityExecutor().canActivateAbility(currentAbility))
                .orElse(false);
    }

    public boolean shouldContinueExecuting() {
        return entity.getCapability(Capabilities.ENTITY_CAPABILITY).map(
                (entityData) -> entityData.getAbilityExecutor().isCasting()).orElse(false);
    }

    @Override
    public void startExecuting() {
        entity.faceEntity(target, 360.0f, 360.0f);
        entity.getLookController().setLookPositionWithEntity(target, 50.0f, 50.0f);
        AbilityContext context = new AbilityContext().withBrainMemory(entity, MKMemoryModuleTypes.ABILITY_TARGET);
        MKCore.LOGGER.info("ai {} casting {} on {}", entity, currentAbility.getAbilityId(), target);
        entity.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent(
                (entityData) -> entityData.getAbilityExecutor().executeAbilityWithContext(currentAbility.getAbilityId(), context));
    }

    @Override
    public void tick() {
        entity.faceEntity(target, 50.0f, 50.0f);
        entity.getLookController().setLookPositionWithEntity(target, 50.0f, 50.0f);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        currentAbility = null;
        target = null;
        entity.getBrain().removeMemory(MKMemoryModuleTypes.CURRENT_ABILITY);
    }
}

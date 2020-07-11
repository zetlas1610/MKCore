package com.chaosbuffalo.mkcore.mku.entity.ai;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.BrainAbilityContext;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKUMemoryModuleTypes;
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
        Optional<MKAbility> abilityOptional = entity.getBrain().getMemory(MKUMemoryModuleTypes.CURRENT_ABILITY);
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKAbilityMemories.ABILITY_TARGET);
        if (abilityOptional.isPresent() && target.isPresent()) {
            currentAbility = abilityOptional.get();
            LivingEntity targetEntity = target.get();

            if (!canActivate())
                return false;

            if (entity != targetEntity) {
                if (!isInRange(currentAbility, targetEntity))
                    return false;
                if (!entity.getEntitySenses().canSee(targetEntity))
                    return false;
            }

            // Now we know we can actually start the cast
            this.target = targetEntity;
            return true;
        } else {
            return false;
        }
    }

    protected boolean isInRange(MKAbility ability, LivingEntity target) {
        float range = ability.getDistance();
        return target.getDistanceSq(entity) <= range * range;
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
        AbilityContext context = new BrainAbilityContext(entity);
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
        entity.getBrain().removeMemory(MKUMemoryModuleTypes.CURRENT_ABILITY);
    }
}

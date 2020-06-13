package com.chaosbuffalo.mkcore.mku.entity.ai;

import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Hand;

import java.util.EnumSet;
import java.util.Optional;

public class MKMeleeAttackGoal extends Goal {
    private final MKEntity entity;
    private double speed;
    private LivingEntity target;
    private double delayCounter;

    @Override
    public boolean shouldExecute() {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        if (targetOpt.isPresent()) {
            LivingEntity target = targetOpt.get();
            if (isInReach(target)) {
                this.target = target;
                return true;
            }
        }
        return false;
    }

    public MKMeleeAttackGoal(MKEntity entity, double speedIn) {
        this.entity = entity;
        this.speed = speedIn;
        this.delayCounter = 0;
        this.target = null;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void startExecuting() {
        this.entity.setAggroed(true);
        this.delayCounter = 0;
    }

    @Override
    public void tick() {
        this.delayCounter = Math.max(delayCounter - 1, 0);
        entity.getNavigator().tryMoveToEntityLiving(target, speed);
        entity.getLookController().setLookPositionWithEntity(target, 30.0f, 30.0f);
        if (delayCounter == 0) {
            checkAndPerformAttack(target, entity.getDistanceSq(target));
        }

    }

    protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);
        if (distToEnemySqr <= d0 && this.delayCounter <= 0) {
            this.delayCounter = 20;
            this.entity.swingArm(Hand.MAIN_HAND);
            this.entity.attackEntityAsMob(enemy);
        }
    }

    public boolean isInReach(LivingEntity target) {
        return entity.getDistanceSq(target) <= this.getAttackReachSqr(target);
    }

    public void resetTask() {
        this.entity.setAggroed(false);
        this.delayCounter = 20;
        this.target = null;
    }

    protected double getAttackReachSqr(LivingEntity attackTarget) {
        return entity.getWidth() * 2.0F * entity.getWidth() * 2.0F + attackTarget.getWidth();
    }

    @Override
    public boolean shouldContinueExecuting() {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        return target != null && targetOpt.map((ent) -> ent.isEntityEqual(target) && isInReach(ent)).orElse(false);
    }


}

package com.chaosbuffalo.mkcore.mku.entity.ai;


import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKUMemoryModuleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;

import java.util.Optional;

public class TargetEnemyGoal extends TargetGoal {

    public TargetEnemyGoal(MobEntity mobIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(mobIn, checkSight, nearbyOnlyIn);
    }

    @Override
    public boolean shouldExecute() {
        Optional<LivingEntity> opt = goalOwner.getBrain().getMemory(MKUMemoryModuleTypes.THREAT_TARGET);
        if (opt.isPresent() && (this.target == null || !this.target.isEntityEqual(opt.get()))) {
            this.target = opt.get();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        Optional<LivingEntity> opt = goalOwner.getBrain().getMemory(MKUMemoryModuleTypes.THREAT_TARGET);
        return opt.isPresent() && opt.get().isEntityEqual(target);
    }

    @Override
    public void resetTask() {
        super.resetTask();
    }

    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.target);
        super.startExecuting();
    }
}

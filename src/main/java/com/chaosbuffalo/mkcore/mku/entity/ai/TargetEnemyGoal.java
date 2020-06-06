package com.chaosbuffalo.mkcore.mku.entity.ai;


import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;

import java.util.Optional;

public class TargetEnemyGoal extends TargetGoal {
    protected LivingEntity nearestTarget;

    public TargetEnemyGoal(MobEntity mobIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(mobIn, checkSight, nearbyOnlyIn);
    }

    @Override
    public boolean shouldExecute() {
        Optional<LivingEntity> opt = goalOwner.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        if (opt.isPresent() && (this.nearestTarget == null || !this.nearestTarget.isEntityEqual(opt.get()))){
            this.nearestTarget = opt.get();
            return true;
        }
        return false;
    }


    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.nearestTarget);
        super.startExecuting();
    }
}

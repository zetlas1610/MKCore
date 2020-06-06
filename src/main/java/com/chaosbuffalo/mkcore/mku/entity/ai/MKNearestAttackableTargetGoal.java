package com.chaosbuffalo.mkcore.mku.entity.ai;


import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;

import java.util.List;
import java.util.Optional;

public class MKNearestAttackableTargetGoal extends TargetGoal {
    protected LivingEntity nearestTarget;

    public MKNearestAttackableTargetGoal(MobEntity mobIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(mobIn, checkSight, nearbyOnlyIn);
    }

    @Override
    public boolean shouldExecute() {
        Optional<List<LivingEntity>> opt = goalOwner.getBrain().getMemory(MKMemoryModuleTypes.THREAT_LIST);
        if (opt.isPresent() && opt.get().size() > 0){
            this.nearestTarget = opt.get().get(0);
            return true;
        }
        return false;
    }


    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.nearestTarget);
        super.startExecuting();
    }
}

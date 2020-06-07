package com.chaosbuffalo.mkcore.mku.entity.ai;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Optional;

public class LookAtThreatTargetGoal extends Goal {
    private final MobEntity entity;
    private LivingEntity target;

    public LookAtThreatTargetGoal(MobEntity entity){
        this.entity = entity;
        setMutexFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        if (target.isPresent()){
            this.target = target.get();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        Optional<LivingEntity> target = entity.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        return target.isPresent() && this.target != null && this.target.isEntityEqual(target.get());
    }

    @Override
    public void resetTask() {
        this.target = null;
    }

    @Override
    public void tick() {
        this.entity.getLookController().setLookPosition(this.target.getPosX(), this.target.getPosYEye(),
                this.target.getPosZ());
    }
}

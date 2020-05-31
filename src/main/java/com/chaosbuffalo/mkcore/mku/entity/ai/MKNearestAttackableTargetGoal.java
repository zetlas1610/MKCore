package com.chaosbuffalo.mkcore.mku.entity.ai;


import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;

public class MKNearestAttackableTargetGoal extends TargetGoal {
    protected LivingEntity nearestTarget;
    protected final int targetChance;
    protected EntityPredicate targetEntitySelector;

    public MKNearestAttackableTargetGoal(MobEntity mobIn, boolean checkSight, boolean nearbyOnlyIn) {
        super(mobIn, checkSight, nearbyOnlyIn);
        this.targetChance = 4;
        targetEntitySelector = new EntityPredicate().setDistance(this.getTargetDistance()).setCustomPredicate(
                (entity) -> Targeting.isValidTarget(TargetingContexts.ENEMY, this.goalOwner, entity));
    }

    @Override
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.goalOwner.getRNG().nextInt(this.targetChance) != 0) {
            return false;
        } else {
            this.findNearestTarget();
            return this.nearestTarget != null;
        }
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.goalOwner.getBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    protected void findNearestTarget() {
        List<LivingEntity> entities = this.goalOwner.world.getLoadedEntitiesWithinAABB(LivingEntity.class,
                getTargetableArea(getTargetDistance()));
        this.nearestTarget =  this.goalOwner.world.getClosestEntity(entities, targetEntitySelector,
                this.goalOwner, this.goalOwner.getPosX(), this.goalOwner.getPosY(), this.goalOwner.getPosZ());
    }

    public void startExecuting() {
        this.goalOwner.setAttackTarget(this.nearestTarget);
        super.startExecuting();
    }
}

package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.ai.LookAtThreatTargetGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.MKMeleeAttackGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.MovementGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.TargetEnemyGoal;
import com.chaosbuffalo.mkcore.mku.entity.ai.controller.MovementStrategyController;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;



public class GreenLadyEntity extends MKEntity {
    private int timesDone;

    public GreenLadyEntity(EntityType<? extends GreenLadyEntity> type, World worldIn) {
        super(type, worldIn);
        timesDone = 0;
    }


    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        if (!player.getEntityWorld().isRemote()){
            if (timesDone % 3 == 0){
                MovementStrategyController.enterMeleeMode(this, 1);
            } else if (timesDone % 3 == 1){
                MovementStrategyController.enterCastingMode(this, 8.0);
            } else {
                MovementStrategyController.enterStationary(this);
            }
            timesDone++;
        }
        return ActionResultType.SUCCESS;
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(7, new LookAtThreatTargetGoal(this));
        this.targetSelector.addGoal(2, new TargetEnemyGoal(this, true,
                true));
        this.goalSelector.addGoal(2, new MovementGoal(this));
        this.goalSelector.addGoal(3, new MKMeleeAttackGoal(this, .25));
    }


}

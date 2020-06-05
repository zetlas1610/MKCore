package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.mku.entity.ai.MKNearestAttackableTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;


public class GreenLadyEntity extends ZombieEntity implements IMKEntity {

    public GreenLadyEntity(EntityType<? extends GreenLadyEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected boolean shouldBurnInDay() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(2, new MKNearestAttackableTargetGoal(this, true,
                true));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
    }
}

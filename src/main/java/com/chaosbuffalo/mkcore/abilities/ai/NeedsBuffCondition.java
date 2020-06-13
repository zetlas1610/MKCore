package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.FollowMovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.MovementStrategy;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;

import javax.annotation.Nullable;

public class NeedsBuffCondition extends AbilityUseCondition {

    private final Effect buffEffect;
    private final MovementStrategy movementStrategy;
    private boolean selfOnly;


    public NeedsBuffCondition(MKAbility ability, Effect buffEffect) {
        super(ability);
        this.buffEffect = buffEffect;
        this.movementStrategy = new FollowMovementStrategy(1.0f,
                Math.round(getAbility().getDistance()));
        selfOnly = false;
    }

    public NeedsBuffCondition setSelfOnly(boolean selfOnly) {
        this.selfOnly = selfOnly;
        return this;
    }

    private boolean needsBuff(LivingEntity entity){
        return entity.getActivePotionEffect(buffEffect) == null;
    }

    @Override
    public boolean test(AbilityUseContext context) {
        if (needsBuff(context.getCaster())){
            return true;
        }
        if (!selfOnly){
            for (LivingEntity friendly : context.getFriendlies()){
                if (needsBuff(friendly)){
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public AbilityTarget getTarget(AbilityUseContext context) {
        if (needsBuff(context.getCaster())){
            return new AbilityTarget(context.getCaster());
        }
        if (!selfOnly){
            for (LivingEntity friendly : context.getFriendlies()){
                if (needsBuff(friendly)){
                    return new AbilityTarget(friendly, movementStrategy);
                }
            }
        }
        return null;
    }

}

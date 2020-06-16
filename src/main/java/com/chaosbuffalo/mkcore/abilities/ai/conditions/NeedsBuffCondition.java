package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.FollowMovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.MovementStrategy;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;

import javax.annotation.Nonnull;

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

    private boolean needsBuff(LivingEntity entity) {
        return entity.getActivePotionEffect(buffEffect) == null;
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(AbilityDecisionContext context) {
        if (getAbility().canSelfCast() && needsBuff(context.getCaster())) {
            return new AbilityTargetingDecision(context.getCaster());
        }
        if (!selfOnly) {
            for (LivingEntity friendly : context.getFriendlies()) {
                if (needsBuff(friendly) && isInRange(context, friendly)) {
                    return new AbilityTargetingDecision(friendly, movementStrategy);
                }
            }
        }
        return AbilityTargetingDecision.UNDECIDED;
    }

}

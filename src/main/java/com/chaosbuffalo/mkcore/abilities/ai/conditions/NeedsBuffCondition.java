package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;

import javax.annotation.Nonnull;

public class NeedsBuffCondition extends AbilityUseCondition {

    private final Effect buffEffect;
    private final AbilityTargetingDecision.MovementSuggestion movementSuggestion;
    private boolean selfOnly;


    public NeedsBuffCondition(MKAbility ability, Effect buffEffect) {
        super(ability);
        this.buffEffect = buffEffect;
        this.movementSuggestion = AbilityTargetingDecision.MovementSuggestion.FOLLOW;
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
            return new AbilityTargetingDecision(context.getCaster(), getAbility());
        }
        if (!selfOnly) {
            for (LivingEntity friendly : context.getFriendlies()) {
                if (needsBuff(friendly)) {
                    return new AbilityTargetingDecision(friendly, movementSuggestion, getAbility());
                }
            }
        }
        return AbilityTargetingDecision.UNDECIDED;
    }

}

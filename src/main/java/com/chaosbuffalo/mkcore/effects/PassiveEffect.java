package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public abstract class PassiveEffect extends SpellEffectBase implements IMKInfiniteEffect {

    protected PassiveEffect(EffectType typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public void doEffect(Entity applier, Entity caster, LivingEntity target, int amplifier, SpellCast cast) {
        attemptInfiniteEffectRefresh(target, this);
    }

    @Override
    public boolean canSelfCast() {
        return true;
    }

    @Override
    public boolean isReady(int duration, int amplitude) {
        // Don't do anything until it's time to refresh
        return needsInfiniteEffectRefresh(duration);
    }

    @Override
    public boolean isInstant() {
        return false;
    }

    @Override
    public boolean canPersistAcrossSessions() {
        return false;
    }

    public EffectInstance createSelfCastEffectInstance(LivingEntity caster, int amplifier) {
        return newSpellCast(caster).setTarget(caster).toPotionEffect(getPassiveDuration(), amplifier);
    }
}

package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

public interface IMKInfiniteEffect {
    int MAX_PASSIVE_DURATION = 32767;
    // This duration makes the client show **:** for the time if it is being rendered
    int DEFAULT_PASSIVE_DURATION = MAX_PASSIVE_DURATION;
    //    int DEFAULT_PASSIVE_DURATION = 1400;
    // This should stay above 600 so the client is refreshed before hitting 0
    int DEFAULT_REFRESH_DURATION = 700;

    default int getRefreshDuration() {
        return DEFAULT_REFRESH_DURATION;
    }

    default int getPassiveDuration() {
        return DEFAULT_PASSIVE_DURATION;
    }

    default void attemptInfiniteEffectRefresh(LivingEntity target, Effect spellEffect) {
        attemptInfiniteEffectRefresh(target, spellEffect, 1);
    }

    default boolean attemptInfiniteEffectRefresh(LivingEntity target, Effect spellEffect, int period) {
        if (target.world.isRemote)
            return false;

        EffectInstance effect = target.getActivePotionEffect(spellEffect);
        if (effect == null)
            return false;
//        MKCore.LOGGER.info("IMKPassiveEffect.checkRefresh() {} {}", target, effect);

//        MKCore.LOGGER.info("IMKPassiveEffect.checkRefresh() cur:{} r:{} p:{} refrdy:{} prdrdy:{}", effect.getDuration(), getRefreshDuration(), getPassiveDuration(), effect.getDuration() <= getRefreshDuration(), effect.getDuration() % period == 0);

        int remaining = effect.getDuration() % period;
        boolean periodTriggered = remaining == 0;
        if (effect.getDuration() <= getRefreshDuration()) {
            // Create a duplicate effect, and then combine them to extend the original

            // Adjust the new duration so the period happens on the correct tick
            int nextDuration = getPassiveDuration() + remaining;
            EffectInstance extend = new EffectInstance(spellEffect, nextDuration, effect.getAmplifier(),
                    effect.isAmbient(), effect.doesShowParticles(), effect.isShowIcon());
            extend.setCurativeItems(effect.getCurativeItems());
            boolean changed = effect.combine(extend);
            MKCore.LOGGER.debug("attemptInfiniteEffectRefresh combined changed {} {}", changed, effect);
        }

        return periodTriggered;
    }

    default boolean needsInfiniteEffectRefresh(int duration) {
        return duration <= getRefreshDuration();
    }
}

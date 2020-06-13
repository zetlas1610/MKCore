package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKSongAbility;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

import java.util.HashSet;
import java.util.Set;

/**
 * Effect applied to the caster that will periodically create the area effect.
 * This effect is itself a periodic effect applied only to the caster, so that even if the player disables the ability
 * toggle that applied this effect, the periodic area effect will continue to be cast until this expires.
 */
public abstract class SongCasterEffect extends SongEffectBase implements IMKInfiniteEffect {
    protected SongCasterEffect(int period, EffectType typeIn, int liquidColorIn) {
        super(period, false, typeIn, liquidColorIn);
    }

    protected Set<EffectInstance> getPotionsToApply(Entity source, int level) {
        return new HashSet<>();
    }

    protected Set<SpellCast> getSpellCasts(Entity source) {
        return new HashSet<>();
    }

    @Override
    public void doEffect(Entity source, Entity indirectSource, LivingEntity target, int amplifier, SpellCast cast) {
        if (!attemptInfiniteEffectRefresh(target, this, getPeriod()))
            return;

        MKCore.getPlayer(source).ifPresent(playerData -> {
            LivingEntity entity = playerData.getEntity();
            MKSongAbility ability = MKSongAbility.getAbilityForCasterEffect(this);
            if (ability == null) {
                MKCore.LOGGER.error("ERROR: SongCasterEffect cast ability null!");
                return;
            }

            if (playerData.getAbilityExecutor().isCasting() ||
                    !playerData.getStats().consumeMana(ability.getCasterEffectSustainCost(playerData))) {
                entity.removePotionEffect(this);
                return;
            }

            getPotionsToApply(entity, amplifier).forEach(entity::addPotionEffect);

            for (SpellCast toCast : getSpellCasts(entity)) {
                entity.addPotionEffect(toCast.setTarget(entity).toPotionEffect(getPeriod(), amplifier));
            }

            PacketHandler.sendToTrackingMaybeSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.NOTE,
                            ParticleEffects.CIRCLE_MOTION, 12, 4,
                            target.getPosX(), target.getPosY() + 1.0f,
                            target.getPosZ(), .25, .25, .25, .5,
                            target.getLookVec()),
                    target);
        });
    }

    public EffectInstance createSelfCastEffectInstance(LivingEntity caster, int amplifier) {
        // Round up to the next multiple of period to avoid inconsistent tick lengths
        int minDuration = (int) (getPeriod() * Math.ceil((float) getPassiveDuration() / getPeriod()));
        return newSpellCast(caster).setTarget(caster).toPotionEffect(minDuration, amplifier);
    }

    @Override
    public boolean isReady(int duration, int amplitude) {
        return super.isReady(duration, amplitude) || needsInfiniteEffectRefresh(duration);
    }
}

package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectType;

public abstract class SongAreaEffect extends SongPotionBase {
    protected SongAreaEffect(int period, EffectType typeIn, int liquidColorIn) {
        super(period, true, typeIn, liquidColorIn);
    }

    public AreaEffectBuilder prepareAreaEffect(LivingEntity source, IMKEntityData entityData, int level,
                                               AreaEffectBuilder builder) {
        return builder;
    }

    public abstract float getSongDistance(int level);

    public IParticleData getSongParticle() {
        return ParticleTypes.NOTE;
    }

    @Override
    public void doEffect(Entity applier, Entity caster, LivingEntity target, int amplifier, SpellCast cast) {
        MKCore.getPlayer(applier).ifPresent(playerData -> {
            LivingEntity entity = playerData.getEntity();
            if (!playerData.getStats().consumeMana(amplifier)) {
                entity.removePotionEffect(this);
            }

            AreaEffectBuilder builder = AreaEffectBuilder.createOnEntity(entity)
                    .instant()
                    .particle(getSongParticle())
                    .color(16762905)
                    .radius(getSongDistance(amplifier), true);
            prepareAreaEffect(entity, playerData, amplifier, builder).spawn();
        });
    }
}

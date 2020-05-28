package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectType;

public abstract class SongEffect extends SongPotionBase {
    protected SongEffect(int period, EffectType typeIn, int liquidColorIn) {
        super(period, true, typeIn, liquidColorIn);
    }

    public AreaEffectBuilder prepareAreaEffect(PlayerEntity source, IMKEntityData playerData, int level, AreaEffectBuilder builder) {
        return builder;
    }

    public abstract float getSongDistance(int level);

    public IParticleData getSongParticle() {
        return ParticleTypes.NOTE;
    }

    @Override
    public void doEffect(Entity source, Entity indirectSource, LivingEntity target, int amplifier, SpellCast cast) {

        if (source instanceof PlayerEntity) {
            MKCore.getPlayer((PlayerEntity) source).ifPresent(pData -> {
                PlayerEntity player = pData.getPlayer();
                if (!pData.consumeMana(amplifier)) {
                    player.removePotionEffect(this);
                }

                AreaEffectBuilder builder = AreaEffectBuilder.Create(player, player)
                        .instant()
                        .particle(getSongParticle())
                        .color(16762905)
                        .radius(getSongDistance(amplifier), true);
                prepareAreaEffect(player, pData, amplifier, builder).spawn();
            });
        }
    }
}

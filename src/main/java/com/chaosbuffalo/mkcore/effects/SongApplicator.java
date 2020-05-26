package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerToggleAbility;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

import java.util.HashSet;
import java.util.Set;

public abstract class SongApplicator extends SongPotionBase {
    protected SongApplicator(int period, EffectType typeIn, int liquidColorIn) {
        super(period, false, typeIn, liquidColorIn);
    }

    public Set<EffectInstance> getPotionsToApply(Entity source, int level) {
        return new HashSet<>();
    }

    public Set<SpellCast> getSpellCasts(Entity source) {
        return new HashSet<>();
    }

    @Override
    public void doEffect(Entity source, Entity indirectSource, LivingEntity target, int amplifier, SpellCast cast) {
        if (source instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source;
            MKCore.getPlayer(player).ifPresent(pData -> {
                PlayerAbility ability = MKCoreRegistry.getAbility(PlayerToggleAbility.getToggleAbilityIdForPotion(this));
                if (ability == null)
                    return;


                if (pData.getAbilityExecutor().isCasting() || !pData.consumeMana(ability.getManaCost(amplifier))) {
                    player.removePotionEffect(this);
                }

                getPotionsToApply(player, amplifier).forEach(player::addPotionEffect);

                for (SpellCast toCast : getSpellCasts(player)) {
                    player.addPotionEffect(toCast.setTarget(player).toPotionEffect(getPeriod(), amplifier));
                }

                PacketHandler.sendToTracking(
                        new ParticleEffectSpawnPacket(
                                ParticleTypes.NOTE,
                                ParticleEffects.CIRCLE_MOTION, 12, 4,
                                target.getPosX(), target.getPosY() + 1.0f,
                                target.getPosZ(), .25, .25, .25, .5,
                                target.getLookVec()),
                        target);
            });
        }
    }
}

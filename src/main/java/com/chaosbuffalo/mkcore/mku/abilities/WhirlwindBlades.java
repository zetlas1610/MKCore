package com.chaosbuffalo.mkcore.mku.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ParticleEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.mku.effects.AbilityMagicDamage;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WhirlwindBlades extends MKAbility {
    public static WhirlwindBlades INSTANCE = new WhirlwindBlades();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static float BASE_DAMAGE = 2.0f;
    public static float DAMAGE_SCALE = 1.0f;

    private WhirlwindBlades() {
        super(MKCore.makeRL("ability.whirlwind_blades"));
        setCastTime(GameConstants.TICKS_PER_SECOND * 3);
        setCooldownSeconds(20);
        setManaCost(6);
    }

    @Override
    public boolean canApplyCastingSpeedModifier() {
        return false;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance() {
        return 4f;
    }

    //    @Override
//    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.spell_whirlwind_1;
//    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void continueCast(LivingEntity entity, IMKEntityData data, int castTimeLeft, AbilityContext context) {
        super.continueCast(entity, data, castTimeLeft, context);
        int tickSpeed = 6;
        if (castTimeLeft % tickSpeed == 0) {
            int level = 1;
            int totalDuration = getCastTime(data);
            int count = (totalDuration - castTimeLeft) / tickSpeed;
            float baseAmount = level > 1 ? 0.10f : 0.15f;
            float scaling = count * baseAmount;
            // What to do for each target hit
            SpellCast damage = AbilityMagicDamage.Create(entity, BASE_DAMAGE, DAMAGE_SCALE, scaling);
            SpellCast particlePotion = ParticleEffect.Create(entity,
                    ParticleTypes.SWEEP_ATTACK,
                    ParticleEffects.CIRCLE_MOTION, false,
                    new Vec3d(1.0, 1.0, 1.0),
                    new Vec3d(0.0, 1.0, 0.0),
                    4, 0, 1.0);


            AreaEffectBuilder.createOnCaster(entity)
                    .spellCast(damage, level, getTargetContext())
                    .spellCast(particlePotion, level, getTargetContext())
//                    .spellCast(SoundPotion.Create(entity, ModSounds.spell_shadow_2, SoundCategory.PLAYERS),
//                            1, getTargetType())
                    .instant()
                    .color(16409620).radius(getDistance(), true)
                    .particle(ParticleTypes.CRIT)
                    .spawn();

            PacketHandler.sendToTrackingMaybeSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.SWEEP_ATTACK,
                            ParticleEffects.SPHERE_MOTION, 16, 4,
                            entity.getPosX(), entity.getPosY() + 1.0,
                            entity.getPosZ(), 1.0, 1.0, 1.0, 1.5,
                            entity.getLookVec()), entity);
        }
    }
}

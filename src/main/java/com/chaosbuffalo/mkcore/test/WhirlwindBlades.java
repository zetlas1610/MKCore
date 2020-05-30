package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ParticlePotion;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.Contexts;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
    }

    @Override
    public TargetingContext getTargetContext() {
        return Contexts.ENEMY;
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
    public void continueCast(LivingEntity entity, IMKEntityData data, int castTimeLeft, CastState state) {
        super.continueCast(entity, data, castTimeLeft, state);
        int tickSpeed = 6;
        if (castTimeLeft % tickSpeed == 0) {
            int level = 1;
            int totalDuration = getCastTime();
            int count = (totalDuration - castTimeLeft) / tickSpeed;
            float baseAmount = level > 1 ? 0.10f : 0.15f;
            float scaling = count * baseAmount;
            // What to do for each target hit
            SpellCast damage = AbilityMagicDamage.Create(entity, BASE_DAMAGE, DAMAGE_SCALE, scaling);
            SpellCast particlePotion = ParticlePotion.Create(entity,
                    ParticleTypes.SWEEP_ATTACK,
                    ParticleEffects.CIRCLE_MOTION, false,
                    new Vec3d(1.0, 1.0, 1.0),
                    new Vec3d(0.0, 1.0, 0.0),
                    4, 0, 1.0);


            AreaEffectBuilder.Create(entity, entity)
                    .spellCast(damage, level, getTargetContext())
                    .spellCast(particlePotion, level, getTargetContext())
//                    .spellCast(SoundPotion.Create(entity, ModSounds.spell_shadow_2, SoundCategory.PLAYERS),
//                            1, getTargetType())
                    .instant()
                    .color(16409620).radius(getDistance(), true)
                    .particle(ParticleTypes.CRIT)
                    .spawn();

            Vec3d lookVec = entity.getLookVec();
            PacketHandler.sendToTrackingAndSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.SWEEP_ATTACK,
                            ParticleEffects.SPHERE_MOTION, 16, 4,
                            entity.getPosX(), entity.getPosY() + 1.0,
                            entity.getPosZ(), 1.0, 1.0, 1.0, 1.5,
                            lookVec), (ServerPlayerEntity) entity);
        }
    }

    @Override
    public void execute(LivingEntity entity, IMKEntityData entityData) {
        entityData.startAbility(this);
    }
}

package com.chaosbuffalo.mkcore.mku.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ParticleEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.mku.effects.FeatherFallEffect;
import com.chaosbuffalo.mkcore.mku.effects.PhoenixAspectEffect;
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
public class PhoenixAspectAbility extends MKAbility {
    public static PhoenixAspectAbility INSTANCE = new PhoenixAspectAbility();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static int BASE_DURATION = 60;
    public static int DURATION_SCALE = 60;

    private PhoenixAspectAbility() {
        super(MKCore.MOD_ID, "ability.phoenix_aspect");
    }

    @Override
    public int getCooldown() {
        return 500 - 100;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public float getManaCost() {
        return 10 + 5;
    }

    @Override
    public float getDistance() {
        return 10.0f + 2.0f * 1;
    }

    @Override
    public int getCastTime() {
        return GameConstants.TICKS_PER_SECOND * 3;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return ModSounds.casting_fire;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return ModSounds.spell_buff_8;
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        int level = 1;

        // What to do for each target hit
        int duration = (BASE_DURATION + DURATION_SCALE * level) * GameConstants.TICKS_PER_SECOND;
//        duration = PlayerFormulas.applyBuffDurationBonus(data, duration);
        SpellCast flying = PhoenixAspectEffect.INSTANCE.newSpellCast(entity);
        SpellCast feather = FeatherFallEffect.INSTANCE.newSpellCast(entity);
        SpellCast particlePotion = ParticleEffect.Create(entity,
                ParticleTypes.FIREWORK,
                ParticleEffects.DIRECTED_SPOUT, false, new Vec3d(1.0, 1.5, 1.0),
                new Vec3d(0.0, 1.0, 0.0), 40, 5, 1.0);

        AreaEffectBuilder.createOnCaster(entity)
                .spellCast(flying, duration, level, getTargetContext())
                .spellCast(feather, duration + 10 * GameConstants.TICKS_PER_SECOND, level, getTargetContext())
                .spellCast(particlePotion, level, getTargetContext())
                .instant()
                .particle(ParticleTypes.FIREWORK)
                .color(65480).radius(getDistance(), true)
                .spawn();

        PacketHandler.sendToTrackingMaybeSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.FIREWORK,
                ParticleEffects.CIRCLE_MOTION, 50, 0,
                entity.getPosX(), entity.getPosY() + 1.5,
                entity.getPosZ(), 1.0, 1.0, 1.0, 1.0f,
                entity.getLookVec()), entity);
    }
}

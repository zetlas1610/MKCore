package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.NeedsBuffCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ParticlePotion;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FireArmor extends MKAbility {
    public static final FireArmor INSTANCE = new FireArmor();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static int BASE_DURATION = 60;
    public static int DURATION_SCALE = 30;

    private FireArmor() {
        super(MKCore.makeRL("ability.fire_armor"));
        setUseCondition(new NeedsBuffCondition(this, Effects.FIRE_RESISTANCE));
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }


    @Override
    public float getDistance() {
        return 12f;
    }

    //    @Nullable
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_buff_5;
//    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, CastState state) {
        super.endCast(entity, data, state);
        int level = 1;

        // What to do for each target hit
        int duration = (BASE_DURATION + DURATION_SCALE * level) * GameConstants.TICKS_PER_SECOND;
//        duration = PlayerFormulas.applyBuffDurationBonus(data, duration);

        EffectInstance absorbEffect = new EffectInstance(Effects.ABSORPTION, duration, level + 1, false, true);

        EffectInstance fireResistanceEffect = new EffectInstance(Effects.FIRE_RESISTANCE, duration, level, false, true);

        SpellCast particlePotion = ParticlePotion.Create(entity,
                ParticleTypes.FLAME,
                ParticleEffects.CIRCLE_PILLAR_MOTION, false,
                new Vec3d(1.0, 1.0, 1.0),
                new Vec3d(0.0, 1.0, 0.0),
                40, 5, .1f);

        AreaEffectBuilder.Create(entity, entity)
                .effect(absorbEffect, getTargetContext())
                .effect(fireResistanceEffect, getTargetContext())
                .spellCast(particlePotion, level, getTargetContext())
//                .spellCast(SoundPotion.Create(entity, ModSounds.spell_fire_2, SoundCategory.PLAYERS),
//                        1, getTargetType())
                .instant()
                .particle(ParticleTypes.DRIPPING_LAVA)
                .color(16762905).radius(getDistance(), true)
                .spawn();

        PacketHandler.sendToTrackingMaybeSelf(
                new ParticleEffectSpawnPacket(
                        ParticleTypes.FLAME,
                        ParticleEffects.CIRCLE_MOTION, 50, 0,
                        entity.getPosX(), entity.getPosY() + 1.0,
                        entity.getPosZ(), 1.0, 1.0, 1.0, .1f,
                        entity.getLookVec()), entity);
    }

    @Override
    public void execute(LivingEntity entity, IMKEntityData entityData) {
        entityData.startAbility(this);
    }
}

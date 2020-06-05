package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.SingleTargetCastState;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClericHeal extends MKAbility {
    public static final ClericHeal INSTANCE = new ClericHeal();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static float BASE_VALUE = 5.0f;
    public static float VALUE_SCALE = 5.0f;

    private ClericHeal() {
        super(MKCore.makeRL("ability.heal"));
    }

    @Override
    public int getCooldown() {
        return 5;
    }

    @Override
    public CastState createCastState(int castTime) {
        return new SingleTargetCastState(castTime);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return ModSounds.casting_holy;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return ModSounds.spell_holy_5;
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, CastState castState) {
        super.endCast(entity, data, castState);

        SingleTargetCastState singleTargetState = (SingleTargetCastState) castState;

        singleTargetState.getTarget().ifPresent(target -> {
            int level = 1;
            SpellCast heal = ClericHealPotion.Create(entity, BASE_VALUE, VALUE_SCALE).setTarget(target);
            target.addPotionEffect(heal.toPotionEffect(level));
            SoundUtils.playSoundAtEntity(target, ModSounds.spell_heal_3, SoundCategory.PLAYERS);
            PacketHandler.sendToTrackingMaybeSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.HAPPY_VILLAGER,
                            ParticleEffects.SPHERE_MOTION, 50, 10,
                            target.getPosX(), target.getPosY() + 1.0f,
                            target.getPosZ(), 1.0, 1.0, 1.0, 1.5,
                            entity.getLookVec()), target);
        });
    }

    @Override
    public boolean canSelfCast() {
        return true;
    }

    @Override
    public float getManaCost() {
        return 4;
    }

    @Override
    public float getDistance() {
        return 10.0f + 5.0f;
    }

    @Override
    protected boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return ClericHealPotion.INSTANCE.isValidTarget(getTargetContext(), caster, target);
    }

    @Override
    public int getCastTime() {
        return GameConstants.TICKS_PER_SECOND / 4;
    }

    @Override
    public void execute(LivingEntity entity, IMKEntityData pData) {
        LivingEntity targetEntity = getSingleLivingTargetOrSelf(entity, getDistance(), true);
        CastState state = pData.startAbility(this);
        SingleTargetCastState singleTargetState = (SingleTargetCastState) state;
        if (singleTargetState != null) {
            singleTargetState.setTarget(targetEntity);
        }
    }
}

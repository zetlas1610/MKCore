package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.abilities.ai.NeedsBuffCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkinLikeWoodAbility extends MKToggleAbility {
    public static final SkinLikeWoodAbility INSTANCE = new SkinLikeWoodAbility();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static int BASE_DURATION = 32767;
    public static int DURATION_SCALE = 0;

    private SkinLikeWoodAbility() {

        super(MKCore.makeRL("ability.skin_like_wood"));
        setUseCondition(new NeedsBuffCondition(this, SkinLikeWoodPotion.INSTANCE).setSelfOnly(true));
    }


    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public float getDistance() {
        return 1.0f;
    }

    @Override
    public Effect getToggleEffect() {
        return SkinLikeWoodPotion.INSTANCE;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void applyEffect(LivingEntity entity, IMKEntityData entityData) {
        super.applyEffect(entity, entityData);
        int level = 1;
        SoundUtils.playSoundAtEntity(entity, ModSounds.spell_earth_7, SoundCategory.PLAYERS);
        // What to do for each target hit
        entity.addPotionEffect(SkinLikeWoodPotion.Create(entity).setTarget(entity).toPotionEffect(BASE_DURATION, level));

        PacketHandler.sendToTrackingMaybeSelf(
                new ParticleEffectSpawnPacket(
                        ParticleTypes.ITEM_SLIME,
                        ParticleEffects.CIRCLE_MOTION, 30, 0,
                        entity.getPosX(), entity.getPosY() + .5,
                        entity.getPosZ(), 1.0, 1.0, 1.0, 1.0f,
                        entity.getLookVec()), entity);
    }
}

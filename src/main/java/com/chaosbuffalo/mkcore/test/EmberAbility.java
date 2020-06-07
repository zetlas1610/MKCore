package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.SingleTargetCastState;
import com.chaosbuffalo.mkcore.abilities.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.abilities.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModDamageTypes;
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
public class EmberAbility extends MKAbility {
    public static final EmberAbility INSTANCE = new EmberAbility();
    protected final FloatAttribute damage = new FloatAttribute("damage", 6.0f);
    protected final IntAttribute burnTime = new IntAttribute("burnTime", 5);

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    private EmberAbility() {
        super(MKCore.makeRL("ability.ember"));
        addAttributes(damage, burnTime);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance() {
        return 25.0f;
    }

    @Override
    public CastState createCastState(int castTime) {
        return new SingleTargetCastState(castTime);
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return ModSounds.spell_cast_7;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return ModSounds.casting_fire;
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, CastState state) {
        super.endCast(entity, data, state);
        MKCore.LOGGER.info("In end cast ember");
        SingleTargetCastState singleTargetState = (SingleTargetCastState) state;
        if (singleTargetState == null) {
            return;
        }
        MKCore.LOGGER.info("has state");
        singleTargetState.getTarget().ifPresent(targetEntity -> {
            int burnDuration = burnTime.getValue();
            float amount = damage.getValue();
            MKCore.LOGGER.info("Ember damage {} burnTime {}", amount, burnDuration);
            targetEntity.setFire(burnDuration);
            targetEntity.attackEntityFrom(MKDamageSource.causeAbilityDamage(ModDamageTypes.FireDamage,
                    getAbilityId(), entity, entity), damage.getValue());
            SoundUtils.playSoundAtEntity(targetEntity, ModSounds.spell_fire_6, SoundCategory.PLAYERS);
            PacketHandler.sendToTrackingMaybeSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.FLAME,
                            ParticleEffects.CIRCLE_PILLAR_MOTION, 60, 10,
                            targetEntity.getPosX(), targetEntity.getPosY() + 1.0,
                            targetEntity.getPosZ(), 1.0, 1.0, 1.0, .25,
                            entity.getLookVec()), targetEntity);
        });
    }

    @Override
    public void execute(LivingEntity entity, IMKEntityData pData) {
        LivingEntity targetEntity = getSingleLivingTarget(entity, getDistance());
        MKCore.LOGGER.info("In cast ember");
        if (targetEntity != null) {
            MKCore.LOGGER.info("Found target: {}", targetEntity);
            CastState state = pData.startAbility(this);
            SingleTargetCastState singleTargetState = (SingleTargetCastState) state;
            if (singleTargetState != null) {
                singleTargetState.setTarget(targetEntity);
            }
        }
    }
}

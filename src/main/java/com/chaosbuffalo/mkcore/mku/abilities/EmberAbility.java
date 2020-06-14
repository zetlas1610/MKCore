package com.chaosbuffalo.mkcore.mku.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
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
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.Set;


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
    public void continueCastClient(LivingEntity entity, IMKEntityData data, int castTimeLeft) {
        super.continueCastClient(entity, data, castTimeLeft);
        Random rand = entity.getRNG();
        entity.getEntityWorld().addParticle(ParticleTypes.LAVA,
                entity.getPosX(), entity.getPosY() + 0.5F, entity.getPosZ(),
                rand.nextFloat() / 2.0F, 5.0E-5D, rand.nextFloat() / 2.0F);
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        MKCore.LOGGER.info("EmberAbility.endCast {}", entity);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKCore.LOGGER.info("with target {}", targetEntity);
            int burnDuration = burnTime.getValue();
            float amount = damage.getValue();
            MKCore.LOGGER.info("Ember damage {} burnTime {}", amount, burnDuration);
            targetEntity.setFire(burnDuration);
            targetEntity.attackEntityFrom(MKDamageSource.causeAbilityDamage(ModDamageTypes.FireDamage,
                    getAbilityId(), entity, entity), damage.getValue());
            SoundUtils.playSoundAtEntity(targetEntity, ModSounds.spell_fire_6);
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
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET);
    }

    @Override
    public AbilityContext createAbilityContext(IMKEntityData pData) {
        LivingEntity targetEntity = getSingleLivingTarget(pData.getEntity(), getDistance());
        MKCore.LOGGER.info("EmberAbility.createAbilityContext {} {}", pData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(targetEntity);
    }

    @Override
    public void executeWithContext(IMKEntityData entityData, AbilityContext context) {
        MKCore.LOGGER.info("EmberAbility.executeWithContext {}", entityData.getEntity());
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(target -> {
            MKCore.LOGGER.info("with target {}", target);
            entityData.startAbility(context, this);
        });
    }
}

package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.SingleTargetCastState;
import com.chaosbuffalo.mkcore.abilities.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.abilities.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModDamageTypes;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;


@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EmberAbility extends PlayerAbility {
    public static final EmberAbility INSTANCE = new EmberAbility();
    protected final FloatAttribute damage = new FloatAttribute("damage", 6.0f);
    protected final IntAttribute burnTime = new IntAttribute("burnTime", 5);

    @SubscribeEvent
    public static void register(RegistryEvent.Register<PlayerAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    private EmberAbility() {
        super(MKCore.makeRL("ability.ember"));
        addAttributes(damage, burnTime);
    }

    @Override
    public Targeting.TargetType getTargetType() {
        return Targeting.TargetType.ALL;
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
    public void endCast(PlayerEntity entity, IMKPlayerData data, World theWorld, CastState state) {
        super.endCast(entity, data, theWorld, state);
        SingleTargetCastState singleTargetState = (SingleTargetCastState) state;
        if (singleTargetState == null) {
            return;
        }

        singleTargetState.getTarget().ifPresent(targetEntity -> {
            int burnDuration = burnTime.getValue();
            float amount = damage.getValue();
            MKCore.LOGGER.info("Ember damage {} burnTime {}", amount, burnDuration);
            targetEntity.setFire(burnDuration);
            targetEntity.attackEntityFrom(MKDamageSource.causeAbilityDamage(ModDamageTypes.FireDamage,
                    getAbilityId(), entity, entity), damage.getValue());
//            AbilityUtils.playSoundAtServerEntity(targetEntity, ModSounds.spell_fire_6, SoundCategory.PLAYERS);
            Vec3d lookVec = entity.getLookVec();
            PacketHandler.sendToTracking(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.FLAME,
                            ParticleEffects.CIRCLE_PILLAR_MOTION, 60, 10,
                            targetEntity.getPosX(), targetEntity.getPosY() + 1.0,
                            targetEntity.getPosZ(), 1.0, 1.0, 1.0, .25,
                            lookVec), targetEntity);
        });
    }

    @Override
    public void execute(PlayerEntity entity, IMKPlayerData pData, World theWorld) {
        LivingEntity targetEntity = getSingleLivingTarget(entity, getDistance());
        if (targetEntity != null) {
            CastState state = pData.startAbility(this);
            SingleTargetCastState singleTargetState = (SingleTargetCastState) state;
            if (singleTargetState != null) {
                singleTargetState.setTarget(targetEntity);
            }
        }
    }
}

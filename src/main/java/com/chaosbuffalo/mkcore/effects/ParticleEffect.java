package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleEffect extends SpellEffectBase {

    public static final ParticleEffect INSTANCE = new ParticleEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source, IParticleData particleId, int motionType, boolean includeSelf,
                                   Vec3d radius, Vec3d offsets, int particleCount, int particleData,
                                   double particleSpeed) {
        return new ParticleCast(source, particleId, motionType, radius, offsets, particleCount, particleData, particleSpeed, includeSelf);
    }

    protected ParticleEffect() {
        super(EffectType.NEUTRAL, 123);
        setRegistryName("effect.particle_potion");
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public boolean canSelfCast() {
        // Since this can be configured per-cast we return true here and then filter in doEffect where we have the
        // SpellCast object carrying the state
        return true;
    }

    @Override
    public void doEffect(Entity applier, Entity caster,
                         LivingEntity target, int amplifier, SpellCast cast) {
        if (!(cast instanceof ParticleCast)) {
            MKCore.LOGGER.error("Got to ParticlePotion.doEffect with a cast that wasn't a ParticleCast!");
            return;
        }
        ParticleCast particleCast = (ParticleCast) cast;
        // Check canSelfCast first
        if (!particleCast.includeSelf && target.equals(caster)) {
            return;
        }
        PacketHandler.sendToTrackingMaybeSelf(particleCast.createPacket(target), target);
    }

    public static class ParticleCast extends SpellCast {
        Entity source;
        IParticleData particleId;
        int motionType;
        Vec3d radius;
        Vec3d offsets;
        int particleCount;
        int particleData;
        double particleSpeed;
        boolean includeSelf;

        public ParticleCast(Entity source, IParticleData particleId, int motionType,
                            Vec3d radius, Vec3d offsets, int particleCount, int particleData,
                            double particleSpeed, boolean includeSelf) {
            super(ParticleEffect.INSTANCE, source);
            this.source = source;
            this.particleId = particleId;
            this.motionType = motionType;
            this.radius = radius;
            this.offsets = offsets;
            this.particleCount = particleCount;
            this.particleData = particleData;
            this.particleSpeed = particleSpeed;
            this.includeSelf = includeSelf;
        }

        public ParticleEffectSpawnPacket createPacket(Entity target) {
            return new ParticleEffectSpawnPacket(particleId,
                    motionType, particleCount, particleData,
                    target.getPosX() + offsets.x,
                    target.getPosY() + offsets.y,
                    target.getPosZ() + offsets.z,
                    radius.x,
                    radius.y,
                    radius.z,
                    particleSpeed,
                    source.getPositionVector().subtract(target.getPositionVector()).normalize());
        }
    }
}
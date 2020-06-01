package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellManager;
import com.chaosbuffalo.mkcore.effects.SpellPotionBase;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MKAreaEffectEntity extends AreaEffectCloudEntity {
    @ObjectHolder(MKCore.MOD_ID + ":mk_area_effect")
    public static EntityType<MKAreaEffectEntity> TYPE;

    private static class EffectEntry {
        final EffectInstance effect;
        final TargetingContext targetContext;
        SpellCast cast;

        EffectEntry(EffectInstance effect, TargetingContext targetContext) {
            this.effect = effect;
            this.targetContext = targetContext;
        }

        EffectEntry(SpellCast cast, EffectInstance effect, TargetingContext targetContext) {
            this(effect, targetContext);
            this.cast = cast;
        }
    }

    private final List<EffectEntry> effects;

    private static final float DEFAULT_RADIUS = 3.0f;
    private static final float DEFAULT_HEIGHT = 1.0f;


    public MKAreaEffectEntity(EntityType<? extends AreaEffectCloudEntity> entityType, World world) {
        super(entityType, world);
        effects = new ArrayList<>();
        setRadius(DEFAULT_RADIUS);
    }

    public MKAreaEffectEntity(World worldIn, double x, double y, double z) {
        this(TYPE, worldIn);
        this.setPosition(x, y, z);
        this.duration = 600;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
    }

    @Nonnull
    @Override
    public EntitySize getSize(@Nonnull Pose poseIn) {
        return EntitySize.flexible(this.getRadius() * 2.0F, DEFAULT_HEIGHT);
    }

    public void setPeriod(int delay) {
        this.reapplicationDelay = delay;
    }

    public void disableParticle() {
        // TODO
    }

    private boolean isInWaitPhase() {
        return shouldIgnoreRadius();
    }

    private void setInWaitPhase(boolean waitPhase) {
        setIgnoreRadius(waitPhase);
    }


    private void entityTick() {
        // We don't want to call AreaEffectCloudEntity.tick because it'll do all the logic. This is what Entity.tick() does
        if (!this.world.isRemote) {
            this.setFlag(6, this.isGlowing());
        }

        this.baseTick();
    }

    @Override
    public void tick() {
        entityTick();

        if (this.world.isRemote) {
            clientUpdate();
        } else {
            if (serverUpdate()) {
                remove();
            }
        }
    }

    public void addSpellCast(SpellCast cast, EffectInstance effect, TargetingContext targetContext) {
        this.effects.add(new EffectEntry(cast, effect, targetContext));
    }

    public void addEffect(EffectInstance effect, TargetingContext targetContext) {
        this.effects.add(new EffectEntry(effect, targetContext));
    }

    private boolean entityCheck(LivingEntity e) {
        return e != null &&
                EntityPredicates.NOT_SPECTATING.test(e) &&
                EntityPredicates.IS_LIVING_ALIVE.test(e) &&
                !reapplicationDelayMap.containsKey(e) &&
                e.canBeHitWithPotion();
    }

    private boolean checkTickModifiers() {
        // TODO: see if there's anything we might want to do every tick (change shape, etc)
        return false;
    }

    private boolean checkUseModifiers(LivingEntity target) {
        // TODO: see if there's anything we might want to do every time we hit a target (change shape, etc)
        return false;
    }

    private boolean serverUpdate() {
        if (ticksExisted >= waitTime + duration) {
            return true;
        }
        if (getOwner() == null) {
            return true;
        }

        boolean stillWaiting = ticksExisted < waitTime;

        if (isInWaitPhase() != stillWaiting) {
            setInWaitPhase(stillWaiting);
        }

        if (stillWaiting) {
            return false;
        }

        if (checkTickModifiers()) {
            return true;
        }

        // TODO: FUTURE: see if this can be made dynamic by inspecting the effects
        if (ticksExisted % 5 != 0) {
            return false;
        }

        this.reapplicationDelayMap.entrySet().removeIf(entry -> ticksExisted >= entry.getValue());

        if (effects.isEmpty()) {
            reapplicationDelayMap.clear();
            return false;
        }

        // Copy in case callbacks try to add more effects
        List<EffectEntry> targetEffects = new ArrayList<>(effects);
        List<LivingEntity> potentialTargets = this.world.getEntitiesWithinAABB(LivingEntity.class,
                this.getBoundingBox(), this::entityCheck);
        if (potentialTargets.isEmpty()) {
            return false;
        }

        float radius = this.getRadius();
        float maxRange = radius * radius;
        for (LivingEntity target : potentialTargets) {

            double d0 = target.getPosX() - getPosX();
            double d1 = target.getPosZ() - getPosZ();
            double entityDist = d0 * d0 + d1 * d1;

            if (entityDist > maxRange) {
                continue;
            }

            if (applyEffectsToTarget(targetEffects, target)) {
                // We applied at least 1 effect to this target, so check the use callbacks
                if (checkUseModifiers(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean applyEffectsToTarget(List<EffectEntry> potions, LivingEntity target) {
        reapplicationDelayMap.put(target, ticksExisted + reapplicationDelay);
        boolean wasHit = false;

        for (EffectEntry spellEffect : potions) {

            EffectInstance instance = spellEffect.effect;

            boolean validTarget;
            SpellPotionBase spBase = instance.getPotion() instanceof SpellPotionBase ? (SpellPotionBase) instance.getPotion() : null;
            if (spBase != null) {
                validTarget = spBase.isValidTarget(spellEffect.targetContext, getOwner(), target);
            } else {
                validTarget = Targeting.isValidTarget(spellEffect.targetContext, getOwner(), target);
            }

            if (!validTarget) {
                continue;
            }

            if (instance.getPotion().isInstant()) {

                if (spBase != null) {

                    SpellCast cast = spellEffect.cast;
                    if (cast == null) {
                        MKCore.LOGGER.warn("MKAreaEffect instant cast was null! Spell: {}", spellEffect.effect.getPotion().getName());
                        continue;
                    }

                    // We can skip affectEntity and go directly to the effect because we
                    // have already ensured the target is valid.
                    spBase.doEffect(this, getOwner(), target, instance.getAmplifier(), cast);
                } else {
                    instance.getPotion().affectEntity(this, this.getOwner(), target, instance.getAmplifier(), 0.5D);
                }
            } else {
                if (spBase != null) {
                    SpellCast cast = spellEffect.cast;
                    if (cast == null) {
                        MKCore.LOGGER.warn("MKAreaEffect periodic cast was null! Spell: {}", spellEffect.effect.getPotion().getName());
                        continue;
                    }

                    // The cast given to MKAreaEffect has no target, so we need to register
                    SpellManager.registerTarget(cast, target);
                }

                target.addPotionEffect(new EffectInstance(instance));
            }
            wasHit = true;
        }

        return wasHit;
    }

    private void clientUpdate() {
        if (ticksExisted % 5 != 0) {
            return;
        }
        IParticleData particle = this.getParticleData();

        if (isInWaitPhase()) {
            if (!rand.nextBoolean()) {
                return;
            }

            for (int i = 0; i < 2; i++) {
                float f1 = rand.nextFloat() * ((float) Math.PI * 2F);
                float f2 = MathHelper.sqrt(rand.nextFloat()) * 0.2F;
                float xOff = MathHelper.cos(f1) * f2;
                float zOff = MathHelper.sin(f1) * f2;

                if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
                    int color = rand.nextBoolean() ? 16777215 : getColor();
                    int r = color >> 16 & 255;
                    int g = color >> 8 & 255;
                    int b = color & 255;
                    world.addOptionalParticle(particle, getPosX() + xOff, getPosY(), getPosZ() + zOff, r / 255f, g / 255f, b / 255f);
                } else {
                    world.addOptionalParticle(particle, getPosX() + xOff, getPosY(), getPosZ() + zOff, 0, 0, 0);
                }
            }
        } else {
            float radius = getRadius();
            int particleCount = (int) radius * 10;

            for (int i = 0; i < particleCount; i++) {
                float f6 = rand.nextFloat() * ((float) Math.PI * 2F);
                float f7 = MathHelper.sqrt(rand.nextFloat()) * radius;
                float xOffset = MathHelper.cos(f6) * f7;
                float zOffset = MathHelper.sin(f6) * f7;

                if (particle == ParticleTypes.ENTITY_EFFECT) {
                    int color = getColor();
                    int r = color >> 16 & 255;
                    int g = color >> 8 & 255;
                    int b = color & 255;
                    world.addOptionalParticle(particle, getPosX() + xOffset, getPosY(), getPosZ() + zOffset, r / 255f, g / 255f, b / 255f);
                } else if (particle == ParticleTypes.NOTE) {
                    world.addOptionalParticle(particle, getPosX() + xOffset, getPosY(), getPosZ() + zOffset, rand.nextInt(24) / 24.0f, 0.009999999776482582D, (0.5D - rand.nextDouble()) * 0.15D);
                } else {
                    world.addOptionalParticle(particle, getPosX() + xOffset, getPosY(), getPosZ() + zOffset, (0.5D - rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - rand.nextDouble()) * 0.15D);
                }
            }
        }
    }

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    public boolean writeUnlessPassenger(CompoundNBT compound) {
        return false;
    }

}

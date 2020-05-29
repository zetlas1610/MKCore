package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellManager;
import com.chaosbuffalo.mkcore.effects.SpellPotionBase;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.material.PushReaction;
import net.minecraft.command.arguments.ParticleArgument;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

// This class exists mostly because EntityAreaEffectCloud has all its members marked as private and many have no getters
public class EntityMKAreaEffect extends Entity {
    @ObjectHolder(MKCore.MOD_ID + ":mk_area_effect")
    public static EntityType<EntityMKAreaEffect> TYPE;

    private static final DataParameter<Float> RADIUS = EntityDataManager.createKey(EntityMKAreaEffect.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> COLOR = EntityDataManager.createKey(EntityMKAreaEffect.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> IN_WAIT_PHASE = EntityDataManager.createKey(EntityMKAreaEffect.class, DataSerializers.BOOLEAN);
    private static final DataParameter<IParticleData> PARTICLE = EntityDataManager.createKey(EntityMKAreaEffect.class, DataSerializers.PARTICLE_DATA);
    private final List<EffectEntry> effects;
    private final Map<Entity, Integer> reapplicationDelayMap;
    private int duration;
    private int waitTime;
    private int reapplicationDelay;
    private boolean colorSet;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    private LivingEntity owner;
    private UUID ownerUniqueId;

    private static class EffectEntry {
        final EffectInstance effect;
        final Targeting.TargetType targetType;
        final boolean excludeCaster;
        SpellCast cast;

        EffectEntry(EffectInstance effect, Targeting.TargetType targetType, boolean excludeCaster) {
            this.effect = effect;
            this.targetType = targetType;
            this.excludeCaster = excludeCaster;
        }

        EffectEntry(SpellCast cast, EffectInstance effect, Targeting.TargetType targetType, boolean excludeCaster) {
            this(effect, targetType, excludeCaster);
            this.cast = cast;
        }
    }

    private static final float DEFAULT_RADIUS = 3.0f;
    private static final float DEFAULT_HEIGHT = 1.0f;

    public EntityMKAreaEffect(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.effects = new ArrayList<>();
        this.reapplicationDelayMap = new HashMap<>();
        this.duration = 600;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
        this.noClip = true;
        this.setRadius(DEFAULT_RADIUS);
    }

    public EntityMKAreaEffect(World worldIn, double x, double y, double z) {
        this(TYPE, worldIn);
        this.setPosition(x, y, z);
    }

    @Override
    protected void registerData() {
        this.getDataManager().register(COLOR, 0);
        this.getDataManager().register(RADIUS, 0.5F);
        this.getDataManager().register(IN_WAIT_PHASE, false);
        this.getDataManager().register(PARTICLE, ParticleTypes.ENTITY_EFFECT);
    }

    public float getRadius() {
        return this.getDataManager().get(RADIUS);
    }

    @Nonnull
    @Override
    public EntitySize getSize(@Nonnull Pose poseIn) {
        return EntitySize.flexible(this.getRadius() * 2.0F, DEFAULT_HEIGHT);
    }

    public void setRadius(float radiusIn) {
        if (!this.world.isRemote) {
            this.getDataManager().set(RADIUS, radiusIn);
        }
    }

    public void addSpellCast(SpellCast cast, EffectInstance effect, Targeting.TargetType targetType, boolean excludeCaster) {
        this.effects.add(new EffectEntry(cast, effect, targetType, excludeCaster));
    }

    public void addEffect(EffectInstance effect, Targeting.TargetType targetType, boolean excludeCaster) {
        this.effects.add(new EffectEntry(effect, targetType, excludeCaster));
    }

    public int getColor() {
        return this.getDataManager().get(COLOR);
    }

    public void setColor(int colorIn) {
        this.colorSet = true;
        this.getDataManager().set(COLOR, colorIn);
    }

    public IParticleData getParticle() {
        return this.getDataManager().get(PARTICLE);
    }

    public void setParticle(IParticleData particleIn) {
        this.getDataManager().set(PARTICLE, particleIn);
    }

    public void disableParticle() {
//        TODO: fix for 1.15
//        getDataManager().set(PARTICLE, -1);
    }

    protected void setInWaitPhase(boolean waiting) {
        this.getDataManager().set(IN_WAIT_PHASE, waiting);
    }

    public boolean isInWaitPhase() {
        return this.getDataManager().get(IN_WAIT_PHASE);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int durationIn) {
        this.duration = durationIn;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isRemote) {
            clientUpdate();
        } else {
            serverUpdate();
        }
    }

    private void serverUpdate() {

        if (this.ticksExisted >= this.waitTime + this.duration) {
            this.remove();
            return;
        }
        if (getOwner() == null) {
            this.remove();
            return;
        }

        boolean stillWaiting = this.ticksExisted < this.waitTime;

        if (isInWaitPhase() != stillWaiting) {
            this.setInWaitPhase(stillWaiting);
        }

        if (stillWaiting) {
            return;
        }

        float radius = this.getRadius();
        if (this.radiusPerTick != 0.0F) {
            radius += this.radiusPerTick;

            if (radius < 0.5F) {
                this.remove();
                return;
            }

            this.setRadius(radius);
        }

        // TODO: FUTURE: see if this can be made dynamic by inspecting the effects
        if (this.ticksExisted % 5 != 0) {
            return;
        }

        this.reapplicationDelayMap.entrySet().removeIf(entry -> this.ticksExisted >= entry.getValue());

        if (this.effects.isEmpty()) {
            this.reapplicationDelayMap.clear();
            return;
        }

        List<EffectEntry> potions = new ArrayList<>(this.effects);
        List<LivingEntity> potentialTargets = this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox(),
                e -> e != null &&
                        EntityPredicates.NOT_SPECTATING.test(e) &&
                        EntityPredicates.IS_LIVING_ALIVE.test(e) &&
                        !reapplicationDelayMap.containsKey(e) &&
                        e.canBeHitWithPotion());
        if (potentialTargets.isEmpty()) {
            return;
        }

        for (LivingEntity target : potentialTargets) {

            double d0 = target.getPosX() - this.getPosX();
            double d1 = target.getPosZ() - this.getPosZ();
            double d2 = d0 * d0 + d1 * d1;

            if (d2 > (double) (radius * radius)) {
                continue;
            }

            applyEffectsToTarget(potions, target);

            if (this.radiusOnUse != 0.0F) {
                radius += this.radiusOnUse;

                if (radius < 0.5F) {
                    this.remove();
                    return;
                }

                this.setRadius(radius);
            }

            if (this.durationOnUse != 0) {
                this.duration += this.durationOnUse;

                if (this.duration <= 0) {
                    this.remove();
                    return;
                }
            }
        }
    }

    private void applyEffectsToTarget(List<EffectEntry> potions, LivingEntity target) {
        this.reapplicationDelayMap.put(target, this.ticksExisted + this.reapplicationDelay);

        for (EffectEntry spellEffect : potions) {

            EffectInstance instance = spellEffect.effect;

            boolean validTarget;
            SpellPotionBase spBase = instance.getPotion() instanceof SpellPotionBase ? (SpellPotionBase) instance.getPotion() : null;
            if (spBase != null) {
                validTarget = spBase.isValidTarget(spellEffect.targetType, getOwner(), target, spellEffect.excludeCaster);
            } else {
                validTarget = Targeting.isValidTarget(spellEffect.targetType, getOwner(), target, spellEffect.excludeCaster);
            }

            if (!validTarget) {
                continue;
            }

            if (instance.getPotion().isInstant()) {

                if (spBase != null) {

                    SpellCast cast = spellEffect.cast;
                    if (cast == null) {
                        MKCore.LOGGER.warn("MKAREA instant null cast! Spell: {}", spellEffect.effect.getPotion().getName());
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
                        MKCore.LOGGER.warn("MKAREA periodic null cast! Spell: {}", spellEffect.effect.getPotion().getName());
                        continue;
                    }

                    // The cast given to MKAreaEffect has no target, so we need to register
                    SpellManager.registerTarget(cast, target);
                }

                target.addPotionEffect(new EffectInstance(instance));
            }
        }
    }

    public void setRadiusOnUse(float radiusOnUseIn) {
        this.radiusOnUse = radiusOnUseIn;
    }

    public void setRadiusPerTick(float radiusPerTickIn) {
        this.radiusPerTick = radiusPerTickIn;
    }

    public void setWaitTime(int waitTimeIn) {
        this.waitTime = waitTimeIn;
    }

    public void setReapplicationDelay(int reapplicationDelay) {
        this.reapplicationDelay = reapplicationDelay;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUniqueId != null && this.world instanceof ServerWorld) {
            Entity entity = ((ServerWorld) this.world).getEntityByUuid(this.ownerUniqueId);

            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }

        return this.owner;
    }

    public void setOwner(LivingEntity ownerIn) {
        this.owner = ownerIn;
        this.ownerUniqueId = ownerIn == null ? null : ownerIn.getUniqueID();
    }

    @Override
    protected void readAdditional(@Nonnull CompoundNBT compound) {
        this.ticksExisted = compound.getInt("Age");
        this.duration = compound.getInt("Duration");
        this.waitTime = compound.getInt("WaitTime");
        this.reapplicationDelay = compound.getInt("ReapplicationDelay");
        this.durationOnUse = compound.getInt("DurationOnUse");
        this.radiusOnUse = compound.getFloat("RadiusOnUse");
        this.radiusPerTick = compound.getFloat("RadiusPerTick");
        this.setRadius(compound.getFloat("Radius"));
        this.ownerUniqueId = compound.getUniqueId("OwnerUUID");

        if (compound.contains("Particle", 8)) {
            try {
                this.setParticle(ParticleArgument.parseParticle(new StringReader(compound.getString("Particle"))));
            } catch (CommandSyntaxException commandsyntaxexception) {
                MKCore.LOGGER.warn("Couldn't load custom particle {}", compound.getString("Particle"), commandsyntaxexception);
            }
        }

        if (compound.contains("Color", 99)) {
            this.setColor(compound.getInt("Color"));
        }

        if (compound.contains("Effects", 9)) {
            ListNBT nbttaglist = compound.getList("Effects", 10);
            this.effects.clear();

            for (int i = 0; i < nbttaglist.size(); ++i) {
                CompoundNBT pe = nbttaglist.getCompound(i);
                EffectInstance effect = EffectInstance.read(pe);

                Targeting.TargetType tt = Targeting.TargetType.valueOf(pe.getString("TargetType"));
                boolean excludeCaster = pe.getBoolean("NoCaster");

                // This is needed because EffectInstance.read can definitely return null, but it's not marked @Nullable
                //noinspection ConstantConditions
                if (effect != null) {
                    this.addEffect(effect, tt, excludeCaster);
                }
            }
        }
    }

    @Override
    protected void writeAdditional(@Nonnull CompoundNBT compound) {
        compound.putInt("Age", this.ticksExisted);
        compound.putInt("Duration", this.duration);
        compound.putInt("WaitTime", this.waitTime);
        compound.putInt("ReapplicationDelay", this.reapplicationDelay);
        compound.putInt("DurationOnUse", this.durationOnUse);
        compound.putFloat("RadiusOnUse", this.radiusOnUse);
        compound.putFloat("RadiusPerTick", this.radiusPerTick);
        compound.putFloat("Radius", this.getRadius());
        compound.putString("Particle", this.getParticle().getParameters());

        if (this.ownerUniqueId != null) {
            compound.putUniqueId("OwnerUUID", this.ownerUniqueId);
        }

        if (this.colorSet) {
            compound.putInt("Color", this.getColor());
        }

        if (!this.effects.isEmpty()) {
            ListNBT list = new ListNBT();

            for (EffectEntry entry : this.effects) {
                CompoundNBT pe = entry.effect.write(new CompoundNBT());

                pe.putString("TargetType", entry.targetType.toString());
                pe.putBoolean("NoCaster", entry.excludeCaster);

                list.add(pe);
            }

            compound.put("Effects", list);
        }
    }

    @Override
    public void notifyDataManagerChange(@Nonnull DataParameter<?> key) {
        if (RADIUS.equals(key)) {
            this.recalculateSize();
        }

        super.notifyDataManagerChange(key);
    }

    @Override
    public void recalculateSize() {
        double d0 = this.getPosX();
        double d1 = this.getPosY();
        double d2 = this.getPosZ();
        super.recalculateSize();
        this.setPosition(d0, d1, d2);
    }

    @Nonnull
    @Override
    public PushReaction getPushReaction() {
        return PushReaction.IGNORE;
    }

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void clientUpdate() {
        if (ticksExisted % 5 != 0) {
            return;
        }
        IParticleData enumparticletypes = this.getParticle();
        if (enumparticletypes == null)
            return;

        if (isInWaitPhase()) {
            if (this.rand.nextBoolean()) {
                for (int i = 0; i < 2; ++i) {
                    float f1 = this.rand.nextFloat() * ((float) Math.PI * 2F);
                    float f2 = MathHelper.sqrt(this.rand.nextFloat()) * 0.2F;
                    float f3 = MathHelper.cos(f1) * f2;
                    float f4 = MathHelper.sin(f1) * f2;

                    if (enumparticletypes.getType() == ParticleTypes.ENTITY_EFFECT) {
                        int j = this.rand.nextBoolean() ? 16777215 : this.getColor();
                        int k = j >> 16 & 255;
                        int l = j >> 8 & 255;
                        int i1 = j & 255;
                        world.addOptionalParticle(enumparticletypes, getPosX() + f3, getPosY(), getPosZ() + f4, k / 255f, l / 255f, i1 / 255f);
                    } else {
                        world.addOptionalParticle(enumparticletypes, getPosX() + f3, getPosY(), getPosZ() + f4, 0, 0, 0);
                    }
                }
            }
        } else {
            float radius = getRadius();
            int particleCount = (int) radius * 10;

            for (int k1 = 0; k1 < particleCount; ++k1) {
                float f6 = this.rand.nextFloat() * ((float) Math.PI * 2F);
                float f7 = MathHelper.sqrt(this.rand.nextFloat()) * radius;
                float f8 = MathHelper.cos(f6) * f7;
                float f9 = MathHelper.sin(f6) * f7;

                if (enumparticletypes == ParticleTypes.ENTITY_EFFECT) {
                    int l1 = this.getColor();
                    int i2 = l1 >> 16 & 255;
                    int j2 = l1 >> 8 & 255;
                    int j1 = l1 & 255;
                    world.addOptionalParticle(enumparticletypes, getPosX() + f8, getPosY(), getPosZ() + f9, i2 / 255f, j2 / 255f, j1 / 255f);
                } else if (enumparticletypes == ParticleTypes.NOTE) {
                    world.addOptionalParticle(enumparticletypes, getPosX() + f8, getPosY(), getPosZ() + f9, this.rand.nextInt(24) / 24.0f, 0.009999999776482582D, (0.5D - this.rand.nextDouble()) * 0.15D);
                } else {
                    world.addOptionalParticle(enumparticletypes, getPosX() + f8, getPosY(), getPosZ() + f9, (0.5D - this.rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.rand.nextDouble()) * 0.15D);
                }
            }
        }
    }
}

package com.chaosbuffalo.mkcore.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class SpellCast {

    private final SpellPotionBase potion;
    private final CompoundNBT data;
    private WeakReference<Entity> applier;
    private WeakReference<Entity> caster;
    private UUID casterUUID;
    private UUID applierUUID;

    public SpellCast(SpellPotionBase potion, Entity caster) {
        this.potion = potion;
        this.applier = new WeakReference<>(caster);
        this.caster = new WeakReference<>(caster);
        this.data = new CompoundNBT();
        if (caster instanceof PlayerEntity) {
            this.casterUUID = caster.getUniqueID();
            this.applierUUID = caster.getUniqueID();
        }
    }

    public SpellPotionBase getPotion() {
        return potion;
    }

    void updateRefs(World world) {
        if (casterUUID != null) {
            caster = new WeakReference<>(world.getPlayerByUuid(casterUUID));
        }
        if (applierUUID != null) {
            applier = new WeakReference<>(world.getPlayerByUuid(applierUUID));
        }
    }

    public Entity getApplier() {
        return applier.get();
    }

    public Entity getCaster() {
        return caster.get();
    }

    public SpellCast setTarget(LivingEntity target) {
        SpellManager.registerTarget(this, target);
        return this;
    }

    public SpellCast setScalingParameters(float base, float scale) {
        setFloat("damageBase", base);
        setFloat("damageScale", scale);
        return this;
    }

    public SpellCast setFloat(String name, float value) {
        data.putFloat(name, value);
        return this;
    }

    public float getFloat(String name) {
        return data.getFloat(name);
    }

    public float getFloat(String name, float defaultValue) {
        if (data.contains(name)) {
            return data.getFloat(name);
        }
        return defaultValue;
    }

    public SpellCast setDouble(String name, double value) {
        data.putDouble(name, value);
        return this;
    }

    public double getDouble(String name) {
        return data.getDouble(name);
    }

    public SpellCast setInt(String name, int value) {
        data.putInt(name, value);
        return this;
    }

    public int getInt(String name) {
        return data.getInt(name);
    }

    public SpellCast setBoolean(String name, boolean value) {
        data.putBoolean(name, value);
        return this;
    }

    public SpellCast setResourceLocation(String name, ResourceLocation loc) {
        data.putString(name, loc.toString());
        return this;
    }

    @Nullable
    public ResourceLocation getResourceLocation(String name) {
        return new ResourceLocation(data.getString(name));
    }

    public boolean getBoolean(String name) {
        return data.getBoolean(name);
    }

    public float getBaseValue() {
        return getFloat("damageBase");
    }

    public float getScaleValue() {
        return getFloat("damageScale");
    }

    public float getScaledValue(int amplifier) {
        return getBaseValue() + (getScaleValue() * amplifier);
    }

    public EffectInstance toPotionEffect(int amplifier) {
        return new EffectInstance(potion, 1, amplifier, potion.isAmbient(), potion.shouldShowParticles());
    }

    public EffectInstance toPotionEffect(int duration, int amplifier) {
        return new EffectInstance(potion, duration, amplifier, potion.isAmbient(), potion.shouldShowParticles());
    }

    @Override
    public String toString() {
        return String.format("Cast[%s, %s]", potion.getName(), caster);
    }
}

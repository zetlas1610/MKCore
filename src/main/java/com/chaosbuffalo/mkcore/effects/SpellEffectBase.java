package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class SpellEffectBase extends Effect {

    public void register(String name) {
        setRegistryName(name);
    }

    protected SpellEffectBase(EffectType typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);
    }

    public abstract TargetingContext getTargetContext();

    public boolean canSelfCast() {
        return false;
    }

    protected boolean isServerSideOnly() {
        return true;
    }

    public boolean isValidTarget(TargetingContext targetContext, Entity caster, LivingEntity target) {
        return !(caster == null || target == null) && Targeting.isValidTarget(targetContext, caster, target);
    }

    @Override
    public boolean isReady(int duration, int amplitude) {
        // Controls whether performEffect is called
        //System.out.printf("SpellPotionBase - isReady %d %d\n", duration, amplitude);
        return duration >= 1;
    }

    public boolean canPersistAcrossSessions() {
        return true;
    }

    public SpellCast createReapplicationCast(LivingEntity target) {
        if (canPersistAcrossSessions())
            return null;

        EffectInstance current = target.getActivePotionEffect(this);
        if (current == null)
            return null;

        return newSpellCast(target).setTarget(target);
    }

    @Override
    public boolean isInstant() {
        return true;
    }

    // Called when the potion is being applied by an
    // AreaEffect or thrown potion bottle
    @Override
    public void affectEntity(Entity applier, Entity caster, @Nonnull LivingEntity target, int amplifier, double health) {
        if (target.world.isRemote && isServerSideOnly())
            return;
        MKCore.LOGGER.debug("affectEntity {} {}", target, getName());

        SpellCast cast = SpellManager.getCast(target, this).orElseGet(() -> createReapplicationCast(target));

        // Second chance
        if (cast == null) {
//            Log.warn("affectEntity cast was null after recast! Spell: %s", getName());
            return;
        }

        if (!isValidTarget(getTargetContext(), caster, target))
            return;

        doEffect(applier, caster, target, amplifier, cast);
    }

    // Called for effects that are applied directly to an entity
    @Override
    public void performEffect(@Nonnull LivingEntity target, int amplifier) {
        if (target.world.isRemote && isServerSideOnly())
            return;
        MKCore.LOGGER.debug("performEffect {} {}", target, getName());

        SpellCast cast = SpellManager.getCast(target, this).orElseGet(() -> createReapplicationCast(target));

        // Second chance
        if (cast == null) {
//            Log.warn("performEffect cast was null after recast! Spell: %s", getName());
            return;
        }

        if (!isValidTarget(getTargetContext(), cast.getCaster(), target))
            return;

        doEffect(cast.getApplier(), cast.getCaster(), target, amplifier, cast);
    }

    @Override
    public void applyAttributesModifiersToEntity(@Nonnull LivingEntity target, @Nonnull AbstractAttributeMap attributes, int amplifier) {
        MKCore.LOGGER.debug("applyAttributesModifiersToEntity {} {}", target, getName());
        super.applyAttributesModifiersToEntity(target, attributes, amplifier);

        if (!target.world.isRemote || !isServerSideOnly()) {
            SpellCast cast = SpellManager.getCast(target, this).orElseGet(() -> createReapplicationCast(target));

            if (cast != null) {
                onPotionAdd(cast, target, attributes, amplifier);
            }
        }
    }


    @Override
    public void removeAttributesModifiersFromEntity(@Nonnull LivingEntity target, @Nonnull AbstractAttributeMap attributes, int amplifier) {
        MKCore.LOGGER.debug("removeAttributesModifiersFromEntity {} {}", target, getName());
        super.removeAttributesModifiersFromEntity(target, attributes, amplifier);

        if (!target.world.isRemote || !isServerSideOnly()) {
            SpellCast cast = SpellManager.getCast(target, this).orElseGet(() -> createReapplicationCast(target));

            if (cast != null) {
                onPotionRemove(cast, target, attributes, amplifier);
            }
        }
    }

    public void onPotionAdd(SpellCast cast, LivingEntity target, AbstractAttributeMap attributes, int amplifier) {
    }

    public void onPotionRemove(SpellCast cast, LivingEntity target, AbstractAttributeMap attributes, int amplifier) {
    }

    public abstract void doEffect(Entity applier, Entity caster, LivingEntity target, int amplifier, SpellCast cast);

    protected boolean shouldShowParticles() {
        return true;
    }

    protected boolean isAmbient() {
        return false;
    }

    public SpellCast newSpellCast(Entity caster) {
        return new SpellCast(this, caster);
    }

}

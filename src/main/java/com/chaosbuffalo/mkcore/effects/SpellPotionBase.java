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

public abstract class SpellPotionBase extends Effect {

    public void register(String name) {
        setRegistryName(name);
    }

    protected SpellPotionBase(EffectType typeIn, int liquidColorIn) {
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

    public EffectInstance toPotionEffect(int amplifier) {
        return toPotionEffect(1, amplifier);
    }

    public EffectInstance toPotionEffect(int duration, int amplifier) {
        return new EffectInstance(this, duration, amplifier, isAmbient(), shouldShowParticles());
    }

    public SpellCast newSpellCast(Entity caster) {
        return new SpellCast(this, caster);
    }

    public ResourceLocation getIconTexture() {
        return null;
    }

    @Override
    public boolean shouldRenderInvText(EffectInstance p_shouldRenderInvText_1_) {
        return false;
    }

    @Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, int x, int y, float partialTicks) {
        if (gui != null && getIconTexture() != null) {
            Minecraft.getInstance().getTextureManager().bindTexture(getIconTexture());
            AbstractGui.blit(x + 6, y + 7, 0, 0, 16, 16, 16, 16);
//            GuiUtils.drawTexturedModalRect(x + 6, y + 7, 0, 0, 16, 16, 16, 16);

            String s1 = I18n.format(this.getName());
            if (effect.getAmplifier() == 2) {
                s1 = s1 + " " + I18n.format("enchantment.level.2");
            } else if (effect.getAmplifier() == 3) {
                s1 = s1 + " " + I18n.format("enchantment.level.3");
            } else if (effect.getAmplifier() == 4) {
                s1 = s1 + " " + I18n.format("enchantment.level.4");
            }
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(s1, x + 10 + 18, y + 6, 16777215);
            String s = EffectUtils.getPotionDurationString(effect, 1.0F);
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(s, x + 10 + 18, y + 6 + 10, 8355711);
        }
    }
}

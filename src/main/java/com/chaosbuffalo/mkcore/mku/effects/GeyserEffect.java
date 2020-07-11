package com.chaosbuffalo.mkcore.mku.effects;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellEffectBase;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeyserEffect extends SpellEffectBase {

    public static final GeyserEffect INSTANCE = new GeyserEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source, float base, float scale) {
        return INSTANCE.newSpellCast(source).setScalingParameters(base, scale);
    }


    protected GeyserEffect() {
        super(EffectType.NEUTRAL, 123);
        setRegistryName(MKCore.makeRL("effect.geyser"));
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public void doEffect(Entity applier, Entity caster, LivingEntity target, int amplifier, SpellCast cast) {
        int baseDuration = 2 * GameConstants.TICKS_PER_SECOND * amplifier;
        if (Targeting.isValidTarget(TargetingContexts.FRIENDLY, caster, target)) {
            target.addPotionEffect(new EffectInstance(Effects.LEVITATION, baseDuration, amplifier, false, true));
            target.addPotionEffect(FeatherFallEffect.Create(caster).setTarget(target).toPotionEffect(baseDuration + 40, amplifier));
            target.addPotionEffect(new EffectInstance(Effects.REGENERATION,
                    (5 + 5 * amplifier) * GameConstants.TICKS_PER_SECOND, amplifier - 1));
        } else {
            target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(applier, caster), cast.getScaledValue(amplifier));
            target.addPotionEffect(new EffectInstance(Effects.WEAKNESS, baseDuration * 2, amplifier, false, true));
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, baseDuration, amplifier, false, true));
        }

        target.addVelocity(0.0, amplifier * 1.5f, 0.0);
        target.velocityChanged = true;
    }
}

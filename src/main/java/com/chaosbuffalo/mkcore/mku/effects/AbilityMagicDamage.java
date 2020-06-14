package com.chaosbuffalo.mkcore.mku.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellEffectBase;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AbilityMagicDamage extends SpellEffectBase {
    public static final String SCALING_CONTRIBUTION = "instant_indirect_magic_damage.scaling_contribution";

    public static ResourceLocation INDIRECT_MAGIC_DMG_ABILITY_ID = MKCore.makeRL("ability.instant_indirect_magic_damage");

    public static final AbilityMagicDamage INSTANCE = new AbilityMagicDamage();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source, float baseDamage, float scaling) {
        return INSTANCE.newSpellCast(source).setScalingParameters(baseDamage, scaling);
    }

    public static SpellCast Create(Entity source, float baseDamage, float scaling, float modifierScaling) {
        return INSTANCE.newSpellCast(source).setScalingParameters(baseDamage, scaling)
                .setFloat(SCALING_CONTRIBUTION, modifierScaling);
    }

    private AbilityMagicDamage() {
        super(EffectType.HARMFUL, 123);
        setRegistryName("effect.instant_indirect_magic_damage");
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public void doEffect(Entity applier, Entity caster, LivingEntity target, int amplifier, SpellCast cast) {
        float damage = cast.getScaledValue(amplifier);
        target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(applier, caster), damage);
    }
}

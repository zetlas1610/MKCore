package com.chaosbuffalo.mkcore.mku.effects;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellEffectBase;
import com.chaosbuffalo.mkcore.init.ModDamageTypes;
import com.chaosbuffalo.mkcore.mku.abilities.ClericHeal;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClericHealEffect extends SpellEffectBase {

    public static final ClericHealEffect INSTANCE = new ClericHealEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source, LivingEntity target, float base, float scaling) {
        return Create(source, base, scaling).setTarget(target);
    }

    public static SpellCast Create(Entity source, float base, float scaling) {
        return INSTANCE.newSpellCast(source).setScalingParameters(base, scaling);
    }

    private ClericHealEffect() {
        super(EffectType.BENEFICIAL, 4393481);
        setRegistryName("effect.cleric_heal");
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public boolean isValidTarget(TargetingContext targetContext, Entity caster, LivingEntity target) {
        return super.isValidTarget(targetContext, caster, target) ||
                (target.isEntityUndead() && MKConfig.healsDamageUndead.get());
    }

    @Override
    public boolean canSelfCast() {
        return true;
    }

    @Override
    public void doEffect(Entity applier, Entity caster, LivingEntity target, int amplifier, SpellCast cast) {

        float value = cast.getScaledValue(amplifier);

        float finalValue = MKCore.getPlayer(caster)
                .map(data -> MKCombatFormulas.applyHealBonus(data, value))
                .orElse(value);

        if (target.isEntityUndead()) {
            if (MKConfig.healsDamageUndead.get()) {
                float healDamageMultiplier = MKConfig.undeadHealDamageMultiplier.get();
                target.attackEntityFrom(MKDamageSource.causeAbilityDamage(ModDamageTypes.HolyDamage,
                        ClericHeal.INSTANCE.getAbilityId(), applier, caster), healDamageMultiplier * finalValue);
            }
        } else {
            target.heal(finalValue);
        }
    }
}

package com.chaosbuffalo.mkcore.core.healing;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.init.ModDamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

public class MKHealing {

    public static void healEntityFrom(LivingEntity target, float amount, MKHealSource healSource){
        float finalValue = MKCore.getEntityData(healSource.getTrueSource())
                .map(data -> MKCombatFormulas.applyHealBonus(data, amount))
                .orElse(amount);
        MKAbilityHealEvent event = new MKAbilityHealEvent(target, finalValue, healSource);
        if (!MinecraftForge.EVENT_BUS.post(event)){
            if (MKConfig.healsDamageUndead.get() && target.isEntityUndead() && healSource.doesDamageUndead()) {
                float healDamageMultiplier = MKConfig.undeadHealDamageMultiplier.get().floatValue();
                target.attackEntityFrom(MKDamageSource.causeAbilityDamage(ModDamageTypes.HolyDamage,
                        healSource.getAbilityId(), healSource.getImmediateSource(), healSource.getTrueSource()),
                        healDamageMultiplier * event.getAmount());
            } else {
                target.heal(event.getAmount());
            }
        }

    }
}

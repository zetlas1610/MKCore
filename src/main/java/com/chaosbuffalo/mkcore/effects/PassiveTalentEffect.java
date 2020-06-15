package com.chaosbuffalo.mkcore.effects;

import net.minecraft.potion.EffectType;

public abstract class PassiveTalentEffect extends PassiveEffect {
    protected PassiveTalentEffect() {
        super(EffectType.BENEFICIAL, 0);
    }

    @Override
    public boolean isInfiniteDuration() {
        return true;
    }
}

package com.chaosbuffalo.mkcore.effects;

import net.minecraft.potion.EffectType;

public abstract class SpellPeriodicPotionBase extends SpellPotionBase {

    private final int period;

    protected SpellPeriodicPotionBase(int period, EffectType typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);
        this.period = period;
    }

    @Override
    public boolean isReady(int duration, int amplitude) {
        return super.isReady(duration, amplitude) && duration % period == 0;
    }

    @Override
    public boolean isInstant() {
        return false;
    }

    public int getPeriod() {
        return period;
    }
}
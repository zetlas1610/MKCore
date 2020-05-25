package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public abstract class SongPotionBase extends SpellPeriodicPotionBase {

    private final boolean isVisible;

    protected SongPotionBase(int period, boolean isVisible, EffectType typeIn, int liquidColorIn) {
        super(period, typeIn, liquidColorIn);
        this.isVisible = isVisible;
    }

    @Override
    public Targeting.TargetType getTargetType() {
        return Targeting.TargetType.SELF;
    }

    @Override
    public boolean canSelfCast() {
        return true;
    }

    @Override
    public boolean canPersistAcrossSessions() {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(EffectInstance effect) {
        return isVisible;
    }

    @Override
    public boolean shouldRenderInvText(EffectInstance effect) {
        return isVisible;
    }

    @Override
    public boolean shouldRender(EffectInstance effect) {
        return isVisible;
    }

}

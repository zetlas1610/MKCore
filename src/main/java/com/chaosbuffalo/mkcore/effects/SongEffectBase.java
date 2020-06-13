package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public abstract class SongEffectBase extends SpellPeriodicEffectBase {

    private final boolean isVisible;

    protected SongEffectBase(int period, boolean isVisible, EffectType typeIn, int liquidColorIn) {
        super(period, typeIn, liquidColorIn);
        this.isVisible = isVisible;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
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

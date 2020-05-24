package com.chaosbuffalo.mkcore.abilities;


import net.minecraft.entity.LivingEntity;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class SingleTargetCastState extends CastState {

    private WeakReference<LivingEntity> target;

    public SingleTargetCastState(int castTime) {
        super(castTime);
    }

    public Optional<LivingEntity> getTarget() {
        return Optional.ofNullable(target.get()).filter(LivingEntity::isAlive);
    }

    public void setTarget(LivingEntity target) {
        this.target = new WeakReference<>(target);
    }
}

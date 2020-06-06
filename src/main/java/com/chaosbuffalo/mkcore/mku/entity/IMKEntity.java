package com.chaosbuffalo.mkcore.mku.entity;

import net.minecraft.entity.LivingEntity;

public interface IMKEntity {

    void addThreat(LivingEntity entity, int value);

    void reduceThreat(LivingEntity entity, int value);

}

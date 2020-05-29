package com.chaosbuffalo.mkcore.mku.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.world.World;

public class GreenLadyEntity extends ZombieEntity {

    public GreenLadyEntity(EntityType<? extends GreenLadyEntity> type, World worldIn) {
        super(type, worldIn);
    }
}

package com.chaosbuffalo.mkcore.mku;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.GreenLadyEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class MKUEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = new DeferredRegister<>(ForgeRegistries.ENTITIES,
            MKCore.MOD_ID);

    public static final String GREEN_LADY_NAME = "green_lady";

    public static final RegistryObject<EntityType<GreenLadyEntity>> GREEN_LADY = ENTITY_TYPES.register(
            GREEN_LADY_NAME, () ->
                    EntityType.Builder.create(GreenLadyEntity::new, EntityClassification.MONSTER)
                            .size(EntityType.ZOMBIE.getWidth(), EntityType.ZOMBIE.getHeight())
                            .build(new ResourceLocation(MKCore.MOD_ID, GREEN_LADY_NAME).toString())
    );
}

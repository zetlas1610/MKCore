package com.chaosbuffalo.mkcore.mku;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.GeyserProjectileEntity;
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
    public static final String GEYSER_PROJECTILE_NAME = "geyser_projectile";

    public static final RegistryObject<EntityType<GreenLadyEntity>> GREEN_LADY = ENTITY_TYPES.register(
            GREEN_LADY_NAME, () ->
                    EntityType.Builder.create(GreenLadyEntity::new, EntityClassification.CREATURE)
                            .size(EntityType.ZOMBIE.getWidth(), EntityType.ZOMBIE.getHeight())
                            .build(new ResourceLocation(MKCore.MOD_ID, GREEN_LADY_NAME).toString())
    );

    public static final RegistryObject<EntityType<GeyserProjectileEntity>> GEYSER_PROJECTILE =
            ENTITY_TYPES.register(GEYSER_PROJECTILE_NAME,
                    () -> EntityType.Builder.<GeyserProjectileEntity>create(GeyserProjectileEntity::new, EntityClassification.MISC)
                            .size(0.4f, 0.4f)
                            .setTrackingRange(64)
                            .setUpdateInterval(10)
                            .setShouldReceiveVelocityUpdates(true)
                            .disableSerialization()
                            .disableSummoning()
                            .build(GEYSER_PROJECTILE_NAME));
}

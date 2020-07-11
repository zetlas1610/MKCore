package com.chaosbuffalo.mkcore.mku.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.mku.entity.GeyserProjectileEntity;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Geyser extends MKAbility {
    public static final Geyser INSTANCE = new Geyser();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }


    public Geyser() {
        super(MKCore.makeRL("ability.geyser"));
        setCastTime(GameConstants.TICKS_PER_SECOND);
        setCooldownSeconds(1);
        setManaCost(3);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PROJECTILE;
    }


    public static float PROJECTILE_SPEED = 2.0f;
    public static float PROJECTILE_INACCURACY = 0.2f;

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {

        GeyserProjectileEntity projectile = new GeyserProjectileEntity(entity.world, entity);
        MKCore.LOGGER.info("Geyser endCast {} {}", entity, projectile);
        projectile.setAmplifier(1);
        projectile.shoot(entity, entity.rotationPitch, entity.rotationYaw, 0.0F, PROJECTILE_SPEED, PROJECTILE_INACCURACY);
        entity.world.addEntity(projectile);
    }
}

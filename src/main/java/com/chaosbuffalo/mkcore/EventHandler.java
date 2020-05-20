package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID)
public class EventHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity living = event.getEntityLiving();

        if (living instanceof PlayerEntity) {
            living.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(IMKPlayerData::update);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            if (event.getWorld().isRemote) {
                MKCore.LOGGER.info("client player joined world!");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone evt) {
        PlayerEntity player = evt.getPlayer();
        PlayerEntity oldPlayer = evt.getOriginal();

        player.getCapability(Capabilities.PLAYER_CAPABILITY)
                .ifPresent(newCap -> oldPlayer.getCapability(Capabilities.PLAYER_CAPABILITY)
                        .ifPresent(newCap::clone));
    }
}

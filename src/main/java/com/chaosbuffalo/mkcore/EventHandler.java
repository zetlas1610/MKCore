package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.effects.PassiveTalentEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID)
public class EventHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity living = event.getEntityLiving();

        if (living instanceof PlayerEntity) {
            living.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(MKPlayerData::update);
        } else {
            living.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent(MKEntityData::update);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            MKCore.getPlayer(event.getEntity()).ifPresent(MKPlayerData::onJoinWorld);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            MKCore.getPlayer(event.getEntity()).ifPresent(MKPlayerData::onJoinWorld);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            MKCore.getPlayer(event.getEntity()).ifPresent(MKPlayerData::onJoinWorld);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone evt) {
        PlayerEntity player = evt.getPlayer();
        PlayerEntity oldPlayer = evt.getOriginal();

        player.getCapability(Capabilities.PLAYER_CAPABILITY)
                .ifPresent(newCap -> oldPlayer.getCapability(Capabilities.PLAYER_CAPABILITY)
                        .ifPresent(oldCap -> newCap.clone(oldCap, evt.isWasDeath())));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
//        MKCore.LOGGER.info("StartTracking {} {}", event.getTarget(), event.getTarget().getEntityId());
        if (event.getTarget() instanceof ServerPlayerEntity) {
            PlayerEntity player = event.getPlayer();
            ServerPlayerEntity target = (ServerPlayerEntity) event.getTarget();

            player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> cap.fullSyncTo(target));
        }
    }

    @SubscribeEvent
    public static void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
//        MKCore.LOGGER.info("PotionRemoveEvent - {} - {}", event.getEntityLiving(), event.getPotion());

        if (event.getEntityLiving() instanceof ServerPlayerEntity && event.getPotion() instanceof PassiveTalentEffect) {
            MKCore.getPlayer(event.getEntityLiving()).ifPresent(playerData -> {
                if (!playerData.getTalentHandler().getTypeHandler(TalentType.PASSIVE).getPassiveTalentsUnlocked()) {
                    MKCore.LOGGER.info("Effect {} is a passive and passives are not unlocked", event.getPotion());
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityJump(LivingEvent.LivingJumpEvent event) {
        MKCore.getEntityData(event.getEntity()).ifPresent(entityData ->
                entityData.getAbilityExecutor().interruptCast());
    }
}

package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.core.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLeftClickEmptyPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingTarget = event.getEntityLiving();
        if (livingTarget.world.isRemote)
            return;

        DamageSource source = event.getSource();
        Entity trueSource = source.getTrueSource();
        if (source == DamageSource.FALL) { // TODO: maybe just use LivingFallEvent?
            SpellTriggers.FALL.onLivingFall(event, source, livingTarget);
        }

        // Player is the source
        if (trueSource instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerSource = (ServerPlayerEntity) trueSource;
            playerSource.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(
                    (sourceData) -> SpellTriggers.PLAYER_HURT_ENTITY.onPlayerHurtEntity(event, source, livingTarget,
                            playerSource, sourceData)
            );
        }

        // Player is the victim
        if (livingTarget instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerTarget = (ServerPlayerEntity) livingTarget;
            playerTarget.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(
                    (targetData) -> SpellTriggers.ENTITY_HURT_PLAYER.onEntityHurtPlayer(event, source,
                            playerTarget, targetData)
            );
        }
    }

    @SubscribeEvent
    public static void onAttackEntityEvent(AttackEntityEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.world.isRemote)
            return;
        Entity target = event.getTarget();

        SpellTriggers.PLAYER_ATTACK_ENTITY.onAttackEntity(player, target);
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getPlayer().world.isRemote) {
            PacketHandler.sendMessageToServer(new PlayerLeftClickEmptyPacket());
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmptyServer(ServerSideLeftClickEmpty event) {
        if (!event.getPlayer().world.isRemote) {
            SpellTriggers.EMPTY_LEFT_CLICK.onEmptyLeftClick(event.getPlayer(), event);
        }
    }

    @SubscribeEvent
    public static void onLivingAttackEvent(LivingAttackEvent event) {
        Entity target = event.getEntity();
        if (target.world.isRemote)
            return;

        DamageSource dmgSource = event.getSource();
        Entity source = dmgSource.getTrueSource();
        if (dmgSource instanceof MKDamageSource) {
            if (((MKDamageSource) dmgSource).shouldSuppressTriggers())
                return;
        }
        if (source instanceof LivingEntity) {
            SpellTriggers.ATTACK_ENTITY.onAttackEntity((LivingEntity) source, target);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();

        if (source.getTrueSource() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source.getTrueSource();
            if (player.world.isRemote) {
                return;
            }
            SpellTriggers.PLAYER_KILL_ENTITY.onEntityDeath(event, source, player);
        }
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            SpellTriggers.PLAYER_DEATH.onEntityDeath(event, source, player);
        }
    }


}

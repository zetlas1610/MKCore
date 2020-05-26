package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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

//         Player is the source
        if (trueSource instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerSource = (ServerPlayerEntity) trueSource;
            playerSource.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(
                    (sourceData) -> {
                        SpellTriggers.PLAYER_HURT_ENTITY.onPlayerHurtEntity(event, source, livingTarget,
                                playerSource, sourceData);
                    }
            );
        }

        // Player is the victim
//        if (livingTarget instanceof EntityPlayerMP) {
//            IPlayerData targetData = MKUPlayerData.get((EntityPlayerMP) livingTarget);
//            if (targetData == null) {
//                return;
//            }
//
//            SpellTriggers.ENTITY_HURT_PLAYER.onEntityHurtPlayer(event, source, (EntityPlayer) livingTarget, targetData);
//        }
    }
}

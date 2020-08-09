package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemEventHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();

        player.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent((playerData) -> {
            playerData.getEquipment().onEquipmentChange(event.getSlot(), event.getFrom(), event.getTo());
            SpellTriggers.PLAYER_EQUIPMENT_CHANGE.onEquipmentChange(event, playerData, player);
        });
    }
}

package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.IMKPlayer;
import com.chaosbuffalo.mkcore.core.PlayerCapabilityHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Capabilities {

    public static ResourceLocation PLAYER_CAP_ID = MKCore.makeRL("player_data");

    @CapabilityInject(IMKPlayer.class)
    public static final Capability<IMKPlayer> PLAYER_CAPABILITY;

    static {
        PLAYER_CAPABILITY = null;
    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(IMKPlayer.class, new PlayerCapabilityHandler.Storage(), PlayerCapabilityHandler::new);
        MinecraftForge.EVENT_BUS.register(Capabilities.class);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof PlayerEntity) {
            e.addCapability(PLAYER_CAP_ID, new PlayerCapabilityHandler.Provider((PlayerEntity)e.getObject()));
        }
    }
}

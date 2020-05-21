package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    private static SimpleChannel networkChannel;
    private static final String VERSION = "1.0";

    public static void setupHandler() {
        networkChannel = NetworkRegistry.newSimpleChannel(MKCore.makeRL("packet_handler"),
                () -> VERSION,
                s -> s.equals(VERSION),
                s -> s.equals(VERSION));
        registerMessages();
    }

    public static SimpleChannel getNetworkChannel() {
        return networkChannel;
    }

    public static void registerMessages() {
        int id = 1;
        networkChannel.registerMessage(id++, PlayerDataSyncPacket.class, PlayerDataSyncPacket::toBytes, PlayerDataSyncPacket::new, PlayerDataSyncPacket::handle);
        networkChannel.registerMessage(id++, AbilityCooldownPacket.class, AbilityCooldownPacket::toBytes, AbilityCooldownPacket::new, AbilityCooldownPacket::handle);
    }

    public static <T> void sendMessage(T msg, ServerPlayerEntity target) {
        PacketDistributor.PLAYER.with(() -> target)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToTrackingAndSelf(T msg, ServerPlayerEntity player) {
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }
}

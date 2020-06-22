package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.Entity;
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
        networkChannel.registerMessage(id++, PlayerDataSyncPacket.class, PlayerDataSyncPacket::toBytes,
                PlayerDataSyncPacket::new, PlayerDataSyncPacket::handle);
        networkChannel.registerMessage(id++, PlayerDataSyncRequestPacket.class, PlayerDataSyncRequestPacket::toBytes,
                PlayerDataSyncRequestPacket::new, PlayerDataSyncRequestPacket::handle);
        networkChannel.registerMessage(id++, ExecuteActiveAbilityPacket.class, ExecuteActiveAbilityPacket::toBytes,
                ExecuteActiveAbilityPacket::new, ExecuteActiveAbilityPacket::handle);
        networkChannel.registerMessage(id++, EntityStartCastPacket.class, EntityStartCastPacket::toBytes,
                EntityStartCastPacket::new, EntityStartCastPacket::handle);
        networkChannel.registerMessage(id++, ParticleEffectSpawnPacket.class, ParticleEffectSpawnPacket::toBytes,
                ParticleEffectSpawnPacket::new, ParticleEffectSpawnPacket::handle);
        networkChannel.registerMessage(id++, PlayerAbilitiesSyncPacket.class, PlayerAbilitiesSyncPacket::toBytes,
                PlayerAbilitiesSyncPacket::new, PlayerAbilitiesSyncPacket::handle);
        networkChannel.registerMessage(id++, CritMessagePacket.class, CritMessagePacket::toBytes,
                CritMessagePacket::new, CritMessagePacket::handle);
        networkChannel.registerMessage(id++, PlayerLeftClickEmptyPacket.class, PlayerLeftClickEmptyPacket::toBytes,
                PlayerLeftClickEmptyPacket::new, PlayerLeftClickEmptyPacket::handle);
        networkChannel.registerMessage(id++, PlayerSlotAbilityPacket.class, PlayerSlotAbilityPacket::toBytes,
                PlayerSlotAbilityPacket::new, PlayerSlotAbilityPacket::handle);
    }

    public static <T> void sendMessageToServer(T msg) {
        networkChannel.sendToServer(msg);
    }

    public static <T> void sendMessage(T msg, ServerPlayerEntity target) {
        PacketDistributor.PLAYER.with(() -> target)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToTracking(T msg, Entity entity) {
        PacketDistributor.TRACKING_ENTITY.with(() -> entity)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToTrackingAndSelf(T msg, ServerPlayerEntity player) {
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToTrackingMaybeSelf(T msg, Entity entity) {
        if (entity.world.isRemote)
            return;

        if (entity instanceof ServerPlayerEntity) {
            sendToTrackingAndSelf(msg, (ServerPlayerEntity) entity);
        } else {
            sendToTracking(msg, entity);
        }
    }

    public static <T> void sendToAll(T msg) {
        PacketDistributor.ALL.noArg().send(
                PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));

    }
}

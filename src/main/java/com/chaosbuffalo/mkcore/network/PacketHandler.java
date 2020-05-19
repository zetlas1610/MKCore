package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
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
    }
}

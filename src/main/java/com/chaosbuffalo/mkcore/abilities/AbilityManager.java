package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerAbilitiesSyncPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class AbilityManager extends JsonReloadListener {
    private final MinecraftServer server;

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public AbilityManager(MinecraftServer server) {
        super(GSON, "player_abilities");
        this.server = server;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> objectIn, IResourceManager resourceManagerIn,
                         IProfiler profilerIn) {
        MKCore.LOGGER.info("In apply reload for AbilityManager");
        boolean wasChanged = false;
        for (Map.Entry<ResourceLocation, JsonObject> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKCore.LOGGER.info("Found file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            if (parse(entry.getKey(), entry.getValue())) {
                wasChanged = true;
            }
        }
        if (wasChanged) {
            syncToPlayers();
        }
    }

    public void syncToPlayers() {
        PlayerAbilitiesSyncPacket updatePacket = new PlayerAbilitiesSyncPacket(MKCoreRegistry.ABILITIES.getValues());
        PacketHandler.sendToAll(updatePacket);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MKCore.LOGGER.info("Player logged in ability manager");
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            PlayerAbilitiesSyncPacket updatePacket = new PlayerAbilitiesSyncPacket(MKCoreRegistry
                    .ABILITIES.getValues());
            MKCore.LOGGER.info("Sending {} update packet", event.getPlayer());
            PacketHandler.sendMessage(updatePacket, (ServerPlayerEntity) event.getPlayer());
        }
    }

    private boolean parse(ResourceLocation loc, JsonObject json) {
        MKCore.LOGGER.info("Parsing Ability Json for {}", loc);
        ResourceLocation abilityLoc = new ResourceLocation(loc.getNamespace(),
                "ability." + loc.getPath());
        PlayerAbility ability = MKCoreRegistry.getAbility(abilityLoc);
        if (ability == null) {
            MKCore.LOGGER.warn("Failed to parse ability data for : {}", abilityLoc);
            return false;
        }
        ability.readFromDataPack(json);
        return true;
    }
}


package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.TalentDefinitionSyncPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TalentManager extends JsonReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, TalentTreeDefinition> talentTreeMap = new HashMap<>();

    public TalentManager() {
        super(GSON, "player_talents");

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonObject> objectIn,
                         @Nonnull IResourceManager resourceManagerIn,
                         @Nonnull IProfiler profilerIn) {

        MKCore.LOGGER.info("In apply reload for TalentLoader");
        boolean wasChanged = false;
        for (Map.Entry<ResourceLocation, JsonObject> entry : objectIn.entrySet()) {
            ResourceLocation location = entry.getKey();
            MKCore.LOGGER.info("Found file: {}", location);
            if (location.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            if (parse(entry.getKey(), entry.getValue())) {
                wasChanged = true;
            }
        }
        if (wasChanged) {
            syncToPlayers();
        }

    }

    private boolean parse(ResourceLocation loc, JsonObject json) {
        MKCore.LOGGER.debug("Parsing Talent Tree Json for {}", loc);
        ResourceLocation treeId = new ResourceLocation(loc.getNamespace(), "talent_tree." + loc.getPath());

        MKCore.LOGGER.debug("Creating new talent tree {}", treeId);
        TalentTreeDefinition talentTree = TalentTreeDefinition.deserialize(treeId, new Dynamic<>(JsonOps.INSTANCE, json));

        registerTalentTree(talentTree);
        return true;
    }

    public TalentTreeDefinition getTalentTree(ResourceLocation treeId) {
        return talentTreeMap.get(treeId);
    }

    public void registerTalentTree(TalentTreeDefinition tree) {
        talentTreeMap.put(tree.getTreeId(), tree);
    }

    public Collection<ResourceLocation> getTreeNames() {
        return Collections.unmodifiableCollection(talentTreeMap.keySet());
    }

    public static PassiveTalentAbility getPassiveTalentAbility(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability instanceof PassiveTalentAbility) {
            return (PassiveTalentAbility) ability;
        }
        return null;
    }

    public static MKAbility getTalentAbility(ResourceLocation talentId) {
        BaseTalent talent = MKCoreRegistry.TALENT_TYPES.getValue(talentId);
        if (talent instanceof IAbilityTalent<?>) {
            return ((IAbilityTalent<?>) talent).getAbility();
        } else {
            return null;
        }
    }

    public void syncToPlayers() {
        TalentDefinitionSyncPacket updatePacket = new TalentDefinitionSyncPacket(talentTreeMap.values());
        PacketHandler.sendToAll(updatePacket);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MKCore.LOGGER.info("Player logged in talent manager");
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            TalentDefinitionSyncPacket updatePacket = new TalentDefinitionSyncPacket(talentTreeMap.values());
            PacketHandler.sendMessage(updatePacket, (ServerPlayerEntity) event.getPlayer());
        }
    }
}

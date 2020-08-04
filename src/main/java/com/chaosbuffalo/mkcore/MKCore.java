package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.command.MKCommand;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.mku.MKUEntityTypes;
import com.chaosbuffalo.mkcore.mku.PersonaTest;
import com.chaosbuffalo.mkcore.mku.RenderRegistry;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MKCore.MOD_ID)
public class MKCore {
    public static final String MOD_ID = "mkcore";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    private AbilityManager abilityManager;
    private final TalentManager talentManager;

    public static MKCore INSTANCE;

    public MKCore() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        talentManager = new TalentManager();

        MKConfig.init();
        MKUEntityTypes.ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        PacketHandler.setupHandler();
        Capabilities.registerCapabilities();
        PersonaManager.registerExtension(PersonaTest.CustomPersonaData::new);
        MKCommand.registerArguments();
    }

    @SubscribeEvent
    public void serverStart(final FMLServerAboutToStartEvent event) {
        // some preinit code
        abilityManager = new AbilityManager(event.getServer());
        event.getServer().getResourceManager().addReloadListener(abilityManager);
        event.getServer().getResourceManager().addReloadListener(talentManager);
        LOGGER.info("HELLO FROM ABOUTTOSTART");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        MinecraftForge.EVENT_BUS.register(new MKOverlay());
        ClientEventHandler.initKeybindings();
        RenderRegistry.registerRenderers();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
        MKCommand.registerCommands(event.getCommandDispatcher());
    }

    private void processIMC(final InterModProcessEvent event)
    {
        MKCore.LOGGER.info("MKCore.processIMC");
        event.getIMCStream().forEach(m -> {
            if (m.getMethod().equals("register_persona_extension")) {
                MKCore.LOGGER.info("IMC register persona extension from mod {} {}", m.getSenderModId(), m.getMethod());
                IPersonaExtensionProvider factory = (IPersonaExtensionProvider) m.getMessageSupplier().get();
                PersonaManager.registerExtension(factory);
            }
        });
    }

    public static ResourceLocation makeRL(String path) {
        return new ResourceLocation(MKCore.MOD_ID, path);
    }

    public static LazyOptional<MKPlayerData> getPlayer(Entity playerEntity) {
        return playerEntity.getCapability(Capabilities.PLAYER_CAPABILITY);
    }

    public static LazyOptional<? extends IMKEntityData> getEntityData(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return entity.getCapability(Capabilities.PLAYER_CAPABILITY);
        } else {
            return entity.getCapability(Capabilities.ENTITY_CAPABILITY);
        }
    }

    public static TalentManager getTalentManager() {
        return INSTANCE.talentManager;
    }

    public static AbilityManager getAbilityManager() {
        return INSTANCE.abilityManager;
    }
}

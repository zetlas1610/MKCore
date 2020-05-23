package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.command.MKCommand;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

    public static MKCore INSTANCE;

    public MKCore() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MKConfig.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, MKConfig.SERVER_CONFIG);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        PacketHandler.setupHandler();
        Capabilities.registerCapabilities();
    }

    @SubscribeEvent
    public void serverStart(final FMLServerAboutToStartEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM ABOUTTOSTART");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        MinecraftForge.EVENT_BUS.register(new MKOverlay());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
        MKCommand.register(event.getCommandDispatcher());
    }

    public static ResourceLocation makeRL(String path) {
        return new ResourceLocation(MKCore.MOD_ID, path);
    }
}

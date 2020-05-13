package com.chaosbuffalo.mkcore;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MKConfig {

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec CLIENT_CONFIG;

    static {
        initClient(CLIENT_BUILDER);
        initServer(SERVER_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static final String CATEGORY_GENERAL = "general";

    public static ForgeConfigSpec.BooleanValue doLogging;
    public static ForgeConfigSpec.BooleanValue doClientLogging;

    private static void initClient(ForgeConfigSpec.Builder CLIENT_BUILDER) {
        CLIENT_BUILDER.comment("General settings for all mods using mcjtylib").push(CATEGORY_GENERAL);

        doClientLogging = SERVER_BUILDER.comment("Do logging on the client").define("logging", false);

        CLIENT_BUILDER.pop();
    }

    private static void initServer(ForgeConfigSpec.Builder SERVER_BUILDER) {
        SERVER_BUILDER.comment("General settings for all mods using mcjtylib").push(CATEGORY_GENERAL);

        doLogging = SERVER_BUILDER.comment("Do logging on the server").define("logging", false);

        SERVER_BUILDER.pop();
    }
}

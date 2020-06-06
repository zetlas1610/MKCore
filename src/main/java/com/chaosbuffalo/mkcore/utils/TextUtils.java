package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;

public class TextUtils {

    public static void sendPlayerChatMessage(PlayerEntity playerEntity, ITextComponent message, boolean brackets) {
        if (brackets)
            message = TextComponentUtils.wrapInSquareBrackets(message);
        playerEntity.sendStatusMessage(message, false);
    }

    public static void sendPlayerChatMessage(PlayerEntity playerEntity, String message) {
        sendPlayerChatMessage(playerEntity, new StringTextComponent(message), true);
    }

    public static void sendChatMessage(PlayerEntity playerEntity, String message) {
        sendPlayerChatMessage(playerEntity, new StringTextComponent(message), false);
    }

}

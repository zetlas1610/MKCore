package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;

public class TextUtils {

    public static void sendPlayerChatMessage(PlayerEntity playerEntity, ITextComponent message) {
        message = TextComponentUtils.wrapInSquareBrackets(message);
        playerEntity.sendStatusMessage(message, false);
    }

    public static void sendPlayerChatMessage(PlayerEntity playerEntity, String message) {
        sendPlayerChatMessage(playerEntity, new StringTextComponent(message));
    }

}

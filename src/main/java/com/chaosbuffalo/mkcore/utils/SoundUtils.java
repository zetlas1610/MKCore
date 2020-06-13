package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class SoundUtils {
    public static void playSoundAtEntity(Entity entity, SoundEvent event) {
        playSoundAtEntity(entity, event, entity.getSoundCategory(), 1.0f, 1.0f);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundCategory cat) {
        playSoundAtEntity(entity, event, cat, 1.0f, 1.0f);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundCategory cat, float volume, float pitch) {
        if (event == null) {
            return;
        }
        entity.world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), event, cat, volume, pitch);
    }
}

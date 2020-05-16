package com.chaosbuffalo.mkcore.core;

import net.minecraft.util.ResourceLocation;

public interface IPlayerClass {
    ResourceLocation getClassId();

    IPlayerClassInfo createClassInfo();
}

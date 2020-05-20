package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class SyncResourceLocation extends SyncObject<ResourceLocation> {

    static void serialize(CompoundNBT tag, SyncObject<ResourceLocation> instance) {
        tag.putString(instance.name, instance.get().toString());
    }

    static void deserialize(CompoundNBT tag, SyncObject<ResourceLocation> instance) {
        instance.set(new ResourceLocation(tag.getString(instance.name)));
    }

    public SyncResourceLocation(String name, ResourceLocation value) {
        super(name, value, SyncResourceLocation::serialize, SyncResourceLocation::deserialize);
    }
}

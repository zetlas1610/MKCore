package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.IMKPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

public class Capabilities {

    public static ResourceLocation PLAYER_CAP_ID = MKCore.makeRL("player_data");

    @CapabilityInject(IMKPlayer.class)
    public static final Capability<IMKPlayer> PLAYER_CAPABILITY;

    static {
        PLAYER_CAPABILITY = null;
    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(IMKPlayer.class, new PlayerCapabilityHandler.Storage(), PlayerCapabilityHandler::new);
        MinecraftForge.EVENT_BUS.register(Capabilities.class);
    }

    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof PlayerEntity) {
            e.addCapability(PLAYER_CAP_ID, new ICapabilitySerializable<CompoundNBT>() {

                IMKPlayer inst = PLAYER_CAPABILITY.getDefaultInstance();
                {
                    inst.attach((PlayerEntity) e.getObject());
                }

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
                    return PLAYER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> inst));
                }

                @Override
                public CompoundNBT serializeNBT() {
                    return (CompoundNBT) PLAYER_CAPABILITY.getStorage().writeNBT(PLAYER_CAPABILITY, inst, null);
                }

                @Override
                public void deserializeNBT(CompoundNBT nbt) {
                    PLAYER_CAPABILITY.getStorage().readNBT(PLAYER_CAPABILITY, inst, null, nbt);
                }

            });
        }
    }
}


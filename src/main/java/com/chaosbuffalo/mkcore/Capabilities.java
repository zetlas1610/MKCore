package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
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
import javax.annotation.Nullable;

public class Capabilities {

    public static ResourceLocation PLAYER_CAP_ID = MKCore.makeRL("player_data");

    @CapabilityInject(IMKPlayerData.class)
    public static final Capability<IMKPlayerData> PLAYER_CAPABILITY;

    static {
        PLAYER_CAPABILITY = null;
    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(IMKPlayerData.class, new PlayerDataStorage(), MKPlayerData::new);
        MinecraftForge.EVENT_BUS.register(Capabilities.class);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof PlayerEntity) {
            e.addCapability(PLAYER_CAP_ID, new PlayerDataProvider((PlayerEntity) e.getObject()));
        }
    }


    public static class PlayerDataStorage implements Capability.IStorage<IMKPlayerData> {

        @Override
        public CompoundNBT writeNBT(Capability<IMKPlayerData> capability, IMKPlayerData instance, Direction side) {
            CompoundNBT tag = new CompoundNBT();
            instance.serialize(tag);
            return tag;
        }

        @Override
        public void readNBT(Capability<IMKPlayerData> capability, IMKPlayerData instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserialize(tag);
            }
        }
    }

    public static class PlayerDataProvider implements ICapabilitySerializable<CompoundNBT> {
        private final IMKPlayerData playerHandler;

        public PlayerDataProvider(PlayerEntity playerEntity) {
            playerHandler = Capabilities.PLAYER_CAPABILITY.getDefaultInstance();
            if (playerHandler != null) {
                playerHandler.attach(playerEntity);
            }
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return Capabilities.PLAYER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> playerHandler));
        }

        @Override
        public CompoundNBT serializeNBT() {
            return (CompoundNBT) Capabilities.PLAYER_CAPABILITY.getStorage().writeNBT(Capabilities.PLAYER_CAPABILITY, playerHandler, null);
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            Capabilities.PLAYER_CAPABILITY.getStorage().readNBT(Capabilities.PLAYER_CAPABILITY, playerHandler, null, nbt);
        }
    }
}

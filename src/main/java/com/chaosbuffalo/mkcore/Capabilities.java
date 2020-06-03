package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
    public static ResourceLocation ENTITY_CAP_ID = MKCore.makeRL("entity_data");

    @CapabilityInject(MKPlayerData.class)
    public static final Capability<MKPlayerData> PLAYER_CAPABILITY;

    @CapabilityInject(MKEntityData.class)
    public static final Capability<MKEntityData> ENTITY_CAPABILITY;

    static {
        PLAYER_CAPABILITY = null;
        ENTITY_CAPABILITY = null;
    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(MKPlayerData.class, new MKDataStorage<>(), MKPlayerData::new);
        CapabilityManager.INSTANCE.register(MKEntityData.class, new MKDataStorage<>(), MKEntityData::new);
        MinecraftForge.EVENT_BUS.register(Capabilities.class);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof PlayerEntity) {
            e.addCapability(PLAYER_CAP_ID, new PlayerDataProvider((PlayerEntity) e.getObject()));
        } else if (e.getObject() instanceof LivingEntity){
            e.addCapability(ENTITY_CAP_ID, new EntityDataProvider((LivingEntity) e.getObject()));
        }
    }


    public static class MKDataStorage<T extends IMKEntityData> implements Capability.IStorage<T> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
            CompoundNBT tag = new CompoundNBT();
            instance.serialize(tag);
            return tag;
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserialize(tag);
            }
        }
    }

    public static class EntityDataProvider implements ICapabilitySerializable<CompoundNBT> {
        private final MKEntityData entityHandler;

        public EntityDataProvider(LivingEntity entity){
            entityHandler = Capabilities.ENTITY_CAPABILITY.getDefaultInstance();
            if (entityHandler != null){
                entityHandler.attach(entity);
            }
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return Capabilities.ENTITY_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> entityHandler));
        }

        @Override
        public CompoundNBT serializeNBT() {
            return (CompoundNBT) Capabilities.ENTITY_CAPABILITY.getStorage().writeNBT(
                    Capabilities.ENTITY_CAPABILITY, entityHandler, null);
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            Capabilities.ENTITY_CAPABILITY.getStorage().readNBT(
                    Capabilities.ENTITY_CAPABILITY, entityHandler, null, nbt);
        }
    }


    public static class PlayerDataProvider implements ICapabilitySerializable<CompoundNBT> {
        private final MKPlayerData playerHandler;

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

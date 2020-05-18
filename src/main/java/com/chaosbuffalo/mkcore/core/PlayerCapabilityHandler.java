package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class PlayerCapabilityHandler implements IMKPlayer {

    private PlayerEntity player;

    public PlayerCapabilityHandler() {

    }

    @Override
    public void attach(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void update() {

        MKCore.LOGGER.info("update {}", this.player);
    }

    @Override
    public void serialize(CompoundNBT nbt) {

    }

    @Override
    public void deserialize(CompoundNBT nbt) {

    }


    public static class Storage implements Capability.IStorage<IMKPlayer> {

        @Override
        public CompoundNBT writeNBT(Capability<IMKPlayer> capability, IMKPlayer instance, Direction side) {
            CompoundNBT tag = new CompoundNBT();
            instance.serialize(tag);
            return tag;
        }

        @Override
        public void readNBT(Capability<IMKPlayer> capability, IMKPlayer instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserialize(tag);
            }
        }
    }
}

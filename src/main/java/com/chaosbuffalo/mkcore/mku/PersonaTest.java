package com.chaosbuffalo.mkcore.mku;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class PersonaTest {

    public static class CustomPersonaData implements IPersonaExtension {
        private final Persona persona;
        private int counter;

        public CustomPersonaData(Persona persona) {
            this.persona = persona;
            counter = 1;
        }

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation("mkultra", "custom");
        }

        @Override
        public void onPersonaActivated() {
            counter++;
            MKCore.LOGGER.info("CustomPersonaData.onPersonaActivated {}", counter);
        }

        @Override
        public void onPersonaDeactivated() {
            MKCore.LOGGER.info("CustomPersonaData.onPersonaDeactivated {}", counter);
        }

        @Override
        public CompoundNBT serialize() {
            MKCore.LOGGER.info("CustomPersonaData.serialize {}", counter);
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("counter", counter);
            return tag;
        }

        @Override
        public void deserialize(CompoundNBT tag) {
            MKCore.LOGGER.info("CustomPersonaData.deserialize");

            counter = tag.getInt("counter");

            MKCore.LOGGER.info("deser counter {}", counter);
        }
    }
}

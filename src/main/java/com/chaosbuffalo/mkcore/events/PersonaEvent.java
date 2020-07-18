package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.PersonaManager;
import net.minecraft.nbt.CompoundNBT;

public class PersonaEvent extends MKPlayerEvent {
    private final PersonaManager.Persona persona;

    public PersonaEvent(MKPlayerData playerData, PersonaManager.Persona persona) {
        super(playerData);
        this.persona = persona;
    }

    public PersonaManager.Persona getPersona() {
        return persona;
    }

    public static class PersonaSerializationEvent extends PersonaEvent {
        private final CompoundNBT rootTag;

        public PersonaSerializationEvent(PersonaManager.Persona persona,
                                         CompoundNBT rootTag) {
            super(persona.getPlayerData(), persona);
            this.rootTag = rootTag;
        }

        public CompoundNBT getPersonaNBT() {
            return rootTag;
        }
    }

    public static class PersonaDeserializationEvent extends PersonaSerializationEvent {

        public PersonaDeserializationEvent(PersonaManager.Persona persona,
                                           CompoundNBT rootTag) {
            super(persona, rootTag);
        }
    }

    public static class PersonaActivated extends PersonaEvent {

        public PersonaActivated(PersonaManager.Persona persona) {
            super(persona.getPlayerData(), persona);
        }
    }

    public static class PersonaDeactivated extends PersonaEvent {

        public PersonaDeactivated(PersonaManager.Persona persona) {
            super(persona.getPlayerData(), persona);
        }
    }
}

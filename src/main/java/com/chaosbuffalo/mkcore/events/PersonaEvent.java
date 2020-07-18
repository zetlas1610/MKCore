package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.PersonaManager;
import net.minecraft.nbt.CompoundNBT;

public class PersonaEvent extends MKPlayerEvent {
    private final PersonaManager.Persona persona;

    public PersonaEvent(PersonaManager.Persona persona) {
        super(persona.getPlayerData());
        this.persona = persona;
    }

    public PersonaManager.Persona getPersona() {
        return persona;
    }

    public static class PersonaSerializationEvent extends PersonaEvent {
        private final CompoundNBT personaTag;

        public PersonaSerializationEvent(PersonaManager.Persona persona,
                                         CompoundNBT personaTag) {
            super(persona);
            this.personaTag = personaTag;
        }

        public CompoundNBT getPersonaNBT() {
            return personaTag;
        }
    }

    public static class PersonaDeserializationEvent extends PersonaSerializationEvent {

        public PersonaDeserializationEvent(PersonaManager.Persona persona,
                                           CompoundNBT personaTag) {
            super(persona, personaTag);
        }
    }

    public static class PersonaActivated extends PersonaEvent {

        public PersonaActivated(PersonaManager.Persona persona) {
            super(persona);
        }
    }

    public static class PersonaDeactivated extends PersonaEvent {

        public PersonaDeactivated(PersonaManager.Persona persona) {
            super(persona);
        }
    }
}

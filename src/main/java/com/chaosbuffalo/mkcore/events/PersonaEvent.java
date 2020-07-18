package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.PersonaManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;

public class PersonaEvent extends Event {
    private final PersonaManager.Persona persona;

    public PersonaEvent(PersonaManager.Persona persona) {
        this.persona = persona;
    }

    public PersonaManager.Persona getPersona() {
        return persona;
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

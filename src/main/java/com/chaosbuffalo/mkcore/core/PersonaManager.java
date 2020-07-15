package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.*;

public class PersonaManager implements IMKSerializable<CompoundNBT> {
    public static final String DEFAULT_PERSONA_NAME = "default";

    private final MKPlayerData playerData;
    private final Map<String, Persona> personas = new HashMap<>();
    protected Persona activePersona;

    public PersonaManager(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    @Override
    public void serialize(CompoundNBT tag) {
        if (activePersona == null) {
            // When creating a new character it comes to serialize first, so create the default persona here if none is active
            loadPersona(DEFAULT_PERSONA_NAME);
        }

        CompoundNBT personaRoot = new CompoundNBT();
        personas.forEach((name, persona) -> {
            CompoundNBT personaTag = new CompoundNBT();
            persona.serialize(personaTag);
            personaRoot.put(name, personaTag);
        });

        tag.put("personas", personaRoot);
        tag.putString("activePersona", getActivePersona().getName());
    }

    private void loadPersona(String name) {
        // Look for the specified persona, or create a new persona if it does not exist
        activatePersonaInternal(personas.computeIfAbsent(name, this::createNewPersona), true);
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        CompoundNBT personaRoot = tag.getCompound("personas");
        for (String name : personaRoot.keySet()) {
            CompoundNBT personaTag = personaRoot.getCompound(name);
            Persona persona = new Persona(playerData, name);
            if (!persona.deserialize(personaTag)) {
                MKCore.LOGGER.error("Failed to deserialize persona {} for {}", name, playerData.getEntity());
                continue;
            }

            personas.put(name, persona);
        }

        String activePersonaName = DEFAULT_PERSONA_NAME;
        if (tag.contains("activePersona")) {
            activePersonaName = tag.getString("activePersona");
        }

        loadPersona(activePersonaName);
        return true;
    }

    protected Persona createNewPersona(String name) {
        return new Persona(playerData, name);
    }

    protected void setActivePersona(Persona persona) {
        activePersona = persona;
    }

    public Persona getActivePersona() {
        Objects.requireNonNull(activePersona);
        return activePersona;
    }

    public Collection<String> getPersonaNames() {
        return Collections.unmodifiableCollection(personas.keySet());
    }

    public Persona getPersona(String name) {
        return personas.get(name);
    }

    public boolean createPersona(String name) {
        if (hasPersona(name)) {
            MKCore.LOGGER.info("Cannot create a persona named {}! Persona with that name already exists.", name);
            return false;
        }

        personas.put(name, createNewPersona(name));
        return true;
    }

    public boolean isPersonaActive(String name) {
        return getActivePersona().getName().equalsIgnoreCase(name);
    }

    public boolean hasPersona(String name) {
        return personas.containsKey(name);
    }

    public boolean deletePersona(String name) {
        if (!hasPersona(name)) {
            MKCore.LOGGER.error("deletePersona({}) - persona does not exist!", name);
            return false;
        }

        if (isPersonaActive(name)) {
            MKCore.LOGGER.error("deletePersona({}) - cannot delete active persona!", name);
            return false;
        }

        personas.remove(name);
        return true;
    }

    public void activatePersona(PersonaManager.Persona persona) {
        activatePersonaInternal(persona, false);
    }

    public boolean activatePersona(String name) {
        PersonaManager.Persona newPersona = getPersona(name);
        if (newPersona == null) {
            MKCore.LOGGER.error("Failed to activate unknown persona {}", name);
            return false;
        }

        activatePersona(newPersona);
        return true;
    }

    private void activatePersonaInternal(Persona persona, boolean firstActivation) {
        MKCore.LOGGER.debug("activatePersona({}) {} ", persona.getName(), playerData.getEntity());
        if (!firstActivation && getActivePersona() != persona) {
            Persona current = getActivePersona();
            MKCore.LOGGER.debug("activatePersona({}) - deactivating previous {}", persona.getName(), current.getName());
            playerData.onPersonaDeactivated();
        }

        setActivePersona(persona);
        playerData.onPersonaActivated();
    }

    public static class Persona implements IMKSerializable<CompoundNBT> {
        private final String name;
        private final PlayerKnowledge knowledge;

        public Persona(MKPlayerData playerData, String name) {
            this.name = name;
            knowledge = new PlayerKnowledge(playerData);
        }

        public String getName() {
            return name;
        }

        public PlayerKnowledge getKnowledge() {
            return knowledge;
        }

        @Override
        public void serialize(CompoundNBT tag) {
            CompoundNBT knowledgeTag = new CompoundNBT();
            knowledge.serialize(knowledgeTag);
            tag.put("knowledge", knowledgeTag);

        }

        @Override
        public boolean deserialize(CompoundNBT tag) {
            knowledge.deserialize(tag.getCompound("knowledge"));
            return true;
        }
    }

    // The client only has a single persona that will be overwritten when the server changes
    public static class ClientPersonaManager extends PersonaManager {

        public ClientPersonaManager(MKPlayerData playerData) {
            super(playerData);

            setActivePersona(createNewPersona("client_persona"));
            getActivePersona().getKnowledge().getSyncComponent().attach(playerData.getUpdateEngine());
        }
    }

    public static PersonaManager getPersonaManager(MKPlayerData playerData) {
        if (playerData.getEntity() instanceof ServerPlayerEntity) {
            return new PersonaManager(playerData);
        } else {
            return new ClientPersonaManager(playerData);
        }
    }
}

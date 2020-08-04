package com.chaosbuffalo.mkcore.core.persona;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.PlayerKnowledge;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundNBT;

import java.util.IdentityHashMap;
import java.util.Map;

public class Persona implements IMKSerializable<CompoundNBT> {
    private final String name;
    private final PlayerKnowledge knowledge;
    private final MKPlayerData data;
    private final Map<Class<? extends IPersonaExtension>, IPersonaExtension> extensions = new IdentityHashMap<>();

    public Persona(MKPlayerData playerData, String name) {
        this.name = name;
        knowledge = new PlayerKnowledge(playerData);
        data = playerData;
    }

    public String getName() {
        return name;
    }

    public PlayerKnowledge getKnowledge() {
        return knowledge;
    }

    public MKPlayerData getPlayerData() {
        return data;
    }

    void registerExtension(IPersonaExtension extension) {
        extensions.put(extension.getClass(), extension);
    }

    public <T extends IPersonaExtension> T getExtension(Class<T> clazz) {
//            MKCore.LOGGER.info("getExtension {} {}", extensions.size(), extensions.values().stream().map(Objects::toString).collect(Collectors.joining(",")));
        IPersonaExtension extension = extensions.get(clazz);
        return extension == null ? null : clazz.cast(extension);
    }

    public void activate() {
        knowledge.getSyncComponent().attach(data.getUpdateEngine());
        knowledge.onPersonaActivated();
        getPlayerData().onPersonaActivated();
        extensions.values().forEach(IPersonaExtension::onPersonaActivated);
    }

    public void deactivate() {
        knowledge.onPersonaDeactivated();
        knowledge.getSyncComponent().detach(data.getUpdateEngine());
        getPlayerData().onPersonaDeactivated();
        extensions.values().forEach(IPersonaExtension::onPersonaDeactivated);
    }

    private CompoundNBT serializeExtensions() {
        CompoundNBT root = new CompoundNBT();
        extensions.values().forEach(extension -> {
            CompoundNBT output = extension.serialize();
            if (output != null) {
                root.put(extension.getName().toString(), output);
            }
        });
        return root;
    }

    private void deserializeExtensions(CompoundNBT root) {
        if (root.isEmpty())
            return;

        extensions.values().forEach(extension -> {
            String name = extension.getName().toString();
            if (root.contains(name)) {
                extension.deserialize(root.getCompound(name));
            }
        });
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("knowledge", knowledge.serialize());
        tag.put("extensions", serializeExtensions());
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        knowledge.deserialize(tag.getCompound("knowledge"));
        deserializeExtensions(tag.getCompound("extensions"));
        return true;
    }
}

package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentModule;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashSet;
import java.util.Set;

public class MKPlayerData implements IMKEntityData {

    private PlayerEntity player;
    private PlayerAbilityExecutor abilityExecutor;
    private PlayerStatsModule stats;
    private PersonaManager personaManager;
    private UpdateEngine updateEngine;
    private PlayerAnimationModule animationModule;
    private PlayerTalentModule talentModule;
    private PlayerEquipmentModule equipmentModule;
    private final Set<String> spellTag = new HashSet<>();

    public MKPlayerData() {

    }

    public void attach(PlayerEntity newPlayer) {
        player = newPlayer;
        updateEngine = new UpdateEngine(this);
        personaManager = PersonaManager.getPersonaManager(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        stats = new PlayerStatsModule(this);
        stats.getSyncComponent().attach(updateEngine);

        animationModule = new PlayerAnimationModule(this);
        abilityExecutor.setStartCastCallback(animationModule::startCast);
        abilityExecutor.setCompleteAbilityCallback(animationModule::endCast);

        talentModule = new PlayerTalentModule(this);
        equipmentModule = new PlayerEquipmentModule(this);

        registerAttributes();
        if (isServerSide())
            setupFakeStats();
    }

    void setupFakeStats() {
        AttributeModifier mod = new AttributeModifier("test max mana", 20, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(MKAttributes.MAX_MANA).applyModifier(mod);

        AttributeModifier mod2 = new AttributeModifier("test mana regen", 1, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(MKAttributes.MANA_REGEN).applyModifier(mod2);

        AttributeModifier mod3 = new AttributeModifier("test cdr", 0.1, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(MKAttributes.COOLDOWN).applyModifier(mod3);

        AttributeModifier mod4 = new AttributeModifier("test haste", 0.1, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(MKAttributes.CASTING_SPEED).applyModifier(mod4);
    }

    private void registerAttributes() {
        AbstractAttributeMap attributes = player.getAttributes();
        MKAttributes.registerEntityAttributes(attributes);
        attributes.registerAttribute(MKAttributes.MAX_MANA);
        attributes.registerAttribute(MKAttributes.MANA_REGEN);
        attributes.registerAttribute(MKAttributes.MELEE_CRIT);
        attributes.registerAttribute(MKAttributes.MELEE_CRIT_MULTIPLIER);
        attributes.registerAttribute(MKAttributes.SPELL_CRIT);
        attributes.registerAttribute(MKAttributes.SPELL_CRIT_MULTIPLIER);
    }

    public void onJoinWorld() {
        getPersonaManager().ensurePersonaLoaded();
        getAbilityExecutor().onJoinWorld();
        getTalentHandler().onJoinWorld();
        if (isServerSide()) {
            MKCore.LOGGER.info("server player joined world!");
            initialSync();
        } else {
            MKCore.LOGGER.info("client player joined world!");
        }
    }

    @Override
    public PlayerStatsModule getStats() {
        return stats;
    }

    @Override
    public PlayerAbilityExecutor getAbilityExecutor() {
        return abilityExecutor;
    }

    @Override
    public PlayerKnowledge getKnowledge() {
        return getPersonaManager().getActivePersona().getKnowledge();
    }

    public UpdateEngine getUpdateEngine() {
        return updateEngine;
    }

    public PersonaManager getPersonaManager() {
        return personaManager;
    }

    public PlayerTalentModule getTalentHandler() {
        return talentModule;
    }

    public PlayerEquipmentModule getEquipment() {
        return equipmentModule;
    }

    public void clone(IMKEntityData previous, boolean death) {
        CompoundNBT tag = previous.serialize();
        deserialize(tag);
    }

    @Override
    public PlayerEntity getEntity() {
        return player;
    }

    public PlayerAnimationModule getAnimationModule() {
        return animationModule;
    }

    public boolean isServerSide() {
        return player instanceof ServerPlayerEntity;
    }

    public void update() {
        getStats().tick();
        getAbilityExecutor().tick();
        getAnimationModule().tick();

        if (!isServerSide()) {
            // client-only handling here
            return;
        }

        syncState();
    }

    private void syncState() {
        updateEngine.syncUpdates();
    }

    public void fullSyncTo(ServerPlayerEntity otherPlayer) {
        MKCore.LOGGER.info("Full public sync {} -> {}", player, otherPlayer);
        updateEngine.sendAll(otherPlayer);
    }

    public void initialSync() {
        MKCore.LOGGER.info("Sending initial sync for {}", player);
        if (isServerSide()) {
            updateEngine.sendAll((ServerPlayerEntity) player);
        }
    }

    public void onPersonaActivated() {
        getTalentHandler().onPersonaActivated();
        getAbilityExecutor().onPersonaActivated();
        getStats().onPersonaActivated();
    }

    public void onPersonaDeactivated() {
        getTalentHandler().onPersonaDeactivated();
        getAbilityExecutor().onPersonaDeactivated();
        getStats().onPersonaDeactivated();
    }

    public <T extends IPersonaExtension> T getPersonaExtension(Class<T> clazz) {
        return getPersonaManager().getActivePersona().getExtension(clazz);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("persona", personaManager.serialize());
        tag.put("stats", getStats().serialize());
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        personaManager.deserialize(tag.getCompound("persona"));
        getStats().deserialize(tag.getCompound("stats"));
    }

    public void addSpellTag(String tag) {
        spellTag.add(tag);
    }

    public void removeSpellTag(String tag) {
        spellTag.remove(tag);
    }

    public boolean hasSpellTag(String tag) {
        return spellTag.contains(tag);
    }
}
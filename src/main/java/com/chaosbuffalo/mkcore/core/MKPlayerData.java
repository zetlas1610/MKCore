package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
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
    private final Set<String> spellTag = new HashSet<>();

    public MKPlayerData() {

    }


    @Override
    public PlayerStatsModule getStats() {
        return stats;
    }

    public void attach(PlayerEntity newPlayer) {
        player = newPlayer;
        updateEngine = new UpdateEngine(this);
        personaManager = PersonaManager.getPersonaManager(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        stats = new PlayerStatsModule(this);
        animationModule = new PlayerAnimationModule(this);
        abilityExecutor.setStartCastCallback(animationModule::startCast);
        abilityExecutor.setCompleteAbilityCallback(animationModule::endCast);
        stats.attach(updateEngine);

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
    }

    private void registerAttributes() {
        AbstractAttributeMap attributes = player.getAttributes();
        attributes.registerAttribute(MKAttributes.MAX_MANA);
        attributes.registerAttribute(MKAttributes.MANA_REGEN);
        attributes.registerAttribute(MKAttributes.COOLDOWN);
        attributes.registerAttribute(MKAttributes.MELEE_CRIT);
        attributes.registerAttribute(MKAttributes.MELEE_CRIT_MULTIPLIER);
        attributes.registerAttribute(MKAttributes.SPELL_CRIT);
        attributes.registerAttribute(MKAttributes.SPELL_CRIT_MULTIPLIER);
        attributes.registerAttribute(MKAttributes.HEAL_BONUS);
        for (MKDamageType damageType : MKCoreRegistry.DAMAGE_TYPES.getValues()) {
            damageType.addAttributes(attributes);
        }
    }

    public void onJoinWorld() {
        getAbilityExecutor().onJoinWorld();
        if (isServerSide()) {
            MKCore.LOGGER.info("server player joined world!");
            initialSync();
        } else {
            MKCore.LOGGER.info("client player joined world!");
        }
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

    public void clone(IMKEntityData previous, boolean death) {
        MKCore.LOGGER.info("onDeath!");

        CompoundNBT tag = new CompoundNBT();
        previous.serialize(tag);
        deserialize(tag);
    }

    @Override
    public PlayerEntity getEntity() {
        return player;
    }

    public PlayerAnimationModule getAnimationModule() {
        return animationModule;
    }

    private boolean isServerSide() {
        return player instanceof ServerPlayerEntity;
    }

    public void update() {
        getStats().tick();
        getAbilityExecutor().tick();
        getAnimationModule().tick();

//        MKCore.LOGGER.info("update {} {}", this.player, mana.get());

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

    public void onPersonaActivated(PersonaManager.Persona persona) {
        persona.getKnowledge().attach(updateEngine);
        persona.getKnowledge().onPersonaActivated();
        getAbilityExecutor().onPersonaActivated();
    }

    public void onPersonaDeactivated(PersonaManager.Persona persona) {
        persona.getKnowledge().onPersonaDeactivated();
        persona.getKnowledge().detach(updateEngine);
        getAbilityExecutor().onPersonaDeactivated();
    }

    @Override
    public void serialize(CompoundNBT nbt) {
        personaManager.serialize(nbt);
        getStats().serialize(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        personaManager.deserialize(nbt);
        getStats().deserialize(nbt);
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
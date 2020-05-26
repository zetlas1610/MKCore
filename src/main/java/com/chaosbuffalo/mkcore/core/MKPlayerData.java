package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncPacket;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncRequestPacket;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashSet;
import java.util.Set;

public class MKPlayerData implements IMKPlayerData {

    private PlayerEntity player;
    private boolean readyForUpdates = false;
    private PlayerAbilityExecutor abilityExecutor;
    private PlayerKnowledge knowledge;
    private PlayerStatsModule stats;
    private UpdateEngine updateEngine;
    private final Set<String> spellTag = new HashSet<>();

    public MKPlayerData() {

    }

    @Override
    public void attach(PlayerEntity newPlayer) {
        player = newPlayer;
        updateEngine = new UpdateEngine(this);
        knowledge = new PlayerKnowledge(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        stats = new PlayerStatsModule(this);
        updateEngine.addPublic(stats);
        updateEngine.addPrivate(knowledge);

        registerAttributes();
        if (isServerSide())
            setupFakeStats();
    }

    void setupFakeStats() {
        AttributeModifier mod = new AttributeModifier("test max mana", 20, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(PlayerAttributes.MAX_MANA).applyModifier(mod);

        AttributeModifier mod2 = new AttributeModifier("test mana regen", 1, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(PlayerAttributes.MANA_REGEN).applyModifier(mod2);

        AttributeModifier mod3 = new AttributeModifier("test cdr", 0.1, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(PlayerAttributes.COOLDOWN).applyModifier(mod3);
    }

    private void registerAttributes() {
        AbstractAttributeMap attributes = player.getAttributes();
        attributes.registerAttribute(PlayerAttributes.MAX_MANA);
        attributes.registerAttribute(PlayerAttributes.MANA_REGEN);
        attributes.registerAttribute(PlayerAttributes.COOLDOWN);
        attributes.registerAttribute(PlayerAttributes.MELEE_CRIT);
        attributes.registerAttribute(PlayerAttributes.MAGIC_ATTACK_DAMAGE);
        attributes.registerAttribute(PlayerAttributes.MELEE_CRITICAL_DAMAGE);
        attributes.registerAttribute(PlayerAttributes.SPELL_CRIT);
        attributes.registerAttribute(PlayerAttributes.SPELL_CRITICAL_DAMAGE);
        attributes.registerAttribute(PlayerAttributes.HEAL_BONUS);
        attributes.registerAttribute(PlayerAttributes.MAGIC_ARMOR);
    }

    public void onJoinWorld() {
        getAbilityExecutor().onJoinWorld();
        if (isServerSide()) {
            MKCore.LOGGER.info("server player joined world!");
        } else {
            MKCore.LOGGER.info("client player joined world!");
            PacketHandler.sendMessageToServer(new PlayerDataSyncRequestPacket());
        }
    }

    @Override
    public PlayerAbilityExecutor getAbilityExecutor() {
        return abilityExecutor;
    }

    @Override
    public PlayerKnowledge getKnowledge() {
        return knowledge;
    }

    @Override
    public PlayerStatsModule getStats() {
        return stats;
    }

    public UpdateEngine getUpdateEngine() {
        return updateEngine;
    }

    @Override
    public void clone(IMKPlayerData previous, boolean death) {
        MKCore.LOGGER.info("onDeath!");

        CompoundNBT tag = new CompoundNBT();
        previous.serialize(tag);
        deserialize(tag);
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    private boolean isServerSide() {
        return player instanceof ServerPlayerEntity;
    }

    @Override
    public void update() {
        getStats().tick();
        getAbilityExecutor().tick();

//        MKCore.LOGGER.info("update {} {}", this.player, mana.get());

        if (!isServerSide()) {
            // client-only handling here
            return;
        }

        syncState();
    }

    private void syncState() {
        if (!readyForUpdates) {
//            MKCore.LOGGER.info("deferring update because client not ready");
            return;
        }

        updateEngine.syncUpdates();
    }

    public void fullSyncTo(ServerPlayerEntity otherPlayer) {
        MKCore.LOGGER.info("Full public sync {} -> {}", player, otherPlayer);
        updateEngine.publicFullSync(otherPlayer);
    }

    public void initialSync() {
        MKCore.LOGGER.info("Sending initial sync for {}", player);
        if (isServerSide()) {
            getStats().sync();
            updateEngine.syncFull();
            readyForUpdates = true;
        }
    }

    @Override
    public void serialize(CompoundNBT nbt) {
//        MKCore.LOGGER.info("serialize({})", mana.get());
        getStats().serialize(nbt);
        getKnowledge().serialize(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        getKnowledge().deserialize(nbt);
        getStats().deserialize(nbt);

//        MKCore.LOGGER.info("deserialize({})", mana.get());
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
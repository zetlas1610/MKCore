package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncPacket;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncRequestPacket;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.List;

public class MKPlayerData implements IMKPlayerData {

    private PlayerEntity player;
    private boolean readyForUpdates = false;
    private PlayerAbilityExecutor abilityExecutor;
    private PlayerKnowledge knowledge;
    private PlayerStatsModule stats;
    private AbilityTracker abilityTracker;
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final CompositeUpdater publicUpdater = new CompositeUpdater();

    public MKPlayerData() {
    }

    @Override
    public void attach(PlayerEntity newPlayer) {
        player = newPlayer;
        knowledge = new PlayerKnowledge(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        stats = new PlayerStatsModule(this);
        abilityTracker = AbilityTracker.getTracker(player);
        publicUpdater.add(stats);
        registerAttributes();

        setupFakeStats();
    }

    void setupFakeStats() {
        AttributeModifier mod = new AttributeModifier("test max mana", 20, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(PlayerAttributes.MAX_MANA).applyModifier(mod);

        AttributeModifier mod2 = new AttributeModifier("test mana regen", 1, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(PlayerAttributes.MANA_REGEN).applyModifier(mod2);

        AttributeModifier mod3 = new AttributeModifier("test cdr", 0.1, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(PlayerAttributes.COOLDOWN).applyModifier(mod3);

        List<ResourceLocation> hotbar = Arrays.asList(
                MKCore.makeRL("ability.ember"),
                MKCore.makeRL("ability.skin_like_wood"),
                MKCore.makeRL("ability.fire_armor"),
                MKCore.makeRL("ability.notorious_dot"),
                MKCore.makeRL("ability.whirlwind_blades"));
        knowledge.setHotBar(hotbar);
    }

    private void registerAttributes() {
        AbstractAttributeMap attributes = player.getAttributes();
        attributes.registerAttribute(PlayerAttributes.MAX_MANA);
        attributes.registerAttribute(PlayerAttributes.MANA_REGEN);
        attributes.registerAttribute(PlayerAttributes.COOLDOWN);
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

    public int getCurrentAbilityCooldown(ResourceLocation abilityId) {
        PlayerAbilityInfo abilityInfo = getKnowledge().getAbilityInfo(abilityId);
        return abilityInfo != null ? abilityTracker.getCooldownTicks(abilityId) : GameConstants.ACTION_BAR_INVALID_COOLDOWN;
    }

    @Override
    public float getCooldownPercent(PlayerAbilityInfo abilityInfo, float partialTicks) {
        return abilityInfo != null ? abilityTracker.getCooldown(abilityInfo.getId(), partialTicks) : 0.0f;
    }

    @Override
    public float getAbilityManaCost(ResourceLocation abilityId) {
        PlayerAbilityInfo abilityInfo = getKnowledge().getAbilityInfo(abilityId);
        if (abilityInfo == null) {
            return 0.0f;
        }
        float manaCost = abilityInfo.getAbility().getManaCost(abilityInfo.getRank());
//        return PlayerFormulas.applyManaCostReduction(this, ); TODO: formulas
        return manaCost;
    }

    public int getAbilityCooldown(PlayerAbility ability) {
        int ticks = ability.getCooldownTicks(getAbilityRank(ability.getAbilityId()));
        ticks = PlayerFormulas.applyCooldownReduction(this, ticks);
        return ticks;
    }

    @Override
    public void setTimer(ResourceLocation id, int cooldown) {
        if (cooldown > 0) {
            abilityTracker.setCooldown(id, cooldown);
        } else {
            abilityTracker.removeCooldown(id);
        }
    }

    @Override
    public int getTimer(ResourceLocation id) {
        return abilityTracker.getCooldownTicks(id);
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
        abilityTracker.tick();
        getAbilityExecutor().tick();
        getStats().tick();
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

        if (isDirty()) {
            PlayerDataSyncPacket packet = getUpdateMessage();
            if (packet != null) {
                MKCore.LOGGER.info("sending dirty update for {}", player);
                PacketHandler.sendToTrackingAndSelf(packet, (ServerPlayerEntity) player);
            }
        }
    }

    public void fullSyncTo(ServerPlayerEntity otherPlayer) {
        MKCore.LOGGER.info("need full sync to {}", otherPlayer);
        PlayerDataSyncPacket packet = getFullSyncMessage();
        PacketHandler.sendMessage(packet, otherPlayer);
    }

    public void initialSync() {
        MKCore.LOGGER.info("initial sync");
        if (isServerSide()) {
            fullSyncTo((ServerPlayerEntity) player);
            abilityTracker.sync();
            readyForUpdates = true;
        }
    }

    private boolean isDirty() {
        return publicUpdater.isDirty();
    }

    private PlayerDataSyncPacket getUpdateMessage() {
        return isDirty() ? new PlayerDataSyncPacket(this, player.getUniqueID(), false) : null;
    }

    private PlayerDataSyncPacket getFullSyncMessage() {
        return new PlayerDataSyncPacket(this, player.getUniqueID(), true);
    }


    public void serializeClientUpdate(CompoundNBT updateTag, boolean fullSync) {
//        MKCore.LOGGER.info("serializeClientUpdate {} {}", mana.get(), fullSync);
        if (fullSync) {
            publicUpdater.serializeFull(updateTag);
        } else {
            publicUpdater.serializeUpdate(updateTag);
        }
    }

    public void deserializeClientUpdate(CompoundNBT updateTag) {
//        MKCore.LOGGER.info("deserializeClientUpdatePre {}", mana.get());
        publicUpdater.deserializeUpdate(updateTag);
//        MKCore.LOGGER.info("deserializeClientUpdatePost - {}", mana.get());
    }

    @Override
    public void serialize(CompoundNBT nbt) {
//        MKCore.LOGGER.info("serialize({})", mana.get());
        getStats().serialize(nbt);
        abilityTracker.serialize(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        getStats().deserialize(nbt);
        abilityTracker.deserialize(nbt);

//        MKCore.LOGGER.info("deserialize({})", mana.get());
    }

    public void printActiveCooldowns() {
        String msg = "All active cooldowns:";

        player.sendMessage(new StringTextComponent(msg));
        abilityTracker.iterateActive((abilityId, current) -> {
            String name = abilityId.toString();
            int max = abilityTracker.getMaxCooldownTicks(abilityId);
            ITextComponent line = new StringTextComponent(String.format("%s: %d / %d", name, current, max));
            player.sendMessage(line);
        });
    }

    public void resetAllCooldowns() {
        abilityTracker.removeAll();
    }
}

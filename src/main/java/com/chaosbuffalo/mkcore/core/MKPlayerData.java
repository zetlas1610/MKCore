package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.PlayerToggleAbility;
import com.chaosbuffalo.mkcore.events.PlayerAbilityEvent;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncPacket;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncRequestPacket;
import com.chaosbuffalo.mkcore.network.PlayerStartCastPacket;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.*;

public class MKPlayerData implements IMKPlayerData {

    private PlayerEntity player;
    private float regenTime;
    private boolean readyForUpdates = false;
    private AbilityTracker abilityTracker;
    private PlayerCastingState currentCast;
    private final Map<ResourceLocation, PlayerToggleAbility> activeToggleMap = new HashMap<>();
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final CompositeUpdater publicUpdater = new CompositeUpdater(mana);

    // TODO: this should be in player knowledge
    private List<ResourceLocation> hotbar = Collections.emptyList();

    public MKPlayerData() {
        regenTime = 0f;
    }

    @Override
    public void attach(PlayerEntity newPlayer) {
        player = newPlayer;
        abilityTracker = AbilityTracker.getTracker(player);
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

        hotbar = Arrays.asList(MKCore.makeRL("ability.ember"), MKCore.makeRL("ability.skin_like_wood"), MKCore.makeRL("ability.fire_armor"), MKCore.makeRL("ability.notorious_dot"));
    }

    private void registerAttributes() {
        AbstractAttributeMap attributes = player.getAttributes();
        attributes.registerAttribute(PlayerAttributes.MAX_MANA);
        attributes.registerAttribute(PlayerAttributes.MANA_REGEN);
        attributes.registerAttribute(PlayerAttributes.COOLDOWN);
    }

    public void onJoinWorld() {
        if (isServerSide()) {
            MKCore.LOGGER.info("server player joined world!");
            rebuildActiveToggleMap();
        } else {
            MKCore.LOGGER.info("client player joined world!");
            PacketHandler.sendMessageToServer(new PlayerDataSyncRequestPacket());
        }
    }

    @Override
    public float getMana() {
        return mana.get();
    }

    @Override
    public void setMana(float value) {
        value = MathHelper.clamp(value, 0, getMaxMana());
        mana.set(value);
    }

    @Override
    public void setMaxMana(float max) {
        player.getAttribute(PlayerAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    @Override
    public int getActionBarSize() {
        // TODO: expandable
        return GameConstants.CLASS_ACTION_BAR_SIZE;
    }

    public void executeHotBarAbility(int slot) {
        MKCore.LOGGER.info("executeHotBarAbility {}", slot);

        ResourceLocation abilityId = getAbilityInSlot(slot);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        PlayerAbilityInfo info = getAbilityInfo(abilityId);
        if (info == null || !info.isCurrentlyKnown())
            return;

        if (getCurrentAbilityCooldown(abilityId) == 0) {

            PlayerAbility ability = info.getAbility();
            if (ability != null &&
                    ability.meetsRequirements(this) &&
                    !MinecraftForge.EVENT_BUS.post(new PlayerAbilityEvent.StartCasting(this, info))) {
                ability.execute(player, this, player.getEntityWorld());
            }
        }
    }

    public ResourceLocation getAbilityInSlot(int slot) {
        if (slot < hotbar.size()) {
            return hotbar.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    @Override
    public int getAbilityRank(ResourceLocation abilityId) {
        return 1;
    }

    @Override
    public boolean isCasting() {
        return currentCast != null;
    }

    @Override
    public int getCastTicks() {
        return currentCast != null ? currentCast.getCastTicks() : 0;
    }

    @Override
    public ResourceLocation getCastingAbility() {
        return currentCast != null ? currentCast.getAbilityId() : MKCoreRegistry.INVALID_ABILITY;
    }

    private void clearCastingAbility() {
        currentCast = null;
    }

    static abstract class PlayerCastingState {
        boolean started = false;
        int castTicks;
        PlayerAbility ability;
        MKPlayerData playerData;

        public PlayerCastingState(MKPlayerData playerData, PlayerAbility ability, int castTicks) {
            this.playerData = playerData;
            this.ability = ability;
            this.castTicks = castTicks;
        }

        public int getCastTicks() {
            return castTicks;
        }

        public ResourceLocation getAbilityId() {
            return ability.getAbilityId();
        }

        public boolean tick() {
            if (castTicks <= 0)
                return false;

            if (!started) {
                begin();
                started = true;
            }

            activeTick();
            castTicks--;
            boolean active = castTicks > 0;
            if (!active) {
                finish();
            }
            return active;
        }

        void begin() {

        }

        abstract void activeTick();

        abstract void finish();
    }

    static class ServerCastingState extends PlayerCastingState {
        PlayerAbilityInfo info;
        CastState abilityCastState;

        public ServerCastingState(MKPlayerData playerData, PlayerAbilityInfo ability, int castTicks) {
            super(playerData, ability.getAbility(), castTicks);
            this.info = ability;
            abilityCastState = ability.getAbility().createCastState(castTicks);
        }

        public CastState getAbilityCastState() {
            return abilityCastState;
        }

        @Override
        void activeTick() {
            ability.continueCast(playerData.player, playerData, playerData.player.getEntityWorld(), castTicks, abilityCastState);
        }

        @Override
        void finish() {
            ability.endCast(playerData.player, playerData, playerData.player.getEntityWorld(), abilityCastState);
            playerData.completeAbility(ability, info);
        }
    }

    static class ClientCastingState extends PlayerCastingState {
        //        MovingSoundCasting sound; TODO: sound
        boolean playing = false;

        public ClientCastingState(MKPlayerData player, PlayerAbility ability, int castTicks) {
            super(player, ability, castTicks);
        }

        @Override
        void begin() {
            SoundEvent event = ability.getCastingSoundEvent();
            if (event != null) {
                // TODO: sound
//                sound = new MovingSoundCasting(playerData.player, event, SoundCategory.PLAYERS, castTicks);
//                Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                playing = true;
            }
        }

        @Override
        void activeTick() {
            ability.continueCastClient(playerData.player, playerData, playerData.player.getEntityWorld(), castTicks);
        }

        @Override
        void finish() {
            // TODO: sound
//            if (playing && sound != null) {
//                Minecraft.getInstance().getSoundHandler().stopSound(sound);
//                playing = false;
//            }
        }
    }

    private CastState startCast(PlayerAbilityInfo abilityInfo, int castTime) {
        MKCore.LOGGER.info("startCast {} {}", abilityInfo.getId(), castTime);
        ServerCastingState serverCastingState = new ServerCastingState(this, abilityInfo, castTime);
        currentCast = serverCastingState;
        if (isServerSide()) {
            PacketHandler.sendToTrackingAndSelf(new PlayerStartCastPacket(abilityInfo.getId(), castTime), (ServerPlayerEntity) player);
        }

        return serverCastingState.getAbilityCastState();
    }

    public void startCastClient(ResourceLocation abilityId, int castTicks) {
        MKCore.LOGGER.info("startCastClient {} {}", abilityId, castTicks);
        PlayerAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            currentCast = new ClientCastingState(this, ability, castTicks);
        } else {
            clearCastingAbility();
        }
    }

    void updateCurrentCast() {
        if (!isCasting())
            return;

        if (!currentCast.tick()) {
            clearCastingAbility();
        }
    }

    @Nullable
    @Override
    public CastState startAbility(PlayerAbility ability) {
        PlayerAbilityInfo info = getAbilityInfo(ability.getAbilityId());
        if (info == null || !info.isCurrentlyKnown() || isCasting()) {
            MKCore.LOGGER.info("startAbility null {} {}", info, isCasting());
            return null;
        }

        float manaCost = getAbilityManaCost(ability.getAbilityId());
        setMana(getMana() - manaCost);

        int castTime = ability.getCastTime(info.getRank());
        if (castTime > 0) {
            return startCast(info, castTime);
        } else {
            completeAbility(ability, info);
        }
        return null;
    }

    private void completeAbility(PlayerAbility ability, PlayerAbilityInfo info) {
        int cooldown = ability.getCooldownTicks(info.getRank());
        cooldown = PlayerFormulas.applyCooldownReduction(this, cooldown);
        setCooldown(info.getId(), cooldown);
//        SoundEvent sound = ability.getSpellCompleteSoundEvent();
//        if (sound != null) {
//            AbilityUtils.playSoundAtServerEntity(player, sound, SoundCategory.PLAYERS);
//        }
        clearCastingAbility();
        MinecraftForge.EVENT_BUS.post(new PlayerAbilityEvent.Completed(this, info));
    }


    @Override
    @Nullable
    public PlayerAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        // TODO: player knowledge
//        PlayerClassInfo info = getActiveClass();
//        return info != null ? info.getAbilityInfo(abilityId) : null;
        PlayerAbility ability = MKCoreRegistry.getAbility(abilityId);
        PlayerAbilityInfo info = new PlayerAbilityInfo(ability);
        info.upgrade();
        return info;
    }


    public int getCurrentAbilityCooldown(ResourceLocation abilityId) {
        PlayerAbilityInfo abilityInfo = getAbilityInfo(abilityId);
        return abilityInfo != null ? abilityTracker.getCooldownTicks(abilityId) : GameConstants.ACTION_BAR_INVALID_COOLDOWN;
    }

    @Override
    public float getCooldownPercent(PlayerAbilityInfo abilityInfo, float partialTicks) {
        return abilityInfo != null ? abilityTracker.getCooldown(abilityInfo.getId(), partialTicks) : 0.0f;
    }

    @Override
    public float getAbilityManaCost(ResourceLocation abilityId) {
        PlayerAbilityInfo abilityInfo = getAbilityInfo(abilityId);
        if (abilityInfo == null) {
            return 0.0f;
        }
        float manaCost = abilityInfo.getAbility().getManaCost(abilityInfo.getRank());
//        return PlayerFormulas.applyManaCostReduction(this, ); TODO: formulas
        return manaCost;
    }

    public int getAbilityCooldown(PlayerAbility ability) {
        int ticks = ability.getCooldownTicks(getAbilityRank(ability.getAbilityId()));
        ticks = PlayerFormulas.applyCooldownReduction(this, ticks); //TODO: formulas
        return ticks;
    }

    private void rebuildActiveToggleMap() {
        for (int i = 0; i < GameConstants.ACTION_BAR_SIZE; i++) {
            ResourceLocation abilityId = getAbilityInSlot(i);
            PlayerAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof PlayerToggleAbility && player != null) {
                PlayerToggleAbility toggle = (PlayerToggleAbility) ability;
                if (player.isPotionActive(toggle.getToggleEffect()))
                    setToggleGroupAbility(toggle.getToggleGroupId(), toggle);
            }
        }
    }

    private void updateToggleAbility(PlayerAbility ability) {
        if (ability instanceof PlayerToggleAbility && player != null) {
            PlayerToggleAbility toggle = (PlayerToggleAbility) ability;
            PlayerAbilityInfo info = getAbilityInfo(ability.getAbilityId());
            if (info != null && info.isCurrentlyKnown()) {
                // If this is a toggle ability we must re-apply the effect to make sure it's working at the proper rank
                if (player.isPotionActive(toggle.getToggleEffect())) {
                    toggle.removeEffect(player, this, player.getEntityWorld());
                    toggle.applyEffect(player, this, player.getEntityWorld());
                }
            } else {
                // Unlearning, remove the effect
                toggle.removeEffect(player, this, player.getEntityWorld());
            }
        }
    }

    public void clearToggleGroupAbility(ResourceLocation groupId) {
        activeToggleMap.remove(groupId);
    }

    public void setToggleGroupAbility(ResourceLocation groupId, PlayerToggleAbility ability) {
        PlayerToggleAbility current = activeToggleMap.get(ability.getToggleGroupId());
        // This can also be called when rebuilding the activeToggleMap after transferring dimensions and in that case
        // ability will be the same as current
        if (current != null && current != ability) {
            current.removeEffect(player, this, player.getEntityWorld());
            setCooldown(current.getAbilityId(), getAbilityCooldown(current));
        }
        activeToggleMap.put(groupId, ability);
    }

    @Override
    public void setCooldown(ResourceLocation id, int ticks) {
        MKCore.LOGGER.info("setCooldown({}, {})", id, ticks);

        if (!id.equals(MKCoreRegistry.INVALID_ABILITY)) {
            setTimer(id, ticks);
        }
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
        updateCurrentCast();
//        MKCore.LOGGER.info("update {} {}", this.player, mana.get());

        if (!isServerSide()) {
            // client-only handling here
            return;
        }

        updateMana();

        syncState();
    }

    private void syncState() {
        if (!readyForUpdates) {
            MKCore.LOGGER.info("deferring update because client not ready");
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

    private void updateMana() {
        if (this.getManaRegenRate() <= 0.0f) {
            return;
        }

        float max = getMaxMana();
        if (getMana() > max)
            setMana(max);

        regenTime += 1. / 20.;
        // if getManaRegenRate == 1, this is 1 mana per 3 seconds
        float i_regen = 3.0f / getManaRegenRate();
        if (regenTime >= i_regen) {
            if (getMana() < max) {
                addMana(1);
            }
            regenTime -= i_regen;
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
        MKCore.LOGGER.info("serializeClientUpdate {} {}", mana.get(), fullSync);
        if (fullSync) {
            publicUpdater.serializeFull(updateTag);
        } else {
            publicUpdater.serializeUpdate(updateTag);
        }
    }

    public void deserializeClientUpdate(CompoundNBT updateTag) {
        MKCore.LOGGER.info("deserializeClientUpdatePre {}", mana.get());
        publicUpdater.deserializeUpdate(updateTag);
        MKCore.LOGGER.info("deserializeClientUpdatePost - {}", mana.get());
    }

    public void serializeActiveState(CompoundNBT nbt) {
        nbt.putFloat("mana", mana.get());
    }

    public void deserializeActiveState(CompoundNBT nbt) {
        // TODO: activate persona here
        if (nbt.contains("mana")) {
            setMana(nbt.getFloat("mana"));
        }
    }

    @Override
    public void serialize(CompoundNBT nbt) {
        MKCore.LOGGER.info("serialize({})", mana.get());
        serializeActiveState(nbt);
        abilityTracker.serialize(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        deserializeActiveState(nbt);
        abilityTracker.deserialize(nbt);

        MKCore.LOGGER.info("deserialize({})", mana.get());
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

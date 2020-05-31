package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerStartCastPacket;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import java.util.HashMap;
import java.util.Map;

public class AbilityExecutor {
    protected final IMKEntityData entityData;
    private EntityCastingState currentCast;
    private final Map<ResourceLocation, MKToggleAbility> activeToggleMap = new HashMap<>();

    public AbilityExecutor(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    protected boolean abilityExecutionCheck(MKAbility ability, MKAbilityInfo info) {
        return ability.meetsRequirements(entityData);
    }

    public void executeAbility(ResourceLocation abilityId) {
        MKAbilityInfo info = entityData.getKnowledge().getAbilityInfo(abilityId);
        if (info == null || !info.isCurrentlyKnown())
            return;

        MKAbility ability = info.getAbility();
        if (abilityExecutionCheck(ability, info)) {
            ability.execute(entityData.getEntity(), entityData);
        }
    }

    public boolean canActivateAbility(MKAbility ability) {
        if (isCasting())
            return false;

        if (entityData.getStats().getCurrentAbilityCooldown(ability.getAbilityId()) > 0)
            return false;
        return true;
    }

    public void tick() {
        updateCurrentCast();
    }

    public void onJoinWorld() {
    }

    public void setCooldown(ResourceLocation id, int ticks) {
        MKCore.LOGGER.debug("setCooldown({}, {})", id, ticks);

        if (!id.equals(MKCoreRegistry.INVALID_ABILITY)) {
            entityData.getStats().setTimer(id, ticks);
        }
    }

    public boolean isCasting() {
        return currentCast != null;
    }

    public int getCastTicks() {
        return currentCast != null ? currentCast.getCastTicks() : 0;
    }

    public ResourceLocation getCastingAbility() {
        return currentCast != null ? currentCast.getAbilityId() : MKCoreRegistry.INVALID_ABILITY;
    }

    private void clearCastingAbility() {
        currentCast = null;
    }

    private CastState startCast(MKAbilityInfo abilityInfo, int castTime) {
        MKCore.LOGGER.debug("startCast {} {}", abilityInfo.getId(), castTime);
        ServerCastingState serverCastingState = createServerCastingState(abilityInfo, castTime);
        currentCast = serverCastingState;

        PacketHandler.sendToTrackingMaybeSelf(new PlayerStartCastPacket(abilityInfo.getId(), castTime), entityData.getEntity());

        return serverCastingState.getAbilityCastState();
    }

    public void startCastClient(ResourceLocation abilityId, int castTicks) {
        MKCore.LOGGER.debug("startCastClient {} {}", abilityId, castTicks);
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            currentCast = createClientCastingState(ability, castTicks);
        } else {
            clearCastingAbility();
        }
    }

    private void updateCurrentCast() {
        if (!isCasting())
            return;

        if (!currentCast.tick()) {
            clearCastingAbility();
        }
    }

    public CastState startAbility(MKAbility ability) {
        if (isCasting()) {
            MKCore.LOGGER.warn("startAbility({}) failed - {} currently casting", entityData::getEntity, ability::getAbilityId);
            return null;
        }

        // TODO: decide if NPCs will need MKAbilityInfo. If not, this can be refactored and moved into PlayerAbilityExecutor
        MKAbilityInfo info = entityData.getKnowledge().getAbilityInfo(ability.getAbilityId());
        if (info == null || !info.isCurrentlyKnown()) {
            MKCore.LOGGER.warn("startAbility({}) failed - {} does not know", entityData::getEntity, ability::getAbilityId);
            return null;
        }

        consumeResource(ability);

        int castTime = ability.getCastTime();
        CastState state = startCast(info, castTime);
        if (castTime > 0) {
            return state;
        } else {
            completeAbility(ability, info, state);
        }
        return null;
    }

    protected void completeAbility(MKAbility ability, MKAbilityInfo info, CastState castState) {
        // Finish the cast
        ability.endCast(entityData.getEntity(), entityData, castState);

        int cooldown = MKCombatFormulas.applyCooldownReduction(entityData, ability.getCooldownTicks());
        setCooldown(info.getId(), cooldown);
        SoundEvent sound = ability.getSpellCompleteSoundEvent();
        if (sound != null) {
            SoundUtils.playSoundAtEntity(entityData.getEntity(), sound, SoundCategory.PLAYERS);
        }
        clearCastingAbility();
    }

    public void onAbilityUnlearned(MKAbility ability) {
        updateToggleAbility(ability);
    }

    protected ServerCastingState createServerCastingState(MKAbilityInfo abilityInfo, int castTime) {
        return new ServerCastingState(this, abilityInfo, castTime);
    }

    protected ClientCastingState createClientCastingState(MKAbility ability, int castTicks) {
        return new ClientCastingState(this, ability, castTicks);
    }

    protected void consumeResource(MKAbility ability) {

    }

    static abstract class EntityCastingState {
        boolean started = false;
        int castTicks;
        MKAbility ability;
        AbilityExecutor executor;

        public EntityCastingState(AbilityExecutor executor, MKAbility ability, int castTicks) {
            this.executor = executor;
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

    static class ServerCastingState extends EntityCastingState {
        protected MKAbilityInfo info;
        protected CastState abilityCastState;

        public ServerCastingState(AbilityExecutor executor, MKAbilityInfo abilityInfo, int castTicks) {
            super(executor, abilityInfo.getAbility(), castTicks);
            this.info = abilityInfo;
            abilityCastState = abilityInfo.getAbility().createCastState(castTicks);
        }

        public CastState getAbilityCastState() {
            return abilityCastState;
        }

        @Override
        void activeTick() {
            ability.continueCast(executor.entityData.getEntity(), executor.entityData, castTicks, abilityCastState);
        }

        @Override
        void finish() {
            executor.completeAbility(ability, info, abilityCastState);
        }
    }

    static class ClientCastingState extends EntityCastingState {
        protected MovingSoundCasting sound;
        protected boolean playing = false;

        public ClientCastingState(AbilityExecutor executor, MKAbility ability, int castTicks) {
            super(executor, ability, castTicks);
        }

        @Override
        void begin() {
            SoundEvent event = ability.getCastingSoundEvent();
            if (event != null) {
                sound = new MovingSoundCasting(executor.entityData.getEntity(), event, SoundCategory.PLAYERS, castTicks);
                Minecraft.getInstance().getSoundHandler().play(sound);
                playing = true;
            }
        }

        @Override
        void activeTick() {
            ability.continueCastClient(executor.entityData.getEntity(), executor.entityData, castTicks);
        }

        @Override
        void finish() {
            if (playing && sound != null) {
                Minecraft.getInstance().getSoundHandler().stop(sound);
                playing = false;
            }
        }
    }

    private void updateToggleAbility(MKAbility ability) {
        if (!(ability instanceof MKToggleAbility)) {
            return;
        }
        MKToggleAbility toggle = (MKToggleAbility) ability;

        LivingEntity entity = entityData.getEntity();
        MKAbilityInfo info = entityData.getKnowledge().getAbilityInfo(ability.getAbilityId());
        if (info != null && info.isCurrentlyKnown()) {
            // If this is a toggle ability we must re-apply the effect to make sure it's working at the proper rank
            if (entity.isPotionActive(toggle.getToggleEffect())) {
                toggle.removeEffect(entity, entityData);
                toggle.applyEffect(entity, entityData);
            }
        } else {
            // Unlearning, remove the effect
            toggle.removeEffect(entity, entityData);
        }
    }

    public void clearToggleGroupAbility(ResourceLocation groupId) {
        activeToggleMap.remove(groupId);
    }

    public void setToggleGroupAbility(ResourceLocation groupId, MKToggleAbility ability) {
        MKToggleAbility current = activeToggleMap.get(ability.getToggleGroupId());
        // This can also be called when rebuilding the activeToggleMap after transferring dimensions and in that case
        // ability will be the same as current
        if (current != null && current != ability) {
            current.removeEffect(entityData.getEntity(), entityData);
            setCooldown(current.getAbilityId(), entityData.getStats().getAbilityCooldown(current));
        }
        activeToggleMap.put(groupId, ability);
    }
}

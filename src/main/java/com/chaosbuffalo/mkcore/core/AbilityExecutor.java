package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.network.EntityCastPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AbilityExecutor {
    protected final IMKEntityData entityData;
    private EntityCastingState currentCast;
    private final Map<ResourceLocation, MKToggleAbility> activeToggleMap = new HashMap<>();
    private Consumer<MKAbility> startCastCallback;
    private Consumer<MKAbility> completeAbilityCallback;

    public AbilityExecutor(IMKEntityData entityData) {
        this.entityData = entityData;
        startCastCallback = null;
        completeAbilityCallback = null;
    }

    public void setCompleteAbilityCallback(Consumer<MKAbility> completeAbilityCallback) {
        this.completeAbilityCallback = completeAbilityCallback;
    }

    public void setStartCastCallback(Consumer<MKAbility> startCastCallback) {
        this.startCastCallback = startCastCallback;
    }

    protected boolean abilityExecutionCheck(MKAbility ability, MKAbilityInfo info) {
        return ability.meetsRequirements(entityData);
    }

    public void executeAbility(ResourceLocation abilityId) {
        executeAbilityWithContext(abilityId, null);
    }

    public void executeAbilityWithContext(ResourceLocation abilityId, AbilityContext context) {
        MKAbilityInfo info = entityData.getKnowledge().getKnownAbilityInfo(abilityId);
        if (info == null)
            return;

        MKAbility ability = info.getAbility();
        if (abilityExecutionCheck(ability, info)) {
            if (context == null) {
                context = ability.getTargetSelector().createContext(entityData, ability);
            } else {
                boolean validContext = ability.getTargetSelector().validateContext(entityData, context);
                if (!validContext) {
                    MKCore.LOGGER.warn("Entity {} tried to execute ability {} with a context that failed validation!", entityData.getEntity(), abilityId);
                    return;
                }
            }
            if (context != null) {
                ability.executeWithContext(entityData, context);
            } else {
                MKCore.LOGGER.warn("Entity {} tried to execute ability {} with a null context!", entityData.getEntity(), abilityId);
            }
        }
    }

    public boolean canActivateAbility(MKAbility ability) {
        if (isCasting())
            return false;

        if (getCurrentAbilityCooldown(ability.getAbilityId()) > 0)
            return false;
        return true;
    }

    public void tick() {
        updateCurrentCast();
    }

    public void onJoinWorld() {
        if (!entityData.getEntity().getEntityWorld().isRemote) {
            checkPassiveEffects();
        }
    }

    public void setCooldown(ResourceLocation id, int ticks) {
        MKCore.LOGGER.debug("setCooldown({}, {})", id, ticks);

        if (!id.equals(MKCoreRegistry.INVALID_ABILITY)) {
            entityData.getStats().setTimer(id, ticks);
        }
    }

    public int getCurrentAbilityCooldown(ResourceLocation abilityId) {
        return entityData.getStats().getTimer(abilityId);
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

    private void startCast(AbilityContext context, MKAbilityInfo abilityInfo, int castTime) {
        MKCore.LOGGER.debug("startCast {} {}", abilityInfo.getId(), castTime);
        currentCast = createServerCastingState(context, abilityInfo, castTime);
        if (startCastCallback != null) {
            startCastCallback.accept(abilityInfo.getAbility());
        }
        PacketHandler.sendToTrackingMaybeSelf(EntityCastPacket.start(entityData, abilityInfo.getId(), castTime), entityData.getEntity());
    }

    public void startCastClient(ResourceLocation abilityId, int castTicks) {
        MKCore.LOGGER.debug("startCastClient {} {}", abilityId, castTicks);
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            currentCast = createClientCastingState(ability, castTicks);
            if (startCastCallback != null){
                startCastCallback.accept(ability);
            }
        } else {
            clearCastingAbility();
        }
    }

    public void interruptCast() {
        if (!isCasting())
            return;

        currentCast.interrupt();
        clearCastingAbility();
    }

    protected void onAbilityInterrupted(MKAbility ability, int ticks) {
        MKCore.LOGGER.info("onAbilityInterrupted {} {}", ability, ticks);
    }

    private void updateCurrentCast() {
        if (!isCasting())
            return;

        if (!currentCast.tick()) {
            clearCastingAbility();
        }
    }

    public boolean startAbility(AbilityContext context, MKAbility ability) {
        if (isCasting()) {
            MKCore.LOGGER.warn("startAbility({}) failed - {} currently casting", entityData::getEntity, ability::getAbilityId);
            return false;
        }

        MKAbilityInfo info = entityData.getKnowledge().getKnownAbilityInfo(ability.getAbilityId());
        if (info == null) {
            MKCore.LOGGER.warn("startAbility({}) failed - {} does not know", entityData::getEntity, ability::getAbilityId);
            return false;
        }

        if (!ability.isExecutableContext(context)) {
            MKCore.LOGGER.debug("Entity {} tried to execute ability {} with missing memories!", entityData.getEntity(), ability.getAbilityId());
            return false;
        }

        int castTime = entityData.getStats().getAbilityCastTime(ability);
        startCast(context, info, castTime);
        if (castTime > 0) {
            return true;
        } else {
            completeAbility(ability, info, context);
        }
        return true;
    }

    protected void completeAbility(MKAbility ability, MKAbilityInfo info, AbilityContext context) {
        // Finish the cast
        consumeResource(ability);
        ability.endCast(entityData.getEntity(), entityData, context);
        if (completeAbilityCallback != null){
            completeAbilityCallback.accept(ability);
        }
        int cooldown = entityData.getStats().getAbilityCooldown(ability);
        setCooldown(ability.getAbilityId(), cooldown);
        SoundEvent sound = ability.getSpellCompleteSoundEvent();
        if (sound != null) {
            SoundUtils.playSoundAtEntity(entityData.getEntity(), sound);
        }
        clearCastingAbility();
    }

    public void onAbilityUnlearned(MKAbility ability) {
        updateToggleAbility(ability);
    }

    public ServerCastingState createServerCastingState(AbilityContext context, MKAbilityInfo abilityInfo, int castTime) {
        return new ServerCastingState(context, this, abilityInfo, castTime);
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

        void interrupt() {
            executor.onAbilityInterrupted(ability, castTicks);
        }
    }

    static class ServerCastingState extends EntityCastingState {
        protected MKAbilityInfo info;
        protected AbilityContext abilityContext;

        public ServerCastingState(AbilityContext context, AbilityExecutor executor, MKAbilityInfo abilityInfo, int castTicks) {
            super(executor, abilityInfo.getAbility(), castTicks);
            this.info = abilityInfo;
            abilityContext = context;
        }

        public AbilityContext getAbilityContext() {
            return abilityContext;
        }

        @Override
        void activeTick() {
            ability.continueCast(executor.entityData.getEntity(), executor.entityData, castTicks, abilityContext);
        }

        @Override
        void finish() {
            executor.completeAbility(ability, info, abilityContext);
        }

        @Override
        void interrupt() {
            MKCore.LOGGER.info("server interrupt");
            super.interrupt();
            PacketHandler.sendToTrackingMaybeSelf(EntityCastPacket.interrupt(executor.entityData), executor.entityData.getEntity());
        }
    }

    static class ClientCastingState extends EntityCastingState {
        protected MovingSoundCasting sound;
        protected boolean playing = false;

        public ClientCastingState(AbilityExecutor executor, MKAbility ability, int castTicks) {
            super(executor, ability, castTicks);
        }

        private void stopSound() {
            if (playing && sound != null) {
                Minecraft.getInstance().getSoundHandler().stop(sound);
                playing = false;
            }
        }

        @Override
        void begin() {
            SoundEvent event = ability.getCastingSoundEvent();
            if (event != null) {
                sound = new MovingSoundCasting(executor.entityData.getEntity(), event,
                        executor.entityData.getEntity().getSoundCategory(), castTicks);
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
            stopSound();
            if (executor.completeAbilityCallback != null){
                executor.completeAbilityCallback.accept(ability);
            }
        }

        @Override
        public void interrupt() {
            MKCore.LOGGER.info("client interrupt");
            super.interrupt();
            stopSound();
        }
    }

    private void updateToggleAbility(MKAbility ability) {
        if (!(ability instanceof MKToggleAbility)) {
            return;
        }
        MKToggleAbility toggle = (MKToggleAbility) ability;

        LivingEntity entity = entityData.getEntity();
        MKAbilityInfo info = entityData.getKnowledge().getKnownAbilityInfo(ability.getAbilityId());
        if (info != null) {
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

    protected void checkPassiveEffects() {
        LivingEntity entity = entityData.getEntity();
        entity.getActivePotionMap().forEach((p, e) -> {
            if (p instanceof PassiveEffect) {
                PassiveEffect sp = (PassiveEffect) p;
                if (sp.canPersistAcrossSessions())
                    return;

                MKCore.LOGGER.debug("AbilityExecutor.checkPassiveEffects {} {}", entity, sp.getName());

                SpellCast cast = sp.createReapplicationCast(entity);
                if (cast != null) {
                    // Call onPotionAdd to re-apply any non-attribute bonuses (such as granting flying)
                    sp.onPotionAdd(cast, entity, entity.getAttributes(), e.getAmplifier());
                    MKCore.LOGGER.debug("AbilityExecutor.checkPassiveEffects {} {} onPotionAdd", entity, sp.getName());
                }
            }
        });
    }
}

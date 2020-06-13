package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.events.ServerSideLeftClickEmpty;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.CritMessagePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpellTriggers {


    public static boolean isMKDamage(DamageSource source) {
        return source instanceof MKDamageSource;
    }

    public static boolean isMinecraftPhysicalDamage(DamageSource source) {
        return (!source.isFireDamage() && !source.isExplosion() && !source.isMagicDamage() &&
                source.getDamageType().equals("player"));
    }

    public static boolean isProjectileDamage(DamageSource source) {
        return source.isProjectile();
    }

    private static boolean startTrigger(Entity source, String tag) {
        if (source instanceof PlayerEntity) {
//            Log.info("startTrigger - %s", tag);
            return source.getCapability(Capabilities.PLAYER_CAPABILITY).map(cap -> {
                if (cap.hasSpellTag(tag)) {
//                Log.info("startTrigger - BLOCKING %s", tag);
                    return false;
                }
                cap.addSpellTag(tag);
                return true;
            }).orElse(true);

        }
        return true;
    }

    private static void endTrigger(Entity source, String tag) {
        if (source instanceof PlayerEntity) {
//            Log.info("endTrigger - %s", tag);
            source.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> cap.removeSpellTag(tag));
        }
    }

    public static class FALL {
        private static final String TAG = FALL.class.getName();
        private static final List<FallTrigger> fallTriggers = new ArrayList<>();

        @FunctionalInterface
        public interface FallTrigger {
            void apply(LivingHurtEvent event, DamageSource source, LivingEntity entity);
        }

        public static void register(FallTrigger trigger) {
            fallTriggers.add(trigger);
        }

        public static void onLivingFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
            if (!startTrigger(entity, TAG))
                return;
            fallTriggers.forEach(f -> f.apply(event, source, entity));
            endTrigger(entity, TAG);
        }
    }

    public static class PLAYER_HURT_ENTITY {

        @FunctionalInterface
        public interface PlayerHurtEntityTrigger {
            void apply(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                       ServerPlayerEntity playerSource, IMKEntityData sourceData);
        }

        private static final String MELEE_TAG = "PLAYER_HURT_ENTITY.melee";
        private static final String MAGIC_TAG = "PLAYER_HURT_ENTITY.magic";
        private static final String POST_TAG = "PLAYER_HURT_ENTITY.post";
        private static final List<PlayerHurtEntityTrigger> playerHurtEntityMeleeTriggers = new ArrayList<>();
        private static final List<PlayerHurtEntityTrigger> playerHurtEntityMagicTriggers = new ArrayList<>();
        private static final List<PlayerHurtEntityTrigger> playerHurtEntityPostTriggers = new ArrayList<>();

        public static void registerMelee(PlayerHurtEntityTrigger trigger) {
            playerHurtEntityMeleeTriggers.add(trigger);
        }

        public static void registerMagic(PlayerHurtEntityTrigger trigger) {
            playerHurtEntityMagicTriggers.add(trigger);
        }

        public static void registerPostHandler(PlayerHurtEntityTrigger trigger) {
            playerHurtEntityPostTriggers.add(trigger);
        }

        public static void onPlayerHurtEntity(LivingHurtEvent event, DamageSource source,
                                              LivingEntity livingTarget, ServerPlayerEntity playerSource,
                                              IMKEntityData sourceData) {
            if (isMKDamage(source)) {
                MKDamageSource mkSource = (MKDamageSource) source;
                if (mkSource.isMeleeDamage()) {
                    handleMKMelee(event, mkSource, livingTarget, playerSource, sourceData);
                } else {
                    handleMKAbility(event, mkSource, livingTarget, playerSource, sourceData);
                }
            }

            // If this is a weapon swing
            if (isMinecraftPhysicalDamage(source)) {
                handleVanillaMelee(event, source, livingTarget, playerSource, sourceData);
            }

            if (isProjectileDamage(source)) {
                handleProjectile(event, source, livingTarget, playerSource, sourceData);
            }

            if (!startTrigger(playerSource, POST_TAG))
                return;
            playerHurtEntityPostTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
            endTrigger(playerSource, POST_TAG);
        }

        private static void handleMKAbility(LivingHurtEvent event, MKDamageSource source, LivingEntity livingTarget,
                                            ServerPlayerEntity playerSource,
                                            IMKEntityData sourceData) {
            calculateAbilityDamage(event, livingTarget, playerSource, sourceData, source,
                    MAGIC_TAG, playerHurtEntityMagicTriggers);
        }

        private static void calculateAbilityDamage(LivingHurtEvent event, LivingEntity livingTarget,
                                                   ServerPlayerEntity playerSource, IMKEntityData sourceData,
                                                   MKDamageSource source, String typeTag,
                                                   List<PlayerHurtEntityTrigger> playerHurtTriggers) {
            event.setAmount(source.getMKDamageType().applyDamage(playerSource, livingTarget, event.getAmount(),
                    source.getModifierScaling()));
            if (source.getMKDamageType().rollCrit(playerSource, livingTarget)) {
                float newDamage = source.getMKDamageType().applyCritDamage(playerSource, livingTarget, event.getAmount());
                event.setAmount(newDamage);
                MKAbility ability = MKCoreRegistry.getAbility(source.getAbilityId());
                ResourceLocation abilityName;
                if (ability != null) {
                    abilityName = ability.getRegistryName();
                } else {
                    abilityName = MKCoreRegistry.INVALID_ABILITY;
                }
                sendCritPacket(livingTarget, playerSource,
                        new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage,
                                abilityName, source.getMKDamageType()));
            }

            if (!startTrigger(playerSource, typeTag))
                return;
            playerHurtTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
            endTrigger(playerSource, typeTag);
        }

        private static void handleProjectile(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                             ServerPlayerEntity playerSource, IMKEntityData sourceData) {
            if (source.getImmediateSource() != null &&
                    MKCombatFormulas.checkCrit(playerSource, MKCombatFormulas.getRangedCritChanceForEntity(sourceData,
                            playerSource, source.getImmediateSource()))) {
                float damageMultiplier = EntityUtils.ENTITY_CRIT.getDamage(source.getImmediateSource());
                if (livingTarget.isGlowing()) {
                    damageMultiplier += 1.0f;
                }
                float newDamage = event.getAmount() * damageMultiplier;
                event.setAmount(newDamage);
                sendCritPacket(livingTarget, playerSource,
                        new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage,
                                source.getImmediateSource().getEntityId()));
            }
        }

        private static void handleMKMelee(LivingHurtEvent event, MKDamageSource source, LivingEntity livingTarget,
                                          ServerPlayerEntity playerSource, IMKEntityData sourceData) {

            calculateAbilityDamage(event, livingTarget, playerSource, sourceData, source,
                    MELEE_TAG, playerHurtEntityMeleeTriggers);
        }

        private static void handleVanillaMelee(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                               ServerPlayerEntity playerSource, IMKEntityData sourceData) {
            ItemStack mainHand = playerSource.getHeldItemMainhand();
            float critChance = MKCombatFormulas.getCritChanceForItem(mainHand);
            if (sourceData instanceof MKPlayerData) {
                MKPlayerData playerData = (MKPlayerData) sourceData;
                if (MKCombatFormulas.checkCrit(playerSource,
                        critChance + playerData.getStats().getMeleeCritChance())) {
                    float critMultiplier = ItemUtils.getCritDamageForItem(mainHand);
                    critMultiplier += playerData.getStats().getMeleeCritDamage();
                    float newDamage = event.getAmount() * critMultiplier;
                    event.setAmount(newDamage);
                    sendCritPacket(livingTarget, playerSource,
                            new CritMessagePacket(livingTarget.getEntityId(), playerSource.getUniqueID(), newDamage,
                                    CritMessagePacket.CritType.MELEE_CRIT));
                }
            }


            if (!startTrigger(playerSource, MELEE_TAG))
                return;
            playerHurtEntityMeleeTriggers.forEach(f -> f.apply(event, source, livingTarget, playerSource, sourceData));
            endTrigger(playerSource, MELEE_TAG);
        }
    }

    public static class ENTITY_HURT_PLAYER {
        @FunctionalInterface
        public interface EntityHurtPlayerTrigger {
            void apply(LivingHurtEvent event, DamageSource source, PlayerEntity livingTarget,
                       MKPlayerData targetData);
        }

        private static final String TAG = ENTITY_HURT_PLAYER.class.getName();
        private static final List<EntityHurtPlayerTrigger> entityHurtPlayerPreTriggers = new ArrayList<>();
        private static final List<EntityHurtPlayerTrigger> entityHurtPlayerPostTriggers = new ArrayList<>();

        public static void registerPreScale(EntityHurtPlayerTrigger trigger) {
            entityHurtPlayerPreTriggers.add(trigger);
        }

        public static void registerPostScale(EntityHurtPlayerTrigger trigger) {
            entityHurtPlayerPostTriggers.add(trigger);
        }

        public static void onEntityHurtPlayer(LivingHurtEvent event, DamageSource source, PlayerEntity livingTarget,
                                              MKPlayerData targetData) {
            if (!startTrigger(livingTarget, TAG))
                return;
            entityHurtPlayerPreTriggers.forEach(f -> f.apply(event, source, livingTarget, targetData));

            if (isMKDamage(source)) {
                MKDamageSource mkDamageSource = (MKDamageSource) source;
                if (mkDamageSource.isUnblockable()) {
                    event.setAmount(mkDamageSource.getMKDamageType().applyResistance(livingTarget, event.getAmount()));
                }
            }

            entityHurtPlayerPostTriggers.forEach(f -> f.apply(event, source, livingTarget, targetData));
            endTrigger(livingTarget, TAG);
        }
    }

    private static void sendCritPacket(LivingEntity livingTarget, ServerPlayerEntity playerSource,
                                       CritMessagePacket packet) {
        PacketHandler.sendToTrackingAndSelf(packet, playerSource);
        Vec3d lookVec = livingTarget.getLookVec();
        PacketHandler.sendToTrackingMaybeSelf(
                new ParticleEffectSpawnPacket(
                        ParticleTypes.ENCHANTED_HIT,
                        ParticleEffects.SPHERE_MOTION, 12, 4,
                        livingTarget.getPosX(), livingTarget.getPosY() + 1.0f,
                        livingTarget.getPosZ(), .5f, .5f, .5f, 0.2,
                        lookVec),
                livingTarget);
    }

    static <T> void selectiveTrigger(LivingEntity entity, Map<SpellPotionBase, T> triggers, BiConsumer<T, EffectInstance> consumer) {
        for (EffectInstance effectInstance : entity.getActivePotionEffects()) {
            if (effectInstance.getPotion() instanceof SpellPotionBase) {
                SpellPotionBase effect = (SpellPotionBase) effectInstance.getPotion();
                T trigger = triggers.get(effect);
                if (trigger != null) {
                    consumer.accept(trigger, effectInstance);
                }
            }
        }
    }

    public static class ATTACK_ENTITY {

        @FunctionalInterface
        public interface AttackEntityTrigger {
            void apply(LivingEntity player, Entity target, EffectInstance effect);
        }

        private static final String TAG = ATTACK_ENTITY.class.getName();
        private static final Map<SpellEffectBase, AttackEntityTrigger> attackEntityTriggers = new HashMap<>();

        public static void register(SpellEffectBase potion, AttackEntityTrigger trigger) {
            attackEntityTriggers.put(potion, trigger);
        }

        public static void onAttackEntity(LivingEntity attacker, Entity target) {
            if (!startTrigger(attacker, TAG))
                return;

            selectiveTrigger(attacker, attackEntityTriggers, (trigger, instance) -> trigger.apply(attacker, target, instance));
            endTrigger(attacker, TAG);
        }
    }

    public static class PLAYER_ATTACK_ENTITY {
        @FunctionalInterface
        public interface PlayerAttackEntityTrigger {
            void apply(LivingEntity player, Entity target, EffectInstance effect);
        }

        private static final String TAG = PLAYER_ATTACK_ENTITY.class.getName();
        private static final Map<SpellEffectBase, PlayerAttackEntityTrigger> attackEntityTriggers = new HashMap<>();

        public static void register(SpellEffectBase potion, PlayerAttackEntityTrigger trigger) {
            attackEntityTriggers.put(potion, trigger);
        }

        public static void onAttackEntity(LivingEntity attacker, Entity target) {
            if (!startTrigger(attacker, TAG))
                return;

            selectiveTrigger(attacker, attackEntityTriggers, (trigger, instance) -> trigger.apply(attacker, target, instance));
            endTrigger(attacker, TAG);
        }
    }

    public static class EMPTY_LEFT_CLICK {

        @FunctionalInterface
        public interface EmptyLeftClickTrigger {
            void apply(ServerSideLeftClickEmpty event, PlayerEntity player, EffectInstance effect);
        }

        private static final String TAG = EMPTY_LEFT_CLICK.class.getName();
        private static final Map<SpellEffectBase, EmptyLeftClickTrigger> emptyLeftClickTriggers = new HashMap<>();

        public static void register(SpellEffectBase potion, EmptyLeftClickTrigger trigger) {
            emptyLeftClickTriggers.put(potion, trigger);
        }

        public static void onEmptyLeftClick(PlayerEntity player, ServerSideLeftClickEmpty event) {
            if (!startTrigger(player, TAG))
                return;

            selectiveTrigger(player, emptyLeftClickTriggers, (trigger, instance) -> trigger.apply(event, player, instance));
            endTrigger(player, TAG);
        }
    }

    public static class PLAYER_KILL_ENTITY {
        private static final String TAG = PLAYER_KILL_ENTITY.class.getName();
        private static final Map<SpellEffectBase, PlayerKillEntityTrigger> killTriggers = new HashMap<>();

        @FunctionalInterface
        public interface PlayerKillEntityTrigger {
            void apply(LivingDeathEvent event, DamageSource source, PlayerEntity player);
        }

        public static void register(SpellEffectBase potion, PlayerKillEntityTrigger trigger) {
            killTriggers.put(potion, trigger);
        }

        public static void onEntityDeath(LivingDeathEvent event, DamageSource source, PlayerEntity entity) {
            if (!startTrigger(entity, TAG))
                return;

            selectiveTrigger(entity, killTriggers, (trigger, instance) -> trigger.apply(event, source, entity));
            endTrigger(entity, TAG);
        }
    }

    public static class PLAYER_DEATH {
        @FunctionalInterface
        public interface PlayerKillEntityTrigger {
            void apply(LivingDeathEvent event, DamageSource source, PlayerEntity player);
        }

        private static final String TAG = PLAYER_DEATH.class.getName();
        private static final Map<SpellEffectBase, PlayerKillEntityTrigger> killTriggers = new HashMap<>();

        public static void register(SpellEffectBase potion, PlayerKillEntityTrigger trigger) {
            killTriggers.put(potion, trigger);
        }

        public static void onEntityDeath(LivingDeathEvent event, DamageSource source, PlayerEntity entity) {
            if (!startTrigger(entity, TAG))
                return;

            selectiveTrigger(entity, killTriggers, (trigger, instance) -> trigger.apply(event, source, entity));
            endTrigger(entity, TAG);
        }
    }

    public static class PLAYER_EQUIPMENT_CHANGE {
        private static final String TAG = PLAYER_EQUIPMENT_CHANGE.class.getName();
        private static final Map<SpellEffectBase, PlayerEquipmentChangeTrigger> triggers = new HashMap<>();

        @FunctionalInterface
        public interface PlayerEquipmentChangeTrigger {
            void apply(LivingEquipmentChangeEvent event, IMKEntityData data, PlayerEntity player);
        }

        public static void register(SpellEffectBase potion, PlayerEquipmentChangeTrigger trigger) {
            triggers.put(potion, trigger);
        }

        public static void onEquipmentChange(LivingEquipmentChangeEvent event, IMKEntityData data, PlayerEntity player) {
            if (!startTrigger(player, TAG))
                return;

            selectiveTrigger(player, triggers, (trigger, instance) -> trigger.apply(event, data, player));
            endTrigger(player, TAG);
        }
    }
}
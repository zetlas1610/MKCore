package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.AbilityUseCondition;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.StandardUseCondition;
import com.chaosbuffalo.mkcore.abilities.attributes.IAbilityAttribute;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class MKAbility extends ForgeRegistryEntry<MKAbility> {

    public enum AbilityType {
        PooledActive(AbilitySlot.Basic, true, true),
        PooledPassive(AbilitySlot.Passive, false, true),
        PooledUltimate(AbilitySlot.Ultimate, true, true),
        Active(AbilitySlot.Basic, true, false),
        Ultimate(AbilitySlot.Ultimate, true, false),
        Passive(AbilitySlot.Passive, false, false);

        final AbilitySlot slotType;
        final boolean canSlot;
        final boolean usesPool;

        AbilityType(AbilitySlot slotType, boolean canSlot, boolean usesPool) {
            this.slotType = slotType;
            this.canSlot = canSlot;
            this.usesPool = usesPool;
        }

        public AbilitySlot getSlotType() {
            return slotType;
        }

        public boolean canPlaceOnActionBar() {
            return canSlot;
        }

        public boolean isPoolAbility() {
            return usesPool;
        }

        public boolean fitsSlot(AbilitySlot slotType) {
            return slotType == getSlotType();
        }
    }

    private int castTime;
    private int cooldown;
    private float manaCost;
    private final List<IAbilityAttribute<?>> attributes;
    private AbilityUseCondition useCondition;


    public MKAbility(String domain, String id) {
        this(new ResourceLocation(domain, id));
    }

    public MKAbility(ResourceLocation abilityId) {
        setRegistryName(abilityId);
        this.cooldown = GameConstants.TICKS_PER_SECOND;
        this.castTime = 0;
        this.manaCost = 1;
        this.attributes = new ArrayList<>();
        setUseCondition(new StandardUseCondition(this));
    }

    protected List<Object> getDescriptionArgs(IMKEntityData entityData) {
        return new ArrayList<>();
    }

    public List<ITextComponent> getDescriptionsForEntity(IMKEntityData entityData) {
        List<ITextComponent> descriptions = new ArrayList<>();
        descriptions.add(AbilityDescriptions.getManaCostDescription(this, entityData));
        descriptions.add(AbilityDescriptions.getCooldownDescription(this, entityData));
        descriptions.add(AbilityDescriptions.getCastTimeDescription(this, entityData));
        getTargetSelector().fillAbilityDescription(descriptions, this, entityData);
        descriptions.add(AbilityDescriptions.getAbilityDescription(this, entityData, this::getDescriptionArgs));
        return descriptions;
    }

    public void setUseCondition(AbilityUseCondition useCondition) {
        this.useCondition = useCondition;
    }

    public List<IAbilityAttribute<?>> getAttributes() {
        return attributes;
    }

    public AbilityUseCondition getUseCondition() {
        return useCondition;
    }

    public MKAbility addAttribute(IAbilityAttribute<?> attr) {
        attributes.add(attr);
        return this;
    }

    public MKAbility addAttributes(IAbilityAttribute<?>... attrs) {
        attributes.addAll(Arrays.asList(attrs));
        return this;
    }

    public ResourceLocation getAbilityId() {
        return getRegistryName();
    }

    public MKAbilityInfo createAbilityInfo() {
        return new MKAbilityInfo(this);
    }


    public String getAbilityName() {
        return I18n.format(getTranslationKey());
    }


    public String getTranslationKey() {
        ResourceLocation abilityId = getRegistryName();
        return String.format("%s.%s.name", abilityId.getNamespace(), abilityId.getPath());
    }

    public String getDescriptionTranslationKey() {
        ResourceLocation abilityId = getRegistryName();
        return String.format("%s.%s.description", abilityId.getNamespace(), abilityId.getPath());
    }

    public ResourceLocation getAbilityIcon() {
        ResourceLocation abilityId = getRegistryName();
        return new ResourceLocation(abilityId.getNamespace(), String.format("textures/class/abilities/%s.png", abilityId.getPath().split(Pattern.quote("."))[1]));
    }

    public void drawAbilityBarEffect(Minecraft mc, int slotX, int slotY) {

    }

    protected int getBaseCastTime() {
        return castTime;
    }

    public int getCastTime(IMKEntityData entityData) {
        return getBaseCastTime();
    }

    protected void setCastTime(int castTicks) {
        castTime = castTicks;
    }

    public boolean canApplyCastingSpeedModifier() {
        return true;
    }

    public float getDistance() {
        return 1.0f;
    }

    protected void setCooldownTicks(int ticks) {
        this.cooldown = ticks;
    }

    protected void setCooldownSeconds(int seconds) {
        this.cooldown = seconds * GameConstants.TICKS_PER_SECOND;
    }

    protected int getBaseCooldown() {
        return cooldown;
    }

    public int getCooldown(IMKEntityData entityData) {
        return getBaseCooldown();
    }

    public AbilityType getType() {
        return AbilityType.PooledActive;
    }

    public abstract TargetingContext getTargetContext();

    public boolean canSelfCast() {
        return false;
    }

    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return Targeting.isValidTarget(getTargetContext(), caster, target);
    }

    protected float getBaseManaCost() {
        return manaCost;
    }

    public float getManaCost(IMKEntityData entityData) {
        return getBaseManaCost();
    }

    protected void setManaCost(float cost) {
        manaCost = cost;
    }

    public boolean meetsRequirements(IMKEntityData entityData) {
        return entityData.getAbilityExecutor().canActivateAbility(this) &&
                entityData.getStats().canActivateAbility(this);
    }

    public <T> T serializeDynamic(DynamicOps<T> ops) {
        return ops.createMap(
                ImmutableMap.of(
                        ops.createString("cooldown"), ops.createInt(getBaseCooldown()),
                        ops.createString("manaCost"), ops.createFloat(getBaseManaCost()),
                        ops.createString("castTime"), ops.createInt(getBaseCastTime()),
                        ops.createString("attributes"),
                        ops.createMap(attributes.stream().map(attr ->
                                Pair.of(ops.createString(attr.getName()), attr.serialize(ops))
                        ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))
                )
        );
    }

    public <T> void deserializeDynamic(Dynamic<T> dynamic) {
        MKCore.LOGGER.debug("ability deserialize {}", dynamic.getValue());
        setCooldownTicks(dynamic.get("cooldown").asInt(getBaseCooldown()));
        setManaCost(dynamic.get("manaCost").asFloat(getBaseManaCost()));
        setCastTime(dynamic.get("castTime").asInt(getBaseCastTime()));

        Map<String, Dynamic<T>> map = dynamic.get("attributes").asMap(d -> d.asString(""), Function.identity());
        getAttributes().forEach(attr -> {
            Dynamic<T> attrValue = map.get(attr.getName());
            if (attrValue != null) {
                attr.deserialize(attrValue);
            }
        });
    }

    @Nullable
    public SoundEvent getCastingSoundEvent() {
        return ModSounds.casting_general;
    }

    @Nullable
    public SoundEvent getSpellCompleteSoundEvent() {
        return ModSounds.spell_cast_3;
    }

    public void executeWithContext(IMKEntityData entityData, AbilityContext context) {
        entityData.getAbilityExecutor().startAbility(context, this);
    }

    public ITextComponent getTargetContextLocalization() {
        return new TranslationTextComponent("mkcore.ability_description.target_type",
                getTargetContext().getLocalizedDescription());
    }

    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.NONE;
    }

    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return ImmutableSet.of();
    }

    public boolean isExecutableContext(AbilityContext context) {
        return getRequiredMemories().stream().allMatch(context::hasMemory);
    }

    public void continueCast(LivingEntity entity, IMKEntityData data, int castTimeLeft, AbilityContext context) {
    }

    public void continueCastClient(LivingEntity entity, IMKEntityData data, int castTimeLeft) {
    }

    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
    }

    public boolean isInterruptible() {
        return true;
    }

    protected LivingEntity getSingleLivingTarget(LivingEntity caster, float distance) {
        return getSingleLivingTarget(caster, distance, true);
    }

    protected List<LivingEntity> getTargetsInLine(LivingEntity caster, Vec3d from, Vec3d to, boolean checkValid, float growth) {
        return RayTraceUtils.getEntitiesInLine(LivingEntity.class, caster, from, to, Vec3d.ZERO, growth,
                e -> !checkValid || (e != null && isValidTarget(caster, e)));
    }

    protected LivingEntity getSingleLivingTarget(LivingEntity caster, float distance, boolean checkValid) {
        return getSingleLivingTarget(LivingEntity.class, caster, distance, checkValid);
    }

    protected <E extends LivingEntity> E getSingleLivingTarget(Class<E> clazz, LivingEntity caster,
                                                               float distance, boolean checkValid) {
        RayTraceResult lookingAt = RayTraceUtils.getLookingAt(clazz, caster, distance,
                e -> !checkValid || (e != null && isValidTarget(caster, e)));

        if (lookingAt != null && lookingAt.getType() == RayTraceResult.Type.ENTITY) {

            EntityRayTraceResult traceResult = (EntityRayTraceResult) lookingAt;
            Entity entityHit = traceResult.getEntity();
            if (entityHit instanceof LivingEntity) {

                if (checkValid && !isValidTarget(caster, (LivingEntity) entityHit)) {
                    return null;
                }

                return (E) entityHit;
            }
        }

        return null;
    }

    @Nonnull
    protected LivingEntity getSingleLivingTargetOrSelf(LivingEntity caster, float distance, boolean checkValid) {
        LivingEntity target = getSingleLivingTarget(caster, distance, checkValid);
        return target != null ? target : caster;
    }
}

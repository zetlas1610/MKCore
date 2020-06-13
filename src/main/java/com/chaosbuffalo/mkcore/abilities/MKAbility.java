package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTarget;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityUseCondition;
import com.chaosbuffalo.mkcore.abilities.ai.StandardUseCondition;
import com.chaosbuffalo.mkcore.abilities.attributes.IAbilityAttribute;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityUseContext;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public abstract class MKAbility extends ForgeRegistryEntry<MKAbility> {

    public enum AbilityType {
        Active,
        Toggle,
        Passive,
        Ultimate
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

    public String getAbilityDescription() {
        ResourceLocation abilityId = getRegistryName();
        return I18n.format(String.format("%s.%s.description", abilityId.getNamespace(), abilityId.getPath()));
    }

    public String getTranslationKey() {
        ResourceLocation abilityId = getRegistryName();
        return String.format("%s.%s.name", abilityId.getNamespace(), abilityId.getPath());
    }

    public ResourceLocation getAbilityIcon() {
        ResourceLocation abilityId = getRegistryName();
        return new ResourceLocation(abilityId.getNamespace(), String.format("textures/class/abilities/%s.png", abilityId.getPath().split(Pattern.quote("."))[1]));
    }

    public void drawAbilityBarEffect(Minecraft mc, int slotX, int slotY) {

    }

    public CastState createCastState(int castTime) {
        return new CastState(castTime);
    }

    public int getCastTime() {
        return castTime;
    }

    public MKAbility setCastTime(int newCastTime) {
        castTime = newCastTime;
        return this;
    }

    public AbilityTarget getAbilityTarget(AbilityUseContext context){
        return getUseCondition().getTarget(context);
    }

    public boolean shouldAIUse(AbilityUseContext context){
        return getUseCondition().test(context);
    }

    public float getDistance() {
        return 1.0f;
    }

    public int getCooldown() {
        return cooldown;
    }

    public MKAbility setCooldown(int cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public int getCooldownTicks() {
        return getCooldown() * GameConstants.TICKS_PER_SECOND;
    }

    public AbilityType getType() {
        return AbilityType.Active;
    }

    public abstract TargetingContext getTargetContext();

    public boolean canSelfCast() {
        return false;
    }

    protected boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return Targeting.isValidTarget(getTargetContext(), caster, target);
    }

    public float getManaCost() {
        return manaCost;
    }

    public float getManaCost(IMKEntityData entityData) {
        return getManaCost();
    }

    public MKAbility setManaCost(float newCost) {
        manaCost = newCost;
        return this;
    }

    public boolean meetsRequirements(IMKEntityData entityData) {
        return entityData.getAbilityExecutor().canActivateAbility(this) &&
                entityData.getStats().canActivateAbility(this);
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("cooldown", getCooldown());
        tag.putInt("castTime", getCastTime());
        tag.putFloat("manaCost", getManaCost());
        if (getAttributes().size() > 0) {
            CompoundNBT attributes = new CompoundNBT();
            for (IAbilityAttribute<?> attr : getAttributes()) {
                attributes.put(attr.getName(), attr.serialize());
            }
            tag.put("attributes", attributes);
        }
        return tag;
    }

    public void deserialize(CompoundNBT nbt) {
        if (nbt.contains("cooldown")) {
            setCooldown(nbt.getInt("cooldown"));
        }
        if (nbt.contains("castTime")) {
            setCastTime(nbt.getInt("castTime"));
        }
        if (nbt.contains("manaCost")) {
            setManaCost(nbt.getFloat("manaCost"));
        }
        if (nbt.contains("attributes")) {
            CompoundNBT attributes = nbt.getCompound("attributes");
            for (IAbilityAttribute<?> attr : getAttributes()) {
                if (attributes.contains(attr.getName())) {
                    attr.deserialize(attributes.getCompound(attr.getName()));
                }
            }
        }
    }

    public void readFromDataPack(JsonObject obj) {
        if (obj.has("cooldown")) {
            setCooldown(obj.get("cooldown").getAsInt());
        }
        if (obj.has("manaCost")) {
            setManaCost(obj.get("manaCost").getAsFloat());
        }
        if (obj.has("castTime")) {
            setCastTime(obj.get("castTime").getAsInt());
        }
        if (obj.has("attributes")) {
            JsonObject attributes = obj.getAsJsonObject("attributes");
            for (IAbilityAttribute<?> attr : getAttributes()) {
                if (attributes.has(attr.getName())) {
                    attr.readFromDataPack(attributes.getAsJsonObject(attr.getName()));
                }
            }
        }
    }

    @Nullable
    public SoundEvent getCastingSoundEvent() {
        return ModSounds.casting_general;
    }

    @Nullable
    public SoundEvent getSpellCompleteSoundEvent() {
        return ModSounds.spell_cast_3;
    }

    public abstract void execute(LivingEntity entity, IMKEntityData data);

    public void continueCast(LivingEntity entity, IMKEntityData data, int castTimeLeft, CastState state) {
    }

    public void continueCastClient(LivingEntity entity, IMKEntityData data, int castTimeLeft) {
    }

    public void endCast(LivingEntity entity, IMKEntityData data, CastState state) {
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

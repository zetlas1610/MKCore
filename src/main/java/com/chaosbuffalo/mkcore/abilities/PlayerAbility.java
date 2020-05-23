package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

public abstract class PlayerAbility extends ForgeRegistryEntry<PlayerAbility> {

    public enum AbilityType {
        Active,
        Toggle,
        Passive,
        Ultimate
    }

    private final ResourceLocation abilityId;

    public PlayerAbility(String domain, String id) {
        this(new ResourceLocation(domain, id));
    }

    public PlayerAbility(ResourceLocation abilityId) {
        this.abilityId = abilityId;
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public PlayerAbilityInfo createAbilityInfo() {
        return new PlayerAbilityInfo(this);
    }



    public String getAbilityName() {
        return I18n.format(getTranslationKey());
    }

    public String getAbilityDescription() {
        return I18n.format(String.format("%s.%s.description", abilityId.getNamespace(), abilityId.getPath()));
    }

    public String getTranslationKey() {
        return String.format("%s.%s.name", abilityId.getNamespace(), abilityId.getPath());
    }

    public ResourceLocation getAbilityIcon() {
        return new ResourceLocation(abilityId.getNamespace(), String.format("textures/class/abilities/%s.png", abilityId.getPath().split(Pattern.quote("."))[1]));
    }

    public void drawAbilityBarEffect(Minecraft mc, int slotX, int slotY) {

    }

    public CastState createCastState(int castTime) {
        return new CastState(castTime);
    }

    public int getCastTime(int currentRank) {
        return 0;
    }

    public float getDistance(int currentRank) {
        return 1.0f;
    }

    public abstract int getCooldown(int currentRank);

    public int getCooldownTicks(int currentRank) {
        return getCooldown(currentRank) * GameConstants.TICKS_PER_SECOND;
    }

    public AbilityType getType() {
        return AbilityType.Active;
    }

    public abstract Targeting.TargetType getTargetType();

    public boolean canSelfCast() {
        return false;
    }

    protected boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return Targeting.isValidTarget(getTargetType(), caster, target, !canSelfCast());
    }

    public abstract float getManaCost(int currentRank);

    public abstract int getRequiredLevel(int currentRank);

    public int getMaxRank() {
        return GameConstants.MAX_ABILITY_RANK;
    }

    public boolean meetsRequirements(IMKPlayerData player) {
        return true;
//        return !player.isCasting() &&
//                player.getMana() >= player.getAbilityManaCost(abilityId) &&
//                player.getCurrentAbilityCooldown(abilityId) == 0;
    }

    @Nullable
    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.casting_general;
        return null;
    }

    @Nullable
    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_cast_3;
        return null;
    }

    public abstract void execute(PlayerEntity entity, IMKPlayerData data, World theWorld);

    public void continueCast(PlayerEntity entity, IMKPlayerData data, World theWorld, int castTimeLeft, CastState state) {
    }

    public void continueCastClient(PlayerEntity entity, IMKPlayerData data, World theWorld, int castTimeLeft) {
    }

    public void endCast(PlayerEntity entity, IMKPlayerData data, World theWorld, CastState state) {
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

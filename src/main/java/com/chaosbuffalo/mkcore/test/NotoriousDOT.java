package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerToggleAbility;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NotoriousDOT extends PlayerToggleAbility {
    public static final NotoriousDOT INSTANCE = new NotoriousDOT();
    public static ResourceLocation TOGGLE_GROUP = MKCore.makeRL("toggle_group.skald");

    @SubscribeEvent
    public static void register(RegistryEvent.Register<PlayerAbility> event) {
        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
    }

    public static float BASE_DAMAGE = 1.0f;
    public static float DAMAGE_SCALE = 2.0f;
    public static int BASE_DURATION = 32767;

    private NotoriousDOT() {
        super(MKCore.makeRL("ability.notorious_dot"));
    }

    @Override
    public Effect getToggleEffect() {
        return NotoriousDOTSongPotion.INSTANCE;
    }

    @Override
    public Targeting.TargetType getTargetType() {
        return Targeting.TargetType.SELF;
    }

    @Override
    public float getDistance(int currentRank) {
        return 3.0f + currentRank * 3.0f;
    }

    @Override
    public int getRequiredLevel(int currentRank) {
        return currentRank * 2;
    }

    @Override
    public ResourceLocation getToggleGroupId() {
        return TOGGLE_GROUP;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void applyEffect(PlayerEntity entity, IMKPlayerData pData, World theWorld) {
        super.applyEffect(entity, pData, theWorld);
        int level = pData.getAbilityRank(getAbilityId());
        entity.addPotionEffect(NotoriousDOTSongPotion.Create(entity).setTarget(entity)
                .toPotionEffect(BASE_DURATION, level));
//        AbilityUtils.playSoundAtServerEntity(entity, ModSounds.spell_shadow_9, SoundCategory.PLAYERS);
        Vec3d lookVec = entity.getLookVec();
        PacketHandler.sendToTrackingAndSelf(
                new ParticleEffectSpawnPacket(
                        ParticleTypes.NOTE,
                        ParticleEffects.SPHERE_MOTION, 50, 5,
                        entity.getPosX(), entity.getPosY() + 1.0,
                        entity.getPosZ(), 1.0, 1.0, 1.0, 1.0f,
                        lookVec),
                (ServerPlayerEntity) entity);
    }
}

package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModItems;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.mku.MKUEntityTypes;
import com.chaosbuffalo.mkcore.mku.effects.GeyserEffect;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class GeyserProjectileEntity extends BaseProjectileEntity implements IRendersAsItem {
    public GeyserProjectileEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.setDeathTime(1200);
        this.setDoGroundProc(true);
        this.setGroundProcTime(50);
//        this.setSize(.4f, .4f);
    }

    public GeyserProjectileEntity(World world, Entity shooter) {
        this(MKUEntityTypes.GEYSER_PROJECTILE.get(), world);
        setPosition(shooter.getPosX(), shooter.getPosYEye() - (double) 0.1F, shooter.getPosZ());
        setShooter(shooter);
    }

    @Override
    protected TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Nonnull
    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.GEYSER_ITEM);
    }

    private boolean doEffect(LivingEntity caster, int amplifier, float baseDamage, float damageScale) {
        if (!this.world.isRemote && caster != null) {
            SpellCast geyser = GeyserEffect.Create(caster, baseDamage, damageScale);
            SoundUtils.playSoundAtEntity(this, ModSounds.spell_water_9, SoundCategory.PLAYERS);
            AreaEffectBuilder.Create(caster, this)
                    .spellCast(geyser, amplifier, TargetingContexts.ALL)
                    .instant()
                    .color(39935).radius(4.0f, true)
                    .spawn();
            PacketHandler.sendToTrackingMaybeSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.DRIPPING_WATER,
                            ParticleEffects.DIRECTED_SPOUT, 100, 1,
                            this.getPosX(), this.getPosY() + 1.0,
                            this.getPosZ(), 1.5, 2.0, 1.5, 1.0,
                            new Vec3d(0., 1.0, 0.0)), this);
//            EnvironmentUtils.putOutFires(caster, this.getPosition(), new Vec3i(16, 8, 16));
            return true;
        }
        return false;
    }

    @Override
    protected boolean onGroundProc(LivingEntity caster, int amplifier) {
        return doEffect(caster, amplifier, 0.0f, 10.0f);
    }

    @Override
    protected boolean onImpact(LivingEntity caster, RayTraceResult result, int amplifier) {
        if (!this.world.isRemote && caster != null) {
            switch (result.getType()) {
                case BLOCK:
                    return false;
                case ENTITY:
                    doEffect(caster, amplifier, 0.0f, 8.0f);
                    return true;
                case MISS:
                    return false;
            }
        }
        return false;
    }
}

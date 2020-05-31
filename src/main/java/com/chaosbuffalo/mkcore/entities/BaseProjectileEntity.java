package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseProjectileEntity extends Entity implements IProjectile {

    @Nullable
    private BlockState inBlockState;
    protected boolean inGround;

    public UUID shootingEntity;

    public static final float ONE_DEGREE = 0.017453292F;
    public static final double MAX_INACCURACY = 0.0075;

    private int amplifier;
    private int graphicalEffectTickInterval;
    private int ticksInGround;
    private int ticksInAir;
    private int deathTime;
    private int airProcTime;
    private boolean doAirProc;
    private int groundProcTime;
    private boolean doGroundProc;

    public BaseProjectileEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.inBlockState = null;
        this.inGround = false;
        this.setDeathTime(100);
        this.setDoGroundProc(false);
        this.setGroundProcTime(20);
        this.setAirProcTime(20);
        this.setDoAirProc(false);
        this.setAmplifier(0);
        graphicalEffectTickInterval = 5;
        setup();
    }

    public void setup() {

    }

    @Override
    protected void registerData() {

    }

    protected abstract TargetingContext getTargetContext();

    public int getAmplifier() {
        return this.amplifier;
    }

    public int getGraphicalEffectTickInterval() {
        return this.graphicalEffectTickInterval;
    }

    public void setGraphicalEffectTickInterval(int value) {
        graphicalEffectTickInterval = value;
    }

    public void setAmplifier(int newVal) {
        this.amplifier = newVal;
    }

    public boolean getDoGroundProc() {
        return this.doGroundProc;
    }

    public boolean getDoAirProc() {
        return this.doAirProc;
    }

    public int getTicksInAir() {
        return this.ticksInAir;
    }

    public int getTicksInGround() {
        return this.ticksInGround;
    }

    public void setTicksInAir(int newVal) {
        this.ticksInAir = newVal;
    }

    public void setTicksInGround(int newVal) {
        this.ticksInGround = newVal;
    }

    public void setDoAirProc(boolean newVal) {
        this.doAirProc = newVal;
    }

    public int getAirProcTime() {
        return this.airProcTime;
    }

    public void setAirProcTime(int newVal) {
        this.airProcTime = newVal;
    }

    public int getDeathTime() {
        return this.deathTime;
    }

    public void setDeathTime(int newVal) {
        this.deathTime = newVal;
    }

    public void setDoGroundProc(boolean newVal) {
        this.doGroundProc = newVal;
    }

    public int getGroundProcTime() {
        return this.groundProcTime;
    }

    public void setGroundProcTime(int newVal) {
        this.groundProcTime = newVal;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double edgeLength = this.getBoundingBox().getAverageEdgeLength() * 10.0D;
        if (Double.isNaN(edgeLength)) {
            edgeLength = 1.0D;
        }

        edgeLength = edgeLength * 64.0D * getRenderDistanceWeight();
        return distance < edgeLength * edgeLength;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return null;
    }

    public void shoot(Entity source, float rotationPitchIn, float rotationYawIn,
                      float pitchOffset, float velocity, float inaccuracy) {
        float x = -MathHelper.sin(rotationYawIn * ONE_DEGREE) * MathHelper.cos(rotationPitchIn * ONE_DEGREE);
        float y = -MathHelper.sin((rotationPitchIn + pitchOffset) * ONE_DEGREE);
        float z = MathHelper.cos(rotationYawIn * ONE_DEGREE) * MathHelper.cos(rotationPitchIn * ONE_DEGREE);
        this.shoot(x, y, z, velocity, inaccuracy);
        this.setMotion(this.getMotion().add(source.getMotion().x, source.onGround ? 0.0D : source.getMotion().y,
                source.getMotion().z));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        float mag = MathHelper.sqrt(x * x + y * y + z * z);
        double nX = x / (double) mag;
        double nY = y / (double) mag;
        double nZ = z / (double) mag;
        nX = nX + this.rand.nextGaussian() * MAX_INACCURACY * (double) inaccuracy;
        nY = nY + this.rand.nextGaussian() * MAX_INACCURACY * (double) inaccuracy;
        nZ = nZ + this.rand.nextGaussian() * MAX_INACCURACY * (double) inaccuracy;
        x = nX * (double) velocity;
        y = nY * (double) velocity;
        z = nZ * (double) velocity;
        this.setMotion(x, y, z);
        calculateOriginalPitchYaw(getMotion());
        this.ticksInGround = 0;
    }

    public boolean isInGround() {
        return inGround;
    }

    public void clientGraphicalUpdate() {

    }

    protected boolean checkIfInGround(BlockPos blockpos, BlockState blockstate) {
        if (!blockstate.isAir(this.world, blockpos)) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3d entityPos = this.getPositionVec();
                for (AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
                    if (axisalignedbb.offset(blockpos).contains(entityPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean missingPrevPitchAndYaw() {
        return this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F;
    }

    protected void calculateOriginalPitchYaw(Vec3d motion) {
        float xyMag = MathHelper.sqrt(horizontalMag(motion));
        this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(motion.y, xyMag) * (double) (180F / (float) Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    protected boolean onGroundProc(LivingEntity caster, int amplifier) {
        return false;
    }

    protected boolean onImpact(LivingEntity caster, RayTraceResult result, int amplifier) {
        return false;
    }


    protected boolean onAirProc(LivingEntity caster, int amplifier) {
        return false;
    }

    @Nullable
    public LivingEntity getShooter() {
        if (shootingEntity != null && world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            Entity entity = serverWorld.getEntityByUuid(shootingEntity);
            if (entity instanceof LivingEntity) {
                return (LivingEntity) entity;
            }
        }
        return null;
    }

    protected boolean isValidEntityTargetGeneric(Entity entity) {
        return entity != this && EntityPredicates.NOT_SPECTATING.test(entity) && EntityPredicates.IS_ALIVE.test(entity);
    }


    protected boolean isValidEntityTarget(Entity entity) {
        if (entity instanceof LivingEntity && getShooter() != null) {
            return Targeting.isValidTarget(getTargetContext(), getShooter(), entity);
        }
        return isValidEntityTargetGeneric(entity);
    }

    private EntityRayTraceResult checkRayTraceEntities(Vec3d traceStart, Vec3d traceEnd, RayTraceResult blockTrace) {
        Entity entity = null;
        EntityRayTraceResult entityResult = null;
        Vec3d motion = getMotion();
        List<Entity> targets = this.world.getEntitiesInAABBexcluding(this,
                this.getBoundingBox().expand(motion.getX(), motion.getY(), motion.getZ()).grow(1.0D),
                this::isValidEntityTarget);
        double shortestEntityDistance = 0.0D;

        Vec3d hitVec = null;
        for (Entity hitEntity : targets) {
            if (!hitEntity.canBeCollidedWith()) {
                continue;
            }
            if (isValidEntityTarget(hitEntity)) {
                AxisAlignedBB mobHitBB = hitEntity.getBoundingBox().grow(0.3);
                Optional<Vec3d> intercept = mobHitBB.rayTrace(traceStart, traceEnd);

                if (intercept.isPresent()) {
                    double distToEntity = traceStart.squareDistanceTo(intercept.get());

                    if (distToEntity < shortestEntityDistance || shortestEntityDistance == 0.0D) {
                        entity = hitEntity;
                        hitVec = intercept.get();
                        shortestEntityDistance = distToEntity;
                    }
                }
            }
        }

        if (entity != null) {
            entityResult = new EntityRayTraceResult(entity, hitVec);
        }
        return entityResult;
    }

    protected boolean onHit(RayTraceResult rayTraceResult) {
        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) rayTraceResult;
            BlockState blockstate = this.world.getBlockState(blockraytraceresult.getPos());
            this.inBlockState = blockstate;
            Vec3d vec3d = blockraytraceresult.getHitVec().subtract(this.getPosX(), this.getPosY(), this.getPosZ());
            this.setMotion(vec3d);
            Vec3d vec3d1 = vec3d.normalize().scale((double) 0.05F);
            this.setRawPosition(this.getPosX() - vec3d1.x, this.getPosY() - vec3d1.y,
                    this.getPosZ() - vec3d1.z);
            this.inGround = true;
            blockstate.onProjectileCollision(this.world, blockstate, blockraytraceresult, this);
        }
        return this.onImpact(getShooter(), rayTraceResult, getAmplifier());

    }

    public float getGravityVelocity() {
        return 0.03F;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        compound.putInt("ticksInGround", this.ticksInGround);
        compound.putInt("ticksInAir", this.ticksInAir);
        if (this.inBlockState != null) {
            compound.put("inBlockState", NBTUtil.writeBlockState(this.inBlockState));
        }

        compound.putBoolean("inGround", this.inGround);
        if (this.shootingEntity != null) {
            compound.putUniqueId("OwnerUUID", this.shootingEntity);
        }

        compound.putBoolean("doAirProc", this.getDoAirProc());
        compound.putBoolean("doGroundProc", this.getDoGroundProc());
        compound.putInt("airProcTime", this.getAirProcTime());
        compound.putInt("groundProcTime", this.getGroundProcTime());
        compound.putInt("deathTime", this.getDeathTime());
        compound.putInt("amplifier", this.getAmplifier());
    }

    public void readAdditional(CompoundNBT compound) {
        this.ticksInGround = compound.getInt("ticksInGround");
        this.ticksInAir = compound.getInt("ticksInAir");
        if (compound.contains("inBlockState", 10)) {
            this.inBlockState = NBTUtil.readBlockState(compound.getCompound("inBlockState"));
        }

        this.inGround = compound.getBoolean("inGround");


        if (compound.hasUniqueId("OwnerUUID")) {
            this.shootingEntity = compound.getUniqueId("OwnerUUID");
        }

        this.setDoAirProc(compound.getBoolean("doAirProc"));
        this.setDoGroundProc(compound.getBoolean("doGroundProc"));
        this.setAirProcTime(compound.getInt("airProcTime"));
        this.setGroundProcTime(compound.getInt("groundProcTime"));
        this.setDeathTime(compound.getInt("deathTime"));
        this.setAmplifier(compound.getInt("amplifier"));
    }

    @Override
    public void tick() {
        this.lastTickPosX = this.getPosX();
        this.lastTickPosY = this.getPosY();
        this.lastTickPosZ = this.getPosZ();
        super.tick();
        if (!isAlive()) {
            return;
        }

        if (this.ticksExisted == this.getDeathTime()) {
            this.remove();
        }

        Vec3d motion = this.getMotion();

        if (missingPrevPitchAndYaw()) {
            calculateOriginalPitchYaw(motion);
        }

        BlockPos blockpos = new BlockPos(this);
        BlockState blockstate = this.world.getBlockState(blockpos);
        this.inGround = checkIfInGround(blockpos, blockstate);

        if (world.isRemote && ticksExisted % graphicalEffectTickInterval == 0) {
            clientGraphicalUpdate();
        }

        if (this.inGround) {
            if (this.inBlockState != blockstate && this.world.hasNoCollisions(this.getBoundingBox().grow(0.06D))) {
                this.inGround = false;
                this.setMotion(motion.mul(this.rand.nextFloat() * 0.2F,
                        this.rand.nextFloat() * 0.2F,
                        this.rand.nextFloat() * 0.2F));
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            } else {
                ++this.ticksInGround;
                if (this.getDoGroundProc() && this.ticksInGround > 0 &&
                        this.ticksInGround % this.getGroundProcTime() == 0) {
                    if (this.onGroundProc(this.getShooter(), this.getAmplifier())) {
                        this.remove();
                    }
                }
            }
        } else {
            this.ticksInGround = 0;
            ++this.ticksInAir;
            if (this.getDoAirProc() && this.ticksInAir == this.getAirProcTime()) {
                if (this.onAirProc(this.getShooter(), this.getAmplifier())) {
                    this.remove();
                }
            }
            RayTraceResult trace;
            Vec3d traceStart = this.getPositionVec();
            Vec3d traceEnd = traceStart.add(motion);
            RayTraceResult blockRayTrace = this.world.rayTraceBlocks(new RayTraceContext(traceStart, traceEnd,
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            trace = blockRayTrace;
            if (blockRayTrace.getType() != RayTraceResult.Type.MISS) {
                traceEnd = blockRayTrace.getHitVec();
            }

            EntityRayTraceResult entityRayTrace = this.checkRayTraceEntities(traceStart, traceEnd, blockRayTrace);
            if (entityRayTrace != null) {
                trace = entityRayTrace;
            }
            if (trace.getType() == RayTraceResult.Type.ENTITY) {
                Entity target = entityRayTrace.getEntity();
                Entity shooter = this.getShooter();
                if (target instanceof PlayerEntity && shooter instanceof PlayerEntity &&
                        !((PlayerEntity) shooter).canAttackPlayer((PlayerEntity) target)) {
                    trace = null;
                }
            }

            if (trace != null && trace.getType() != RayTraceResult.Type.MISS &&
                    !ForgeEventFactory.onProjectileImpact(this, trace)) {
                if (this.onHit(trace)) {
                    this.remove();
                }
                this.isAirBorne = true;
            }

            float xyMag = MathHelper.sqrt(horizontalMag(motion));
            this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI));
            this.rotationPitch = (float) (MathHelper.atan2(motion.y, xyMag) * (double) (180F / (float) Math.PI));

            while (this.rotationPitch - this.prevRotationPitch < -180.0F) {
                this.prevRotationPitch -= 360.0F;
            }

            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            if (!this.inGround && !world.isRemote) {
                setMotion(motion.subtract(new Vec3d(0.0, getGravityVelocity(), 0.0)));
            }
            this.setPosition(getPosX() + motion.getX(), getPosY() + motion.getY(), getPosZ() + motion.getZ());
        }
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void setVelocity(double x, double y, double z) {
        super.setVelocity(x, y, z);
        if (missingPrevPitchAndYaw()) {
            calculateOriginalPitchYaw(getMotion());
            this.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(),
                    this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }
}

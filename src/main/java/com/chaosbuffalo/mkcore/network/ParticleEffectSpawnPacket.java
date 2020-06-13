package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ParticleEffectSpawnPacket {
    private final double xPos;
    private final double yPos;
    private final double zPos;
    private final int motionType;
    private final double speed;
    private final int count;
    private final double radiusX;
    private final double radiusY;
    private final double radiusZ;
    private final IParticleData particleID;
    private final int data;
    private final double headingX;
    private final double headingY;
    private final double headingZ;


    public ParticleEffectSpawnPacket(IParticleData particleID, int motionType, int count, int data,
                                     double xPos, double yPos, double zPos,
                                     double radiusX, double radiusY, double radiusZ,
                                     double speed, double headingX, double headingY, double headingZ) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.motionType = motionType;
        this.count = count;
        this.speed = speed;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.particleID = particleID;
        this.data = data;
        this.headingX = headingX;
        this.headingY = headingY;
        this.headingZ = headingZ;
    }

    public ParticleEffectSpawnPacket(IParticleData particleID, int motionType, int count, int data,
                                     double xPos, double yPos, double zPos,
                                     double radiusX, double radiusY, double radiusZ,
                                     double speed, Vec3d headingVec) {
        this(particleID, motionType, count, data,
                xPos, yPos, zPos,
                radiusX, radiusY, radiusZ, speed,
                headingVec.x, headingVec.y, headingVec.z);
    }

    public ParticleEffectSpawnPacket(IParticleData particleID, int motionType, int count, int data,
                                     Vec3d posVec,
                                     double radiusX, double radiusY, double radiusZ,
                                     double speed, Vec3d headingVec) {
        this(particleID, motionType, count, data, posVec.x, posVec.y, posVec.z, radiusX,
                radiusY, radiusZ, speed, headingVec.x, headingVec.y, headingVec.z);
    }

    public IParticleData read(PacketBuffer buf) {
        return this.read(buf, Registry.PARTICLE_TYPE.getByValue(buf.readVarInt()));
    }

    private <T extends IParticleData> T read(PacketBuffer buf, ParticleType<T> type) {
        return type.getDeserializer().read(type, buf);
    }

    public ParticleEffectSpawnPacket(PacketBuffer buf) {
        this.particleID = read(buf);
        this.motionType = buf.readInt();
        this.data = buf.readInt();
        this.count = buf.readInt();
        this.xPos = buf.readDouble();
        this.yPos = buf.readDouble();
        this.zPos = buf.readDouble();
        this.radiusX = buf.readDouble();
        this.radiusY = buf.readDouble();
        this.radiusZ = buf.readDouble();
        this.speed = buf.readDouble();
        this.headingX = buf.readDouble();
        this.headingY = buf.readDouble();
        this.headingZ = buf.readDouble();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(Registry.PARTICLE_TYPE.getId(particleID.getType()));
        buf.writeInt(this.motionType);
        buf.writeInt(this.data);
        buf.writeInt(this.count);
        buf.writeDouble(this.xPos);
        buf.writeDouble(this.yPos);
        buf.writeDouble(this.zPos);
        buf.writeDouble(this.radiusX);
        buf.writeDouble(this.radiusY);
        buf.writeDouble(this.radiusZ);
        buf.writeDouble(this.speed);
        buf.writeDouble(this.headingX);
        buf.writeDouble(this.headingY);
        buf.writeDouble(this.headingZ);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
//            MKCore.LOGGER.info("Got spawn particle packet");
            ParticleEffects.spawnParticleEffect(
                    particleID, motionType, data, speed, count,
                    new Vec3d(xPos, yPos, zPos),
                    new Vec3d(radiusX, radiusY, radiusZ),
                    new Vec3d(headingX, headingY, headingZ),
                    Minecraft.getInstance().player.world);
        });
        ctx.setPacketHandled(true);
    }
}

package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CritMessagePacket {
    public enum CritType {
        MELEE_CRIT,
        MK_CRIT,
        PROJECTILE_CRIT
    }

    private final int targetId;
    private final UUID sourceUUID;
    private ResourceLocation abilityName;
    private ResourceLocation damageType;
    private final float critDamage;
    private final CritType type;
    private int projectileId;

    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage, CritType type) {
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.type = type;
    }

    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage, ResourceLocation abilityName,
                             MKDamageType damageType) {
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.type = CritType.MK_CRIT;
        this.abilityName = abilityName;
        this.damageType = damageType.getRegistryName();
    }

    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage, int projectileId) {
        this.type = CritType.PROJECTILE_CRIT;
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.projectileId = projectileId;
    }

    public CritMessagePacket(PacketBuffer pb) {
        this.type = pb.readEnumValue(CritType.class);
        this.targetId = pb.readInt();
        this.sourceUUID = pb.readUniqueId();
        this.critDamage = pb.readFloat();
        if (type == CritType.MK_CRIT) {
            this.abilityName = pb.readResourceLocation();
            this.damageType = pb.readResourceLocation();
        }
        if (type == CritType.PROJECTILE_CRIT) {
            this.projectileId = pb.readInt();
        }
    }

    public void toBytes(PacketBuffer pb) {
        pb.writeEnumValue(type);
        pb.writeInt(targetId);
        pb.writeUniqueId(sourceUUID);
        pb.writeFloat(critDamage);
        if (type == CritType.MK_CRIT) {
            pb.writeResourceLocation(this.abilityName);
            pb.writeResourceLocation(this.damageType);
        }
        if (type == CritType.PROJECTILE_CRIT) {
            pb.writeInt(this.projectileId);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Style messageStyle = new Style();
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean isSelf = player.getUniqueID().equals(sourceUUID);
        PlayerEntity playerSource = player.getEntityWorld().getPlayerByUuid(sourceUUID);
        Entity target = player.getEntityWorld().getEntityByID(targetId);
        if (target == null || playerSource == null) {
            return;
        }
        boolean isSelfTarget = player.getEntityId() == targetId;
        if (isSelf || isSelfTarget) {
            if (!MKConfig.CLIENT.showMyCrits.get()) {
                return;
            }
        } else {
            if (!MKConfig.CLIENT.showOthersCrits.get()) {
                return;
            }
        }
        switch (type) {
            case MELEE_CRIT:
                messageStyle.setColor(TextFormatting.DARK_RED);
                if (isSelf) {
                    player.sendMessage(new StringTextComponent(
                            String.format("You just crit %s with %s for %s",
                                    target.getDisplayName().getFormattedText(),
                                    playerSource.getHeldItemMainhand().getDisplayName().getFormattedText(),
                                    Math.round(critDamage)))
                            .setStyle(messageStyle));
                } else {
                    player.sendMessage(new StringTextComponent(
                            String.format("%s just crit %s with %s for %s",
                                    playerSource.getDisplayName().getFormattedText(),
                                    target.getDisplayName().getFormattedText(),
                                    playerSource.getHeldItemMainhand().getDisplayName().getFormattedText(),
                                    Math.round(critDamage))
                    ).setStyle(messageStyle));
                }
                break;
            case MK_CRIT:
                messageStyle.setColor(TextFormatting.AQUA);
                MKAbility ability = MKCoreRegistry.getAbility(abilityName);
                MKDamageType mkDamageType = MKCoreRegistry.getDamageType(damageType);
                if (ability == null || mkDamageType == null) {
                    break;
                }
                player.sendMessage(mkDamageType.getCritMessage(playerSource, (LivingEntity) target, critDamage, ability, isSelf));
                break;
            case PROJECTILE_CRIT:
                Entity projectile = player.getEntityWorld().getEntityByID(projectileId);
                if (projectile != null) {
                    messageStyle.setColor(TextFormatting.LIGHT_PURPLE);
                    if (isSelf) {
                        player.sendMessage(new StringTextComponent(
                                String.format("You just crit %s with %s for %s",
                                        target.getDisplayName().getUnformattedComponentText(),
                                        projectile.getDisplayName().getUnformattedComponentText(),
                                        Math.round(critDamage)))
                                .setStyle(messageStyle));
                    } else {
                        player.sendMessage(new StringTextComponent(
                                String.format("%s just crit %s with %s for %s",
                                        playerSource.getDisplayName().getUnformattedComponentText(),
                                        target.getDisplayName().getUnformattedComponentText(),
                                        projectile.getDisplayName().getUnformattedComponentText(),
                                        Math.round(critDamage))
                        ).setStyle(messageStyle));
                    }
                }
                break;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }
}
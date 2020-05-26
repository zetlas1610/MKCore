package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CritMessagePacket {
    public enum CritType {
        INDIRECT_MAGIC_CRIT,
        MELEE_CRIT,
        SPELL_CRIT,
        INDIRECT_CRIT,
        PROJECTILE_CRIT,
        HOLY_DAMAGE_CRIT
    }

    private int targetId;
    private UUID sourceUUID;
    private ResourceLocation abilityName;
    private float critDamage;
    private CritType type;
    private int projectileId;

    public CritMessagePacket() {
    }

    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage, CritType type) {
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.type = type;
    }

    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage, ResourceLocation abilityName) {
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.type = CritType.SPELL_CRIT;
        this.abilityName = abilityName;
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
        if (type == CritType.SPELL_CRIT) {
            this.abilityName = pb.readResourceLocation();
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
        if (type == CritType.SPELL_CRIT) {
            pb.writeResourceLocation(this.abilityName);
        }
        if (type == CritType.PROJECTILE_CRIT) {
            pb.writeInt(this.projectileId);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Style messageStyle = new Style();
            PlayerEntity player = Minecraft.getInstance().player;
            boolean isSelf = player.getUniqueID().equals(sourceUUID);
            PlayerEntity playerSource = player.getEntityWorld().getPlayerByUuid(sourceUUID);
            Entity target = player.getEntityWorld().getEntityByID(targetId);
            if (target == null || playerSource == null) {
                return;
            }
            boolean isSelfTarget = player.getEntityId() == targetId;
            if (isSelf || isSelfTarget) {
                if (!MKConfig.showMyCrits.get()) {
                    return;
                }
            } else {
                if (!MKConfig.showOthersCrits.get()) {
                    return;
                }
            }
            switch (type) {
                case MELEE_CRIT:
                case INDIRECT_CRIT:
                    messageStyle.setColor(
                            type == CritType.MELEE_CRIT ? TextFormatting.DARK_RED : TextFormatting.GOLD
                    );
                    if (isSelf) {
                        player.sendMessage(new StringTextComponent(
                                String.format("You just crit %s with %s for %s",
                                        target.getDisplayName().getUnformattedComponentText(),
                                        playerSource.getHeldItemMainhand().getDisplayName(),
                                        Integer.toString(Math.round(critDamage))))
                                .setStyle(messageStyle));
                    } else {
                        player.sendMessage(new StringTextComponent(
                                String.format("%s just crit %s with %s for %s",
                                        playerSource.getDisplayName().getUnformattedComponentText(),
                                        target.getDisplayName().getUnformattedComponentText(),
                                        playerSource.getHeldItemMainhand().getDisplayName(),
                                        Math.round(critDamage))
                        ).setStyle(messageStyle));
                    }
                    break;
                case INDIRECT_MAGIC_CRIT:
                    messageStyle.setColor(TextFormatting.BLUE);
                    if (isSelf) {
                        player.sendMessage(new StringTextComponent(
                                String.format("Your magic spell just crit %s for %s",
                                        target.getDisplayName().getUnformattedComponentText(),
                                        Math.round(critDamage)))
                                .setStyle(messageStyle));
                    } else {
                        player.sendMessage(new StringTextComponent(
                                String.format("%s's magic spell just crit %s for %s",
                                        playerSource.getDisplayName().getUnformattedComponentText(),
                                        target.getDisplayName().getUnformattedComponentText(),
                                        Math.round(critDamage)))
                                .setStyle(messageStyle));
                    }
                    break;
                case HOLY_DAMAGE_CRIT:
                    messageStyle.setColor(TextFormatting.RED);
                    if (isSelf) {
                        player.sendMessage(new StringTextComponent(
                                String.format("Your holy aura just crit %s for %s",
                                        target.getDisplayName().getUnformattedComponentText(),
                                        Math.round(critDamage)))
                                .setStyle(messageStyle));
                    } else {
                        player.sendMessage(new StringTextComponent(
                                String.format("%s's holy aura just crit %s for %s",
                                        playerSource.getDisplayName().getUnformattedComponentText(),
                                        target.getDisplayName().getUnformattedComponentText(),
                                        Integer.toString(Math.round(critDamage))))
                                .setStyle(messageStyle));
                    }
                    break;
                case SPELL_CRIT:
                    messageStyle.setColor(TextFormatting.AQUA);
                    PlayerAbility ability = MKCoreRegistry.getAbility(abilityName);
                    if (ability == null) {
                        break;
                    }
                    if (isSelf) {
                        player.sendMessage(new StringTextComponent(
                                String.format("Your %s spell just crit %s for %s",
                                        ability.getAbilityName(),
                                        target.getDisplayName().getUnformattedComponentText(),
                                        Math.round(critDamage)))
                                .setStyle(messageStyle)
                        );

                    } else {
                        player.sendMessage(new StringTextComponent(
                                String.format("%s's %s spell just crit %s for %s",
                                        playerSource.getDisplayName().getUnformattedComponentText(),
                                        ability.getAbilityName(),
                                        target.getDisplayName().getUnformattedComponentText(),
                                        Math.round(critDamage)))
                                .setStyle(messageStyle)
                        );
                    }
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
        });
        ctx.setPacketHandled(true);
    }
}
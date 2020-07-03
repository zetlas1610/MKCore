package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.LearnAbilitiesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenLearnAbilitiesGuiPacket {

    private final List<MKAbility> abilities;

    public OpenLearnAbilitiesGuiPacket(List<MKAbility> abilities){
        this.abilities = abilities;
    }

    public OpenLearnAbilitiesGuiPacket(PacketBuffer buffer){
        int count = buffer.readInt();
        this.abilities = new ArrayList<>();
        for (int i=0;i<count;i++){
            ResourceLocation loc = buffer.readResourceLocation();
            MKAbility ability = MKCoreRegistry.getAbility(loc);
            if (ability != null){
                abilities.add(ability);
            }
        }
    }

    public void toBytes(PacketBuffer buffer){
        buffer.writeInt(abilities.size());
        for (MKAbility ability : abilities){
            buffer.writeResourceLocation(ability.getAbilityId());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Minecraft.getInstance().displayGuiScreen(new LearnAbilitiesScreen(
                    new StringTextComponent("Learn Abilities"), abilities));
        });
        ctx.setPacketHandled(true);
    }
}

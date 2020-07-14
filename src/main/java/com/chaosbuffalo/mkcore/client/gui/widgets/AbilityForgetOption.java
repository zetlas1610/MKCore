package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKModal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.List;

class AbilityForgetOption extends MKLayout {

    private final List<ResourceLocation> abilitySlots;
    private final ResourceLocation loc;
    private final MKModal popup;
    private final MKAbility ability;
    private final int trainerEntityId;

    public AbilityForgetOption(MKAbility ability, List<ResourceLocation> abilitySlots,
                               ResourceLocation loc, MKModal popup,
                               FontRenderer font, int trainerEntity) {
        super(0, 0, 200, 16);
        this.abilitySlots = abilitySlots;
        this.loc = loc;
        this.popup = popup;
        this.ability = ability;
        this.trainerEntityId = trainerEntity;
        IconText iconText = new IconText(0, 0, 16, ability.getAbilityName(), ability.getAbilityIcon(),
                font, 16, 1);
        this.addWidget(iconText);
        addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), iconText);
        addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.LEFT), iconText);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        MKCore.LOGGER.info("In mouse release for popup {}", getAbility().getAbilityId());
        PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(
                loc, abilitySlots.indexOf(getAbility().getAbilityId()), trainerEntityId));
        if (getScreen() != null){
            getScreen().closeModal(popup);
        }
        return true;
    }

    public MKAbility getAbility() {
        return ability;
    }

    @Override
    public void postDraw(Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()) {
            mkFill(x, y, x + width, y + height, 0x55ffffff);
        }
    }
}

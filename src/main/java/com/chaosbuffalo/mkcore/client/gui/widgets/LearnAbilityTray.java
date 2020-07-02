package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;

public class LearnAbilityTray extends MKStackLayoutHorizontal {
    private MKAbility ability;
    private final MKPlayerData playerData;
    private final FontRenderer font;

    public LearnAbilityTray(int x, int y, int height, MKPlayerData playerData, FontRenderer font) {
        super(x, y, height);
        this.playerData = playerData;
        this.font = font;
        this.ability = null;
        setPaddingLeft(2);
        setPaddingRight(2);
        setup();
    }

    public void setup(){
        clearWidgets();
        if (getAbility() != null){
            MKText abilityName = new MKText(font, getAbility().getAbilityName());
            abilityName.setWidth(font.getStringWidth(getAbility().getAbilityName()));
            addWidget(abilityName);
            boolean canLearn = playerData.getKnowledge().getKnownAbilityInfo(
                    getAbility().getAbilityId()) == null;
            String knowText = canLearn ? "can learn" : "already know";
            MKText doesKnowWid = new MKText(font, knowText);
            doesKnowWid.setWidth(font.getStringWidth(knowText));
            addWidget(doesKnowWid);
            MKButton learnButton = new MKButton(0, 0, "Learn");
            learnButton.setWidth(font.getStringWidth("Learn") + 10);
            learnButton.setEnabled(canLearn);
            learnButton.setPressedCallback((button, buttonType) -> {
                PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(getAbility().getAbilityId()));
                return true;
            });
            addWidget(learnButton);
        } else {
            MKText prompt = new MKText(font, "Select an ability to learn.");
            addWidget(prompt);
        }
    }

    public void setAbility(MKAbility ability) {
        this.ability = ability;
        setup();
    }

    public MKAbility getAbility() {
        return ability;
    }
}

package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
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
        setPaddingLeft(2);
        setPaddingRight(2);
    }

    public void setup(){
        clearWidgets();
        if (getAbility() != null){
            MKText abilityName = new MKText(font, getAbility().getAbilityName());
            abilityName.setWidth(font.getStringWidth(getAbility().getAbilityName()));
            addWidget(abilityName);
            String doesKnow = playerData.getKnowledge().getKnownAbilityInfo(
                    getAbility().getAbilityId()) == null ? "can learn" : "already know";
            MKText doesKnowWid = new MKText(font, doesKnow);
            doesKnowWid.setWidth(font.getStringWidth(doesKnow));
            addWidget(doesKnowWid);
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

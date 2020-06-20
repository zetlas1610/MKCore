package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;

public class AbilityInfoWidget extends MKStackLayoutVertical {

    private final MKPlayerData playerData;
    private MKAbilityInfo abilityInfo;
    private final FontRenderer fontRenderer;

    public AbilityInfoWidget(int x, int y, int width, MKPlayerData playerData, FontRenderer fontRenderer) {
        super(x, y, width);
        this.playerData = playerData;
        this.fontRenderer = fontRenderer;
        setMargins(4, 4, 4, 4);
        doSetChildWidth(true);
        setup();
    }

    public void setup(){
        if (abilityInfo == null){
            MKText noSelectPrompt = new MKText(fontRenderer,"Select An Ability to inspect it.");
            addWidget(noSelectPrompt);
        } else {
            IconText ability = new IconText(0, 0, 16,
                    abilityInfo.getAbility().getAbilityName(),
                    abilityInfo.getAbility().getAbilityIcon(),
                    fontRenderer, 16);
            addWidget(ability);
        }
    }


    public void setAbilityInfo(MKAbilityInfo abilityInfo) {
        this.abilityInfo = abilityInfo;
        clearWidgets();
        setup();
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }
}

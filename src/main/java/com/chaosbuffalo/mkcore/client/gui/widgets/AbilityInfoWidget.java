package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.AbilityPanelScreen;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityInfoWidget extends MKStackLayoutVertical {

    private final MKPlayerData playerData;
    private MKAbilityInfo abilityInfo;
    private final FontRenderer fontRenderer;
    private final AbilityPanelScreen screen;

    public AbilityInfoWidget(int x, int y, int width, MKPlayerData playerData,
                             FontRenderer fontRenderer, AbilityPanelScreen screen) {
        super(x, y, width);
        this.screen = screen;
        this.playerData = playerData;
        this.fontRenderer = fontRenderer;
        abilityInfo = null;
        setMargins(6, 6, 6, 6);
        setPaddings(0, 0, 2, 2);
        doSetChildWidth(true);
        setup();
    }

    public void setup(){
        if (abilityInfo == null){
            MKText noSelectPrompt = new MKText(fontRenderer,new TranslationTextComponent("mkcore.gui.select_ability"));
            noSelectPrompt.setColor(0xffffffff);
            addWidget(noSelectPrompt);
        } else {
            IconText ability = new AbilityIconText(0, 0, 16,
                    abilityInfo.getAbility().getAbilityName(),
                    abilityInfo.getAbility().getAbilityIcon(),
                    fontRenderer, 16, screen, abilityInfo.getAbility());
            addWidget(ability);
            for (ITextComponent desc : abilityInfo.getAbility().getDescriptionsForEntity(playerData)){
                MKText text = new MKText(fontRenderer, desc);
                text.setColor(0xaaffffff);
                text.setMultiline(true);
                addWidget(text);
            }
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

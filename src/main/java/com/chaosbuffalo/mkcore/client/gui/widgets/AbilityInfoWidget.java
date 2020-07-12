package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.AbilityPanelScreen;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityInfoWidget extends MKStackLayoutVertical {

    private final MKPlayerData playerData;
    private MKAbility ability;
    private final FontRenderer fontRenderer;
    private final AbilityPanelScreen screen;

    public AbilityInfoWidget(int x, int y, int width, MKPlayerData playerData,
                             FontRenderer fontRenderer, AbilityPanelScreen screen) {
        super(x, y, width);
        this.screen = screen;
        this.playerData = playerData;
        this.fontRenderer = fontRenderer;
        ability = null;
        setMargins(6, 6, 6, 6);
        setPaddings(0, 0, 2, 2);
        doSetChildWidth(true);
        setup();
    }

    public void setup() {
        if (ability == null) {
            MKText noSelectPrompt = new MKText(fontRenderer, new TranslationTextComponent("mkcore.gui.select_ability"));
            noSelectPrompt.setColor(0xffffffff);
            addWidget(noSelectPrompt);
        } else {
            IconText abilityIcon = new AbilityIconText(0, 0, 16,
                    this.ability.getAbilityName(),
                    this.ability.getAbilityIcon(),
                    fontRenderer, 16, screen, this.ability);
            addWidget(abilityIcon);
            for (ITextComponent desc : this.ability.getDescriptionsForEntity(playerData)) {
                MKText text = new MKText(fontRenderer, desc);
                text.setColor(0xaaffffff);
                text.setMultiline(true);
                addWidget(text);
            }
        }
    }


    public void setAbility(MKAbility ability) {
        this.ability = ability;
        clearWidgets();
        setup();
    }

    public MKAbility getAbility() {
        return ability;
    }
}

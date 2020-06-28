package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkwidgets.client.gui.actions.WidgetHoldingDragState;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

public class AbilityIconText extends IconText {
    private CharacterScreen screen;
    private MKAbility ability;

    public AbilityIconText(int x, int y, int height, String text, ResourceLocation iconLoc,
                           FontRenderer font, int iconWidth, CharacterScreen screen, MKAbility ability) {
        super(x, y, height, text, iconLoc, font, iconWidth, 1);
        this.screen = screen;
        this.ability = ability;
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        screen.setDragState(new WidgetHoldingDragState(new MKImage(0, 0, icon.getWidth(),
                icon.getHeight(), icon.getImageLoc())), this);
        screen.setDragging(ability);
        return true;
    }
}

package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

public class IconText extends MKStackLayoutHorizontal {

    private MKText textWidget;

    public IconText(int x, int y, int height, String text, ResourceLocation iconLoc,
                    FontRenderer font, int iconWidth) {
        super(x, y, height);
        setPaddingRight(2);
        setPaddingLeft(2);
        MKImage icon = new MKImage(0, 0, iconWidth, height, iconLoc);
        addWidget(icon);
        MKText name = new MKText(font, text);
        textWidget = name;
        name.setWidth(100);
        name.setColor(0xffffffff);
        addWidget(name);
        addConstraintToWidget(new CenterYConstraint(), name);
    }

    public MKText getTextWidget() {
        return textWidget;
    }
}

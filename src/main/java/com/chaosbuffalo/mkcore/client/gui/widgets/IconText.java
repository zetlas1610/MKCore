package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

public class IconText extends MKStackLayoutHorizontal {

    protected MKText text;
    protected MKImage icon;

    public IconText(int x, int y, int height, String textStr, ResourceLocation iconLoc,
                    FontRenderer font, int iconWidth) {
        super(x, y, height);
        setPaddingRight(2);
        setPaddingLeft(2);
        icon = new MKImage(0, 0, iconWidth, height, iconLoc);
        addWidget(icon);
        text = new MKText(font, textStr);
        text.setWidth(100);
        text.setColor(0xffffffff);
        addWidget(text);
        addConstraintToWidget(new CenterYConstraint(), text);
    }

    public MKText getText() {
        return text;
    }

}

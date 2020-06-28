package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;

public class NamedField extends MKStackLayoutHorizontal {

    public NamedField(int x, int y, String name, int nameColor,
                      String value, int valueColor, FontRenderer renderer) {
        super(x, y, renderer.FONT_HEIGHT + 2);
        setMargins(1, 1, 1, 1);
        setPaddings(1, 1, 0, 0);
        MKText nameText = new MKText(renderer, name, renderer.getStringWidth(name));
        nameText.setColor(nameColor);
        MKText valueText = new MKText(renderer, value, renderer.getStringWidth(value));
        valueText.setColor(valueColor);
        addWidget(nameText);
        addWidget(valueText);
    }
}

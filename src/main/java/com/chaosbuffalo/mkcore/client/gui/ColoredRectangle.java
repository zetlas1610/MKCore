package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.Minecraft;

public class ColoredRectangle extends MKWidget {
    private int color;

    public ColoredRectangle(int x, int y, int width, int height, int color) {
        super(x, y, width, height);
        this.color = color;
    }

    public static ColoredRectangle GetBar(int height, int color){
        return new ColoredRectangle(0, 0, 200, height, color);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void draw(Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        mkFill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), getColor());
    }
}

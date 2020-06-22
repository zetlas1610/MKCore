package com.chaosbuffalo.mkcore.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.BaseConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

public class CopyConstraint extends BaseConstraint {
    @Override
    public void applyConstraint(IMKLayout imkLayout, IMKWidget imkWidget, int i) {
        imkWidget.setX(imkLayout.getX());
        imkWidget.setY(imkLayout.getY());
        imkWidget.setWidth(imkLayout.getWidth());
        imkWidget.setHeight(imkLayout.getHeight());
    }
}

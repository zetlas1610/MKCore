package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.HorizontalStackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeHeightConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;

public class ScrollingListPanelLayout extends MKLayout {

    private MKScrollView listScrollView;
    private MKScrollView contentScrollView;

    public ScrollingListPanelLayout(int x, int y, int width, int height) {
        super(x, y, width, height);
        setMargins(4, 4, 4, 4);
        setPaddings(1, 1, 0, 0);
        listScrollView = new MKScrollView(getX(), getY(), Math.round(getWidth() * .33f), getHeight(), true);
        addWidget(listScrollView);
        addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.LEFT), listScrollView);
        addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), listScrollView);
        addConstraintToWidget(new LayoutRelativeWidthConstraint(.33f), listScrollView);
        addConstraintToWidget(new LayoutRelativeHeightConstraint(1.0f), listScrollView);
        MKRectangle rect = new MKRectangle(getX(), getY(), 1, getHeight(), 0x99ffffff);
        addWidget(rect);
        addConstraintToWidget(new LayoutRelativeHeightConstraint(1.0f), rect);
        addConstraintToWidget(new HorizontalStackConstraint(), rect);
        addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), rect);
        contentScrollView = new MKScrollView(getX(), getY(), Math.round(getWidth() * .66f), getHeight(), true);
        addWidget(contentScrollView);
        addConstraintToWidget(new HorizontalStackConstraint(), contentScrollView);
        addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), contentScrollView);
        addConstraintToWidget(new LayoutRelativeWidthConstraint(.66f), contentScrollView);
        addConstraintToWidget(new LayoutRelativeHeightConstraint(1.0f), contentScrollView);
        manualRecompute();
//        contentScrollView.setDrawDebug(true);
//        contentScrollView.setDebugColor(0x9900ff00);
//        listScrollView.setDrawDebug(true);
//        listScrollView.setDebugColor(0x99ff0000);
        listScrollView.setToTop();
        contentScrollView.setToTop();
        listScrollView.setToRight();
        contentScrollView.setToRight();
//        setDebugColor(0x330000ff);
//        setDrawDebug(true);
    }

    public MKScrollView getListScrollView() {
        return listScrollView;
    }

    public MKScrollView getContentScrollView() {
        return contentScrollView;
    }

    public void setList(IMKWidget widget) {
        listScrollView.addWidget(widget);
    }

    public void setContent(IMKWidget widget) {
        contentScrollView.addWidget(widget);
    }

}

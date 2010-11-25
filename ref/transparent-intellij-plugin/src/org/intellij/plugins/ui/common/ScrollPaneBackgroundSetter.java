// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   cf.java

package org.intellij.plugins.ui.common;

import org.intellij.plugins.ui.common.SimpleScrollPane;

import java.awt.Component;
import javax.swing.JViewport;

// Referenced classes of package com.intellij.ui:
//            gb

class ScrollPaneBackgroundSetter implements Runnable
{

    ScrollPaneBackgroundSetter(SimpleScrollPane scrollPane)
    {
        super();
        this.scrollPane = scrollPane;
    }

    public void run()
    {
        Component component = scrollPane.getViewport().getView();
        if(component != null)
            scrollPane.getViewport().setBackground(component.getBackground());
    }

    private final SimpleScrollPane scrollPane;
}

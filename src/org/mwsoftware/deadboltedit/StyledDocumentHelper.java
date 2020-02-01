/*
 ****************************************************************************
 * Copyright (C) 2018   Michael Wright   All Rights Reserved                *
 *                                                                          *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.            *
 *                                                                          *
 * This Source Code Form is subject to the terms of the                     *
 * Mozilla Public License, v.2.0. If a copy of the MPL was not              *
 * distributed with this file, You can obtain one at                        *
 * http://mozilla.org/MPL/2.0/.                                             *
 *                                                                          *
 * This code is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or    *
 * FITNESS FOR A PARTICULAR PURPOSE.  See the Mozilla Public License,       *
 * version 2 for more details (a copy is included in the LICENSE.txt file   *
 * that accompanied this code).                                             *
 *                                                                          *
 *                                                                          *
 * DefaultStyledDocumentHelper.java - A helper class for                    *
 *                                    DefaultStyledDocument                 *
 *                                                                          *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.FontMetrics;

public class StyledDocumentHelper {

    private static int MaxTabsPerLine = 100;
    
    public StyledDocumentHelper() 
    {
        //
    }
    public void setTabs( JTextPane textPane, int charactersPerTab ) {
        FontMetrics fm = textPane.getFontMetrics( textPane.getFont() );
        int charWidth = fm.charWidth( 'w' );
        int tabWidth = charWidth * charactersPerTab;

        TabStop[] tabs = new TabStop[MaxTabsPerLine];

        for (int j = 0; j < tabs.length; j++)
        {
           int tab = j + 1;
           tabs[j] = new TabStop( tab * tabWidth );
        }

        TabSet tabSet = new TabSet(tabs);
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setTabSet(attributes, tabSet);
        int length = textPane.getDocument().getLength();
        textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);          
    }
}

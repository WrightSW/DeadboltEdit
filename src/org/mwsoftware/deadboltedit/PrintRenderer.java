/*
 ****************************************************************************
 * Copyright (C) 2012-2018   Michael Wright   All Rights Reserved           *
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
 * PrintRenderer.java - Renders text from a DefaultStyledDocument for       *
 *                      printing.                                           *
 *                                                                          *
 *                                                                          *
 * This class renders a Text Document for printing.  It uses a JTextPane    *
 * as a container to make the Document printable.                           *
 *                                                                          *
 * Usage:                                                                   *
 *     PrintRenderer pr = new PrintRenderer(Font font, int tabSize);        *
 *                        font =    Font to be used for printing.           *
 *                                  if null, will use default font for      *
 *                                  DefaultStyledDocument.                  *
 *                        tabSize = No. of spaces for Tab expansion. Actual *
 *                                 spacing is dependent on the font.        *
 *                                 if set to zero, will use default value   *
 *                                 DefaultStyledDocument.                   *
 *     pr.printText( String text );                                         *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.awt.Font;
import java.awt.Color;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;


public class PrintRenderer {
    protected JTextPane textPane;
    
    public PrintRenderer(Font font, int tabSize) {
        textPane = new JTextPane();
        textPane.setForeground(Color.black);
        textPane.setBackground(Color.white);
        if (font != null){
            textPane.setFont( font );
        }
        if (tabSize != 0 ) {
            //    Dependence: Tab Size must be set AFTER setting font, FontMetrics are used
            //    to calculate tab spacing
            Document printableDoc = textPane.getDocument();
            if ( printableDoc instanceof DefaultStyledDocument ) {
                if ( (tabSize < 1) || (tabSize > PgmUtils.MAX_TABSIZE) ) {
                    System.err.println("Error: PrintRenderer.setTabSize() - tab.size out of range, using DefaultStyledDocument default.");
                }
                StyledDocumentHelper docHelper = new StyledDocumentHelper();
                docHelper.setTabs(textPane, tabSize);
                printableDoc = null;
            }
            else {
                System.err.println("Error: PrintRenderer.setTabSize() - Cannot set tab size - Default Document type not DefaultStyledDocument.\n");
            }
        }
    }
        
    public void printText(String text) {
        textPane.setText( text );
        print();
    }
    
    private void print() {
        PrinterJob prtJob = PrinterJob.getPrinterJob();
        MessageFormat header = new MessageFormat("");
        MessageFormat footer = new MessageFormat("- {0} -");
        if ( prtJob.printDialog() ) {
            prtJob.setPrintable( textPane.getPrintable(header, footer) );
            try {
                prtJob.print();
            }
            catch (PrinterException printerException) {
                System.out.println("Error Printing Document");
            }
        }
    }
}

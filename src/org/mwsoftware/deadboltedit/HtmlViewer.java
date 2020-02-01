/*
 ****************************************************************************
 * Copyright (C) 2012-2014   Michael Wright   All Rights Reserved           *
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
 * HtmlViewer.java - Basic HTML viewer, created for use as a viewer for     *
 *      program help written in HTML.  Can also be used as stand-alone HTML *
 *      viewer.                                                             *
 *                                                                          *
 *                                                                          *
 * Uses JEditorPane, HTMLEditorKit, StyleSheet, and JFrame.                 *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;

public class HtmlViewer extends JFrame implements HyperlinkListener {
    public HtmlViewer(URL url) {
        this(url, null, 600, 600, true);
    }

    public HtmlViewer(URL url, String title, int Width, int Heighth, boolean exitOnClose) {
        this(url, title, Width, Heighth, exitOnClose, null);
    }
    
    public HtmlViewer(URL url, String title, int Width, int Heighth, boolean exitOnClose, ImageIcon icon) {
        initComponents();
        this.exitWhenClosed = exitOnClose;
        // Optional JFrame title
        if (title != null ) {
            this.setTitle( title );
        }
        // Optional JFrame icon
        if (icon != null ) {
            this.setIconImage( icon.getImage() );
        }
        editorPane.addHyperlinkListener( this );
        // add an html editor kit
        HTMLEditorKit kit = new HTMLEditorKit();
        editorPane.setEditorKit(kit);
        
        // add some styles to the html
        StyleSheet styleSheet = kit.getStyleSheet();
        // Examples ...
        //styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
        //styleSheet.addRule("h1 {color: blue;}");
        //styleSheet.addRule("h2 {color: #ff0000;}");
        //styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");
        
        // create a document, set it on the jeditorpane, then add the html
        Document doc = kit.createDefaultDocument();
        editorPane.setDocument(doc);
        String splashString = "<html>\n" +
            "<body>\n" +
            "<h2>Loading Document ...</h2>\n" +
            "</body>\n";
        editorPane.setText( splashString );

        try {
            editorPane.setPage( url );
        }
        catch (Exception e) {
            System.err.println("HtmlViewer Error: Got exception.");
            e.printStackTrace();
        }
        
        this.setSize(new Dimension(Width, Heighth));
        this.setVisible( true );
    }

    private void buttonCloseActionPerformed() {
        this.thisWindowClosing( null );
    }

    // Important: Following option must be set for JFrame:
    //    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    //    It was added with JFormDesigner.  See initComponents().
    private void thisWindowClosing(WindowEvent e) {
        this.dispose();
        if ( this.exitWhenClosed ) {
            System.exit(0);
        }
    }
    
    // Implement HyperlinkListener.  It is invoked when user clicks on a
    // hyperlink, or moves the mouse onto or off of the link.
    public void hyperlinkUpdate(HyperlinkEvent e) {
        HyperlinkEvent.EventType type = e.getEventType();
        if ( type == HyperlinkEvent.EventType.ACTIVATED ) { // click
            try {
                editorPane.setPage( e.getURL() );      // follow link
            }
            catch (Exception ex ) {
            }
        }
        //else {  HyperlinkEvent.EventType.ENTERED or HyperlinkEvent.EventType.EXITED
        //    System.err.println("Got other HyperlinkEvent ...");
        //}
    }
    
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        buttonClose = new JButton();
        scrollPane1 = new JScrollPane();
        editorPane = new JEditorPane();

        //======== this ========
        setTitle("HTML Viewer");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent e) {
        		thisWindowClosing(e);
        	}
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {10, 0, 5, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

        //---- buttonClose ----
        buttonClose.setText("Close");
        buttonClose.setToolTipText("Close Viewer");
        buttonClose.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		buttonCloseActionPerformed();
        	}
        });
        contentPane.add(buttonClose, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
        	GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
        	new Insets(0, 0, 5, 5), 0, 0));

        //======== scrollPane1 ========
        {

        	//---- editorPane ----
        	editorPane.setEditable(false);
        	scrollPane1.setViewportView(editorPane);
        }
        contentPane.add(scrollPane1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
        	GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        	new Insets(0, 0, 5, 5), 0, 0));
        setSize(500, 600);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JButton buttonClose;
    private JScrollPane scrollPane1;
    private JEditorPane editorPane;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    
    private static boolean exitWhenClosed;

    // public static void main(String[] args) {
        // final String htmlFile = "Resources/test.html";
        // URL testURL = null;
        // testURL = ClassLoader.getSystemClassLoader().getResource( htmlFile );
        // new HtmlViewer( testURL, "Test of HTML Viewer", 600, 600, true );
        // //new HtmlViewer( testURL );
    // }
}

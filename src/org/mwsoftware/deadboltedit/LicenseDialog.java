/*
 ****************************************************************************
 * Copyright (C) 2012-2015   Michael Wright   All Rights Reserved           *
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
 * LicenseDialog.java - A dialog to display a license agreement and allow   *
 *       the user to accept or decline the agreememt.                       *
 *                                                                          *
 * Uses JEditorPane, HTMLEditorKit, StyleSheet, and JDialog.                *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;

// Note: title may be null
public class LicenseDialog extends JDialog implements HyperlinkListener {
	public LicenseDialog(Frame owner, URL url, String title, int Width, int Heighth) {
		super(owner);
		initLicenseDialog(url, title, Width, Heighth);
	}

	public LicenseDialog(Dialog owner, URL url, String title, int Width, int Heighth) {
		super(owner);
		initLicenseDialog(url, title, Width, Heighth);
	}
    
    private void initLicenseDialog(URL url, String title, int Width, int Heighth) {
        initComponents();
        // Optional JFrame title
        if (title != null ) {
            this.setTitle( title );
        }
        // Add HyperlinkListener
        editorPane1.addHyperlinkListener( this );
        //
        // add an html editor kit
        HTMLEditorKit kit = new HTMLEditorKit();
        editorPane1.setEditorKit(kit);
        
        // add some styles to the html
        StyleSheet styleSheet = kit.getStyleSheet();
        // Examples ...
        //styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
        //styleSheet.addRule("h1 {color: blue;}");
        //styleSheet.addRule("h2 {color: #ff0000;}");
        //styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");
        
        // create a document, set it on the jeditorpane, then add the html
        Document doc = kit.createDefaultDocument();
        editorPane1.setDocument(doc);
        String splashString = "<html>\n" +
            "<body>\n" +
            "<h2>Loading License ...</h2>\n" +
            "</body>\n";
        editorPane1.setText( splashString );

        try {
            editorPane1.setPage( url );
        }
        catch (Exception e) {
            System.err.println("LicenseDialog Viewer Error: Got exception loading URL.");
            e.printStackTrace();
        }
        
        this.setSize(new Dimension(Width, Heighth));
        this.licenseAccept = false;
    }
	
	
	private void acceptButtonActionPerformed() {
		this.licenseAccept = true;
		setVisible(false);
	}

	private void declineButtonActionPerformed() {
		this.licenseAccept = false;
		setVisible(false);
	}
	
	public boolean getResult() {
	    return this.licenseAccept;
	}

    
    // Implement HyperlinkListener.  It is invoked when user clicks on a
    // hyperlink, or moves the mouse onto or off of the link.
    public void hyperlinkUpdate(HyperlinkEvent e) {
        HyperlinkEvent.EventType type = e.getEventType();
        if ( type == HyperlinkEvent.EventType.ACTIVATED ) { // click
            try {
                editorPane1.setPage( e.getURL() );      // follow link
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
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		scrollPane1 = new JScrollPane();
		editorPane1 = new JEditorPane();
		buttonBar = new JPanel();
		acceptButton = new JButton();
		declineButton = new JButton();

		//======== this ========
		setTitle("Licensing and Legal Information");
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new GridBagLayout());
				((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

				//======== scrollPane1 ========
				{

					//---- editorPane1 ----
					editorPane1.setEditable(false);
					scrollPane1.setViewportView(editorPane1);
				}
				contentPanel.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

				//---- acceptButton ----
				acceptButton.setText("Accept");
				acceptButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						acceptButtonActionPerformed();
					}
				});
				buttonBar.add(acceptButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- declineButton ----
				declineButton.setText("Decline");
				declineButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						declineButtonActionPerformed();
					}
				});
				buttonBar.add(declineButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(520, 585);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JScrollPane scrollPane1;
	private JEditorPane editorPane1;
	private JPanel buttonBar;
	private JButton acceptButton;
	private JButton declineButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	private boolean licenseAccept;
}

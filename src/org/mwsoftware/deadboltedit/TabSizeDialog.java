/*
 ****************************************************************************
 * Copyright (C) 2012-2016   Michael Wright   All Rights Reserved           *
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
 * TabSizeDialog.java - Dialog to allow selection of tab size setting.      *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Note: this dialog was made Modal by adding second argument (true) to the
 *       Superclass constructors.
 */
public class TabSizeDialog extends JDialog {
	public TabSizeDialog(Frame owner,int maxTabSize) {
		super(owner, true);
		initComponents();
		DialogHelper.addEscapeListener( this );  // Allow Esc to close dialog
		initDialog( 1, maxTabSize );  // Setup Dialog
	}

	public TabSizeDialog(Dialog owner,int maxTabSize) {
		super(owner, true);
		initComponents();
		DialogHelper.addEscapeListener( this ); // Allow Esc to close dialog
		initDialog( 1, maxTabSize );  // Setup Dialog
	}

	private void initDialog( int initialVal, int upperTabSettingLimit ) {
	    // Configure (JSpinner) spinner1
		spinner1.setModel( new SpinnerNumberModel(initialVal, 1, upperTabSettingLimit, 1) );
		getRootPane().setDefaultButton( okButton );
	}

	// setTabSize()
	public void setValue( int tabSize ) {
	    this.tabSizeValue = tabSize;
	    spinner1.setValue( (int) tabSizeValue );
	}

	// getTabSize()
	public int getValue() {
        return (this.tabSizeValue);
	}


	private void okButtonActionPerformed() {
	    int val = (int) spinner1.getValue();
	    if ( val > 0 ) {
	        tabSizeValue = val;
	    }
		setVisible( false );
	}

	private void cancelButtonActionPerformed() {
		setVisible( false );
	}

	private void okButtonKeyPressed() {
		okButtonActionPerformed();
	}

	private void cancelButtonKeyPressed() {
		cancelButtonActionPerformed();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label4 = new JLabel();
		spinner1 = new JSpinner();
		label5 = new JLabel();
		label2 = new JLabel();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle("Set Tab Size");
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new GridBagLayout());
				((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {88, 0, 0, 0};
				((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 29, 0, 0, 0};
				((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
				((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};

				//---- label4 ----
				label4.setText("Tab Size:");
				contentPanel.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 5), 0, 0));
				contentPanel.add(spinner1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label5 ----
				label5.setText("Spaces");
				contentPanel.add(label5, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label2 ----
				label2.setText("<html>Usage Tip: Use a Monospace font for evenly<br>spaced columns and indents.</html>");
				contentPanel.add(label2, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed();
					}
				});
				okButton.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						okButtonKeyPressed();
					}
				});
				buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed();
					}
				});
				cancelButton.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						cancelButtonKeyPressed();
					}
				});
				buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label4;
	private JSpinner spinner1;
	private JLabel label5;
	private JLabel label2;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	//
	private int tabSizeValue;
}

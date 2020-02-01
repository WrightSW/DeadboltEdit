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
 * PasswordDialog.java - Dialog to get password when file is opened.        *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


public class PasswordDialog extends JDialog {
	public PasswordDialog(Frame owner) {
		super(owner, true);
		initComponents();
		DialogHelper.addEscapeListener( this ); // Allow Esc to close Dialog
        // JPasswordField will process CR to invoke DefaultButton if ActionPerformed is NOT overridden.
        getRootPane().setDefaultButton( okButton );
        expectedMD5Verification = null;
	}

	public PasswordDialog(Dialog owner) {
		super(owner, true);
		initComponents();
		DialogHelper.addEscapeListener( this ); // Allow Esc to close Dialog
        // JPasswordField will process CR to invoke DefaultButton if ActionPerformed is NOT overridden.
        getRootPane().setDefaultButton( okButton );
        expectedMD5Verification = null;
	}

	private void okButtonActionPerformed(ActionEvent e) {
	    //System.err.println("Password is : " + new String( passwordField1.getPassword() ) );
	    // Verify password with verification code
	    if (expectedMD5Verification != null ) {
		    String tmpPWVerificationCode = PgmUtils.MD5Hash( passwordField1.getPassword() );
		    if (tmpPWVerificationCode.equals( expectedMD5Verification ) ) {
		        this.setVisible( false );
		        return;
		    }
		    else {
		        // error dialog
                String warningMsg = "Password is not correct for this file.";
                JOptionPane warningPane = new JOptionPane();
                warningPane.showMessageDialog(null, warningMsg, "Incorrect Password", JOptionPane.WARNING_MESSAGE);
                return;     // try again
		    }
		}
		else {
		    // Accept password with no verification
		    this.setVisible( false );
		    return;
		}
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		//
		passwordField1.setText("");
		this.setVisible( false );
		return;
	}

	public char[] getPasswd() {
	    return passwordField1.getPassword();
    }

    // Set the MD5 verification hash for the anticipated password
    // (Enables password verification)
    public void setExpectedMD5Verification(String pwVerificationCode) {
		expectedMD5Verification = pwVerificationCode;
    }

    // setFrameTitle() - Set the title string of the dialog frame.
    //                   Overides the default string.
    public void setFrameTitle(String title) {
        this.setTitle( title );
    }

    private void okButtonKeyPressed(KeyEvent e) {
    	//
    	okButtonActionPerformed( null );
    }

    private void cancelButtonKeyPressed(KeyEvent e) {
    	//
    	cancelButtonActionPerformed ( null );
    }

    private void thisWindowClosing() {
        //
    	cancelButtonActionPerformed ( null );
    }

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label2 = new JLabel();
		passwordField1 = new JPasswordField();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle("Password");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing();
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new GridBagLayout());
				((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {0, 333, 0, 0};
				((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
				((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
				((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

				//---- label2 ----
				label2.setText("Password For File:");
				contentPanel.add(label2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				contentPanel.add(passwordField1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
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
						okButtonActionPerformed(e);
					}
				});
				okButton.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						okButtonKeyPressed(e);
					}
				});
				buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				cancelButton.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						cancelButtonKeyPressed(e);
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

    private String  expectedMD5Verification;    // verification code of anticipated password

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label2;
	private JPasswordField passwordField1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

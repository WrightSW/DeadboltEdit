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
 *                                                                          *
 * WindowStyleDialog.java - Choose GUI Look and Feel.                       *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class WindowStyleDialog extends JDialog {
    public WindowStyleDialog(Frame owner) {
        super(owner);
        initComponents();
        DialogHelper.addEscapeListener( this ); // Allow Esc to close Dialog
    }

    public WindowStyleDialog(Dialog owner) {
        super(owner);
        initComponents();
        DialogHelper.addEscapeListener( this ); // Allow Esc to close Dialog
    }

    private void okButtonActionPerformed() {
        this.setVisible( false );
        return;
    }

    private void cancelButtonActionPerformed() {
        windowStyleProperty = null;
        this.setVisible( false );
        return;
    }
    
    // Set the system name label (overide generic label)
    public void setSystemNameLabel(String label) {
        radioButtonSystem.setText( label );
        return;
    }
    
    // set radio button and property to current program setting 
    public void setSelection(String winStyle) {
        if ( "System".equals(winStyle) ) {
            windowStyleProperty = "System";
            radioButtonSystem.setSelected( true );
        }
        else {
            windowStyleProperty = "Metal";
            radioButtonCrossPlatform.setSelected( true );
        }
    }
    
    public String getSelection() {
        return windowStyleProperty;
    }

    private void okButtonKeyPressed() {
    	okButtonActionPerformed();
    	return;
    }

    private void cancelButtonKeyPressed() {
    	cancelButtonActionPerformed();
    	return;
    }

    private void radioButtonSystemActionPerformed(ActionEvent e) {
    	windowStyleProperty = "System";
    	return;
    }

    private void radioButtonCrossPlatformActionPerformed(ActionEvent e) {
    	windowStyleProperty = "Metal";
    	return;
    }

    private void thisWindowClosing() {
        cancelButtonActionPerformed();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        radioButtonSystem = new JRadioButton();
        radioButtonCrossPlatform = new JRadioButton();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle("Window Style");
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
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
                ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
                ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
                ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
                ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- label1 ----
                label1.setText("Window Style Selection");
                contentPanel.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.NONE,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- radioButtonSystem ----
                radioButtonSystem.setText("System (Mac OS or Windows)");
                radioButtonSystem.setSelected(true);
                radioButtonSystem.setToolTipText("Native window style for this system (Windows or Mac OS)");
                radioButtonSystem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        radioButtonSystemActionPerformed(e);
                    }
                });
                contentPanel.add(radioButtonSystem, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- radioButtonCrossPlatform ----
                radioButtonCrossPlatform.setText("Metal (Cross-Platform)");
                radioButtonCrossPlatform.setToolTipText("Cross platform window style");
                radioButtonCrossPlatform.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        radioButtonCrossPlatformActionPerformed(e);
                    }
                });
                contentPanel.add(radioButtonCrossPlatform, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
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
                    @Override
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
                    @Override
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
        setSize(350, 205);
        setLocationRelativeTo(getOwner());

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(radioButtonSystem);
        buttonGroup1.add(radioButtonCrossPlatform);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JRadioButton radioButtonSystem;
    private JRadioButton radioButtonCrossPlatform;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    private String windowStyleProperty = null;
}

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
 * FindDialog.java - Dialog for text search.                                *
 *                                                                          *
 * Note: This dialog is non-modal, it may be kept open and used along with  *
 *       the edit window.                                                   *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class FindDialog extends JDialog {
    public FindDialog(Frame owner, TextSearcher tSearcher) {
        super(owner);
        initComponents();
        DialogHelper.addEscapeListener( this ); // Allow Esc to close Dialog
        this.doDialogSetup( tSearcher );
    }

    public FindDialog(Dialog owner, TextSearcher tSearcher) {
        super(owner);
        initComponents();
        DialogHelper.addEscapeListener( this ); // Allow Esc to close Dialog
        this.doDialogSetup( tSearcher );
    }

    // doDialogSetup(): Called from constructors.
    private void doDialogSetup( TextSearcher ts ) {
        this.textSearcher = ts;
        textSearcher.setDialogLock();
        // following allows persistent strings and case-match setting
        String ss;
        if ( (ss=textSearcher.getSearchString()) != null ) {
            textFieldFind.setText( ss );
        }
        if ( textSearcher.isMatchCaseSet() ) {
            checkBoxMatchCase.setSelected( true );
        } else {
            checkBoxMatchCase.setSelected( false );
        }
    }

    private void setStatus( String msg ) {
        statusLine.setText( msg );
        return;
    }

    private void clearStatus( ) {
        statusLine.setText( "" );
        return;
    }

    private void buttonFindActionPerformed() {
        //
        clearStatus();
        String searchStr = textFieldFind.getText();
        if ( searchStr.length() <= 0 ) {
            setStatus( "Please enter search text." );
            buttonFindNext.setEnabled( false );
            return;
        }

        boolean caseMatch = checkBoxMatchCase.isSelected();
        int result;
        if ( caseMatch ) {
            result = textSearcher.findMatchCase( searchStr );
        } else {
            result = textSearcher.findIgnoreCase( searchStr );
        }
        if ( result >= 0 ) {
            setStatus("Found match #" + textSearcher.getMatchCount());
            if ( textSearcher.canContinueSearch() ) {
                buttonFindNext.setEnabled( true );
                if ( getRootPane().getDefaultButton() != null ) {
                    getRootPane().setDefaultButton( buttonFindNext );
                } else {
                    buttonFindNext.requestFocusInWindow();
                }
            } else {
                if ( getRootPane().getDefaultButton() != null ) {
                    getRootPane().setDefaultButton( buttonFind );
                }
                buttonFindNext.setEnabled( false );
            }
        } else {
            setStatus("Search text not found.");
            buttonFindNext.setEnabled( false );
        }
    }

    private void buttonFindNextActionPerformed() {
        //
        boolean caseMatch = checkBoxMatchCase.isSelected();
        int result;
        if ( caseMatch ) {
            result = textSearcher.findNextMatchCase();
        } else {
            result = textSearcher.findNextIgnoreCase();
        }
        if ( result >= 0 ) {
            setStatus("Found match #" + textSearcher.getMatchCount() );
            if ( !textSearcher.canContinueSearch() ) {
                if ( getRootPane().getDefaultButton() != null ) {
                    getRootPane().setDefaultButton( buttonFind );
                }
                buttonFindNext.setEnabled( false );
            }
        } else {
            setStatus("Search reached end-of-text. Total found: " + textSearcher.getMatchCount() );
            if ( getRootPane().getDefaultButton() != null ) {
                getRootPane().setDefaultButton( buttonFind );
            } else {
                // put focus back in textFieldFind
                textFieldFind.requestFocusInWindow();
            }
            buttonFindNext.setEnabled( false );
        }
    }

    private void buttonCloseActionPerformed() {
        //
        textSearcher.resetDialogLock();
        this.dispose();
    }

    // Occurs when key is selected and activated with keyboard
    private void buttonFindKeyPressed() {
        //
        this.buttonFindActionPerformed();
    }

    // Occurs when key is selected and activated with keyboard
    private void buttonFindNextKeyPressed() {
        //
        this.buttonFindNextActionPerformed();
    }

    private void buttonCloseKeyPressed() {
        //
        this.buttonCloseActionPerformed();
    }

    private void thisWindowClosing() {
        //
        this.buttonCloseActionPerformed();
    }

    private void thisWindowGainedFocus() {
        //
        textFieldFind.requestFocusInWindow();
    }

    private void textFieldFindFocusGained() {
        //
        // Set DefaultButton: Responds to Carriage Return while focus is in JTextField, if
        // ActionPerformed for JTextField is NOT overridden.
        getRootPane().setDefaultButton( buttonFind );
    }

    private void textFieldFindFocusLost() {
        //
        // Remove DefaultButton
        getRootPane().setDefaultButton( null );
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        textFieldFind = new JTextField();
        panel2 = new JPanel();
        checkBoxMatchCase = new JCheckBox();
        panel1 = new JPanel();
        buttonFind = new JButton();
        buttonFindNext = new JButton();
        buttonClose = new JButton();
        statusLine = new JLabel();

        //======== this ========
        setTitle("Find");
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thisWindowClosing();
            }
        });
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                thisWindowGainedFocus();
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {10, 67, 333, 5, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {10, 0, 0, 0, 0, 0, 10, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

        //---- label1 ----
        label1.setText("Find:");
        contentPane.add(label1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- textFieldFind ----
        textFieldFind.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textFieldFindFocusGained();
            }
            @Override
            public void focusLost(FocusEvent e) {
                textFieldFindFocusLost();
            }
        });
        contentPane.add(textFieldFind, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //======== panel2 ========
        {
            panel2.setLayout(new GridBagLayout());
            ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {72, 0, 0};
            ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
            ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

            //---- checkBoxMatchCase ----
            checkBoxMatchCase.setText("Match Case");
            checkBoxMatchCase.setSelected(true);
            panel2.add(checkBoxMatchCase, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(panel2, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //======== panel1 ========
        {
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {74, 0, 0, 0, 0, 108, 0, 0};
            ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};
            ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

            //---- buttonFind ----
            buttonFind.setText("Find");
            buttonFind.setToolTipText("Find from beginning of text");
            buttonFind.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    buttonFindActionPerformed();
                }
            });
            buttonFind.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    buttonFindKeyPressed();
                }
            });
            panel1.add(buttonFind, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- buttonFindNext ----
            buttonFindNext.setText("Find Next");
            buttonFindNext.setEnabled(false);
            buttonFindNext.setToolTipText("Find next occurance");
            buttonFindNext.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    buttonFindNextActionPerformed();
                }
            });
            buttonFindNext.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    buttonFindNextKeyPressed();
                }
            });
            panel1.add(buttonFindNext, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- buttonClose ----
            buttonClose.setText("Close");
            buttonClose.setToolTipText("Close this dialog");
            buttonClose.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    buttonCloseActionPerformed();
                }
            });
            buttonClose.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    buttonCloseKeyPressed();
                }
            });
            panel1.add(buttonClose, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 5), 0, 0));
        }
        contentPane.add(panel1, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- statusLine ----
        statusLine.setForeground(Color.blue);
        contentPane.add(statusLine, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JTextField textFieldFind;
    private JPanel panel2;
    private JCheckBox checkBoxMatchCase;
    private JPanel panel1;
    private JButton buttonFind;
    private JButton buttonFindNext;
    private JButton buttonClose;
    private JLabel statusLine;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    private TextSearcher textSearcher;
}

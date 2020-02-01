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
 * DeadboltEdit.java - Primary class for DeadboltEdit.                      *
 *                                                                          *
 * DeadboltEdit is an application to securely edit text documents and save  *
 * them as encrypted files.                                                 *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;   // Document, PlainDocument

import java.awt.FileDialog;
import javax.swing.event.*;  //DocumentListener interface, DocumentEvent
import java.io.*;
import java.util.*;     // ex. java.util.Arrays.fill()
import org.apache.commons.ssl.*;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;


public class DeadboltEdit extends JFrame implements DocumentListener, UndoableEditListener, MacOSXOpenFileAdapterCallback {
    //
    public DeadboltEdit( File fileToOpen ) {
        openEditors.add( this );    // Add our new editor to tracking Vector
        editorID = editorCount++;
        openEditorCount++;
        if ( editorID == 0 ) {
            String windowStyle = settings.getProperty("window.style");
            try {
                if ( "System".equals(windowStyle) ) {
                    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
                }
                else if ( "Metal".equals(windowStyle) ) {
                    UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
                }
                else {
                    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
                }
            }
            catch (Exception UIMe) {
                System.err.println("DeadboltEdit Error: Exception attempting to set Window Style to: " + windowStyle);
            }
        }
        SwingUtilities.updateComponentTreeUI( this );
        initComponents();
        initEditorPopupMenu();
        //  MacOS Only: Set a QuitStrategy to catch the Quit event from either
        //              the Apple menu or Dock
        if (runtimeSettings.getProperty("system.type").equals(PgmUtils.SYS_MACOSX)) {
            if (macOSXadapter != null ) {
                macOSXadapter.setQuitStrategy();
            }
        } else {
        // Other OS (Windows, Linux/UNIX) : Set an icon for the main JFrame
            java.net.URL iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/DeadboltEdit32x32.png");
            this.setIconImage( new ImageIcon( iconURL).getImage() );
        }
        //
        // Set toolbar button icons from resources in jar file
        java.net.URL iconURL;
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/new-win.png");
        toolbarNew.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/open.png");
        toolbarOpen.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/save.png");
        toolbarSave.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/close.png");
        toolbarClose.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/printer.png");
        toolbarPrint.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/cut_red.png");
        toolbarCut.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/copy.png");
        toolbarCopy.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/paste.png");
        toolbarPaste.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/find.png");
        toolbarFind.setIcon(new ImageIcon(iconURL));
        iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/Silk16x16/help.png");
        toolbarHelp.setIcon(new ImageIcon(iconURL));
        //
        // Add our DocumentListener to the editor.  This signals edit changes
        editorPane.getDocument().addDocumentListener(this);
        //
        // Create the undo manager
        undoManager = new UndoManager();
        editorPane.getDocument().addUndoableEditListener(undoManager);
        undoManager.setLimit( 50 );  // default is 100, we're small and lightweight
        // Add also our UndoableEditListener, to keep the Undo menu item refreshed
        editorPane.getDocument().addUndoableEditListener( this );
        //
        //
        file = new FileInfo();  // Our file-related info
        editorPane.setText("");
        startingHashCode = editorPane.getText().hashCode();
        startingTextLength = editorPane.getText().length();
        this.undoManager.discardAllEdits();  // Ensure the Undo/Redo memory is clear
        file.setFullpath("");
        initActiveDirectory();  // set activeDirectory, for file dialogs
        logger.out("Opening editor: " + Integer.toString(editorID) );
        file.setBasename( "Edit" + Integer.toString(editorID + 1) );
        file.setExt( ".ctxt" );
        file.setPassword( null );
        menuFilePasswordChange.setEnabled(false);
        showEditStatus( false, "" );
        // Restore window to last location and size if we have saved values
        if ( editorID == 0 && settings.containsKey("window.last.X")  ) {
            int x = Integer.parseInt( settings.getProperty("window.last.X") );
            int y = Integer.parseInt( settings.getProperty("window.last.Y") );
            int h = Integer.parseInt( settings.getProperty("window.last.Height") );
            int w = Integer.parseInt( settings.getProperty("window.last.Width") );
            Rectangle winDimensions = new Rectangle(x, y, w, h);
            this.setBounds( winDimensions );
        }
        else {
            this.setSize(600, 500);
            setLocationRelativeTo(getOwner());
        }
        applySettings();
        fileStatusField.setText("");
        editStatusField.setText("");
        setVisible( true );
        if ( editorID == 0 && "false".equals(settings.getProperty("license.accepted")) ) {
            // Ask user to view and accept license.
            java.net.URL licenseURL = ClassLoader.getSystemClassLoader().getResource( "Resources/license.html" );
            LicenseDialog licDialog = new LicenseDialog(this, licenseURL, null, 750, 600);
            licDialog.setVisible( true );
            boolean licenseAccepted = licDialog.getResult();
            licDialog.dispose();
            if ( licenseAccepted ) {
                settings.setProperty("license.accepted", "true");
            }
            else {
                quitApp( null );
            }
        }
        if (runtimeSettings.getProperty("system.type").equals(PgmUtils.SYS_MACOSX)) {
            if ( (editorID == 0) && (macOSXadapter != null)) {
                // On MacOSX, if app is launched on an associated file, it is passed by
                // way of an OpenFileEvent.
                // Files passed by OpenFileEvent during app launch will overide command-line arg.
                File f = macOSXadapter.getFile();
                if (f != null ) {
                    fileToOpen = f;
                    logger.out( "Start file (MacOSX OpenFileEvent): " + fileToOpen );
                }
                // Register a MacOSXOpenFileAdapter callback to get any remaining or subsequent
                // files, which will open a new window for each.
                macOSXadapter.addCallback( this );
            }
        }
        if (fileToOpen != null ) {
            loadEncryptedFile( fileToOpen );
        }
        editorPane.requestFocusInWindow();
    }

    // ---- DocumentListener Methods --------------------------------------
    public void insertUpdate(DocumentEvent e) {
        processDocumentEvent("insert event");
    }
    public void removeUpdate(DocumentEvent e) {
        processDocumentEvent("remove event");
    }
    public void changedUpdate(DocumentEvent e) {
        processDocumentEvent("change event");
    }
    // ---- End: DocumentListener Methods ---------------------------------
    
    private void processDocumentEvent(String type) {
        //System.out.println("DocumentListener: " + type);
        if ( unsavedEditorChanges() ) {
            if (! documentChangeStatusShown ) {
                showEditStatus( true, "(Edited)" ); // show text modified
            }
        }
        else {
            if ( documentChangeStatusShown ) {
                showEditStatus( false, "" ); // remove text modified
            }
        }
        // Redo becomes unavailable after an edit
        if ( redoInformationShown ) {
            menuEditRedo.setText("Redo");
            redoInformationShown = false;
        }
    }
    
    // ---- UndoableEditListener Methods ----------------------------------
    // undoableEditHappened() - Used to keep the "Undo" & "Redo" menu items
    // refreshed
    public void undoableEditHappened(UndoableEditEvent evt) {
        UndoableEdit edit = evt.getEdit();
        // if event is un-doable or re-doable, modify the description of the Undo
        // and/or Redo menu items in Edit Menu.
        if ( edit.canUndo() ) {
            String undoMenuDesc = edit.getUndoPresentationName();
            menuEditUndo.setText(undoMenuDesc);
        }
        else {
            menuEditUndo.setText("Undo");
        }
    }
    // ---- End: UndoableEditListener Methods -----------------------------

    // ---- MacOSXOpenFileAdapterCallback ---------------------------------
    public void MacOSXOpenFileAdapterListener() {
        logger.out("MacOSXOpenFileAdapterListener(): Notification event. editorID: " + Integer.toString(editorID));
        File fileToOpen = null;
        while ( (fileToOpen = macOSXadapter.getFile()) != null ) {
            new DeadboltEdit( fileToOpen );
        }
    }
    // ---- End MacOSXOpenFileAdapterCallback -----------------------------

    //
    // Important: Following option must be set for JFrame:
    //    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    //    It was added with JFormDesigner.  See initComponents().
    //
    public void mainWindowClosing(WindowEvent e) {
        if (openEditors.get(editorID) == null) {    // Possible when quitting with Cmd-Q on MacOS
            return; // ignore
        }
        // Check for text changes
        if ( unsavedEditorChanges() ) {
            int response;
            JOptionPane optionPane = new JOptionPane();
            String msg = "Save changes for \"" + file.getBasename() + file.getExt() + "\"?";
            response = optionPane.showConfirmDialog(null, msg, "Warning: Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            switch ( response ) {
            case JOptionPane.CANCEL_OPTION:
                return; // Cancell the Window Close
            case JOptionPane.YES_OPTION:
                if (file.getExt().equals(".ctxt") ) {
                    fileSave( null );
                }
                else {
                    fileSavePlaintext( null );
                }
                break;
            default:
                break;  // Proceed with Window Close and lose edits ...
            }
        }
        if ( editorID == 0 ) {
                Rectangle winDimensions = this.getBounds();
                int x = (int) winDimensions.getX();
                int y = (int) winDimensions.getY();
                int w = (int) winDimensions.getWidth();
                int h = (int) winDimensions.getHeight();
                settings.setProperty( "window.last.X", Integer.toString(x) );
                settings.setProperty( "window.last.Y", Integer.toString(y) );
                settings.setProperty( "window.last.Width", Integer.toString(w) );
                settings.setProperty( "window.last.Height", Integer.toString(h) );
        }
        openEditors.set(editorID, null);
        openEditorCount--;
        logger.out("Closing editor: " + Integer.toString(editorID) );
        if ( openEditorCount <= 0) {        // If this is the last window to close ...
            if (activeDirectory != null ) {
                settings.setProperty( "active.directory", activeDirectory );
            }
            PgmUtils.saveSettings( settings, runtimeSettings.getProperty("settings.dir"), runtimeSettings.getProperty("settings.file") );
            logger.close();
            System.exit(0);
        }
        else {
            //this.dispose();   // Preferable
            // -- Workaround -- for JSE 1.7 bug on Mac OS that causes the Screen Menubar to disappear
            //                  when one window closes. Only happens if using Mac-style windows.
            if (runtimeSettings.getProperty("system.type").equals(PgmUtils.SYS_MACOSX)
                && settings.getProperty("window.style").equals("System") ) {
                this.setVisible( false );
            }
            else {
                this.dispose();
            }
        }
    }
    
    // resetUndoHistory() = Clear Undo history, and reset menu descriptions
    private void resetUndoHistory() {
        this.undoManager.discardAllEdits();     // Clear the Undo/Redo memory
        menuEditUndo.setText("Undo");
        menuEditRedo.setText("Redo");
    }

    // Apply settings that are applicable to an editor instance
    private void applySettings() {
        String valueStr = null;
        // -- Text color
        valueStr = settings.getProperty("text.color");
        if (valueStr != null ) {
            try {
                Color decodedColor = new Color(Integer.parseInt( valueStr ) );
                editorPane.setForeground( decodedColor );
            }
            catch (Exception e) {       // Bad value from user settings file
                System.err.println("Error: applySettings() - Invalid text.color, using default");
                editorPane.setForeground( PgmUtils.FALLBACK_DEFAULT_FOREGROUND_COLOR );
                settings.remove("text.color");      // avoid the problem next time
            }
        }
        else {      // Possible problem with resource file defaultSettings.dat
            System.err.println("Error: applySettings() - NULL value for text.color, using default.");
            editorPane.setForeground( PgmUtils.FALLBACK_DEFAULT_FOREGROUND_COLOR );
        }
        // -- Background color
        valueStr = settings.getProperty("background.color");
        if (valueStr != null ) {
            try {
                Color decodedColor = new Color(Integer.parseInt( valueStr ) );
                editorPane.setBackground( decodedColor );
            }
            catch (Exception e) {       // Bad value from user settings file
                System.err.println("Error: applySettings() - Invalid background.color, using default");
                editorPane.setBackground( PgmUtils.FALLBACK_DEFAULT_BACKGROUND_COLOR );
                settings.remove("background.color");      // avoid the problem next time
            }
        }
        else {      // Possible problem with resource file defaultSettings.dat
            System.err.println("Error: applySettings() - NULL value for background.color, using default.");
            editorPane.setBackground( PgmUtils.FALLBACK_DEFAULT_BACKGROUND_COLOR );
        }
        // -- Text Font
        String fontName = settings.getProperty("text.font.name");
        String fontStyle = settings.getProperty("text.font.style");
        String fontSize = settings.getProperty("text.font.size");
        if ( (fontName != null) && (fontStyle != null) && (fontSize != null) ) {
            try {
                Font decodedFont = new Font(fontName, Integer.parseInt(fontStyle), Integer.parseInt(fontSize));
                editorPane.setFont( decodedFont );
            }
            catch (Exception e) {       // Bad value from user settings file
                System.err.println("Error: applySettings() - Invalid font value, using default");
                this.editorPane.setFont( PgmUtils.FALLBACK_DEFAULT_TEXT_FONT );
                settings.remove("text.font.name");      // avoid the problem next time
                settings.remove("text.font.style");
                settings.remove("text.font.size");
            }
        }
        else {      // Possible problem with resource file defaultSettings.dat
            System.err.println("Error: applySettings() - NULL value for font parameter, using default.");
            this.editorPane.setFont( PgmUtils.FALLBACK_DEFAULT_TEXT_FONT );
        }
        // -- Tab Size
        //    Dependence: Tab Size must be set AFTER setting font, FontMetrics are used to calculate tab spacing!
        Document editorDoc = editorPane.getDocument();
        if ( editorDoc instanceof DefaultStyledDocument ) {
            valueStr = settings.getProperty("tab.size");
            int tabSize;
            try {
                tabSize = Integer.parseInt( valueStr );
            }
            catch (NumberFormatException e) {       // Bad value from user settings file
                System.err.println("Error: applySettings() - Invalid tab.size, using default");
                tabSize = PgmUtils.FALLBACK_DEFAULT_TAB_SIZE;
                settings.remove("tab.size");      // avoid the problem next time
            }
            if ( (tabSize < 0) || (tabSize > PgmUtils.MAX_TABSIZE) ) {
                System.err.println("Error: applySettings() - tab.size out of range, using default.");
                tabSize = PgmUtils.FALLBACK_DEFAULT_TAB_SIZE;
                settings.remove("tab.size");      // avoid the problem next time
            }
            StyledDocumentHelper docHelper = new StyledDocumentHelper();
            docHelper.setTabs(editorPane, tabSize);
            editorDoc = null;
            this.revalidate();
        }
        else {
            System.err.println("Error: Cannot set tab size - Editor Document type not DefaultStyledDocument.\n");
        }
        // -- Line Wrap: Enable/Disable line-wrap.  Scrolling is enables when line-wrap
        //               is disabled.
        if ( "1".equals( settings.getProperty("line.wrap.enable")) ) {
            menuSettingsLineWrapChkbox.setSelected(true);
            scrollPane1.setViewportView( editorPane );
            // Vertical scrolling speed - set faster rate than default
            scrollPane1.getVerticalScrollBar().setUnitIncrement(16);

        }
        else {
            menuSettingsLineWrapChkbox.setSelected(false);
            scrollPane1.setViewportView( noWrapPanel );
            // Scrolling speed - set faster rate than default
            scrollPane1.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane1.getHorizontalScrollBar().setUnitIncrement(14);
            noWrapPanel.add( editorPane );
        }
        // -- Debug Log.  Log is enabled/disabled elsewhere, we just adjust the Settings menu
        //                checkbox here.
        if ( "1".equals( settings.getProperty("debug.log.enable")) ) {
            MenuSettingsDebugLogChkbox.setSelected(true);
        }
        else {
            MenuSettingsDebugLogChkbox.setSelected(false);
        }
    }

    // Propagate settings to all open editors - ask each editor to update
    private void applySettingsAllWindows() {
        String windowStyle = settings.getProperty("window.style");
        try {
            if ( "System".equals(windowStyle) ) {
                UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            }
            else if ( "Metal".equals(windowStyle) ) {
                UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
            }
            else {
                UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            }
        }
        catch (Exception UIMe) {
            System.err.println("DeadboltEdit Error: Exception attempting to set Window Style to: " + windowStyle);
        }
        DeadboltEdit editor;
        for (int i = 0; i < openEditors.size(); i++) {
            editor = (DeadboltEdit) openEditors.get(i);
            if ( editor != null ) {
                SwingUtilities.updateComponentTreeUI( editor );
                editor.applySettings( );
            }
        }
        // Debug Log: logger is static, so we eneble centrally here for all open windows.
        // applySettings() handles setting of menu checkbox for each window
        if ( "1".equals( settings.getProperty("debug.log.enable")) ) {
            if ( !logger.isOpen() ) {
                logger.open( runtimeSettings.getProperty("log.file") );
            }
        }
        else {
            if ( logger.isOpen() ) {
                logger.close();
            }
        }
    }

    // Check for unsaved changes in editor
    private boolean unsavedEditorChanges() {
        int editorHashCode;
        int editorTextLength;
        // Check for text changes
        editorHashCode = this.editorPane.getText().hashCode();
        editorTextLength = this.editorPane.getText().length();
        if ((editorHashCode == startingHashCode) && (startingTextLength == editorTextLength) )
            return(false);
        else
            return(true);
    }

    // showEditStatus(boolean documentChangeStatus, String editStatusMsg)
    //   1. Show filename in editor frame title.
    //   2. If bufferModified is TRUE:
    //      A. Show an asterisk at the beginning of the title
    //      B. Show edit state in editStatusField (bottom of frame)
    //
    // Check (boolean) documentChangeStatusShown before calling to avoid frequent
    // re-writing of the same status
    //
    private void showEditStatus(boolean documentChangeStatus, String editStatusMsg) {
        if (documentChangeStatus) {
            this.setTitle("*DeadboltEdit - " + file.getBasename() + file.getExt() );
            //editStatusField.setText("(Edited)");
            documentChangeStatusShown = true;      // Remember our currently shown state
        }
        else {
            this.setTitle("DeadboltEdit - " + file.getBasename() + file.getExt() );
            //editStatusField.setText("");
            documentChangeStatusShown = false;     // Remember our currently shown state
        }
        // show edit status message
        if (editStatusMsg != null ) {
            editStatusField.setText( editStatusMsg );
        }
    }

    // loadEncryptedFile() - Read and decrypt DeadboltEdit encrypted file
    //
    private void loadEncryptedFile( File f ) {
        if ( !f.exists() || !f.canRead() ) {
            String warningMsg = "File \"" + f.getPath() + "\" does not exist, or is not readable.\n\n" +
                "(File may have been renamed, moved, or deleted?)";
            JOptionPane warningPane = new JOptionPane();
            warningPane.showMessageDialog(null, warningMsg, "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // check for correct file suffix
        String tmpFileName = f.getName();   // filename
        int dotIndex = tmpFileName.lastIndexOf(".");
        if (tmpFileName.endsWith(".ctxt") && dotIndex >= 0 ) {
            file.setBasename( tmpFileName.substring(0, dotIndex) );
            file.setExt( tmpFileName.substring(dotIndex) );
            String tmpDir = f.getParent();
            if ( tmpDir != null ) {
                file.setFullpath( tmpDir + File.separator + file.getBasename() + file.getExt() );
            }
            else {
                file.setFullpath( file.getBasename() + file.getExt() );
            }
        } else {
            String warningMsg = "File \"" + tmpFileName + "\" is not a DeadboltEdit file.\n\n" +
                "Please choose a file with \".ctxt\" file extension.";
            JOptionPane warningPane = new JOptionPane();
            warningPane.showMessageDialog(null, warningMsg, "Not a DeadboltEdit File", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FileInputStream fis = null;
        byte[] inBuf = null;
        boolean fileLoaded = false;
        menuFilePasswordChange.setEnabled(false);
        logger.out("Opening encrypted file: " + file.getFullpath() );
        processFile: {      // Begin: processFile
            try {
                fis = new FileInputStream ( file.getFullpath() );
                int size = fis.available ();
                inBuf = new byte[size];
                fis.read (inBuf);
            } catch (IOException ioe) {
                fileStatusField.setText("IO Error reading file header.");
                editStatusField.setText("");
                System.err.println("IO Error reading file header.");
                ioe.printStackTrace();
                break processFile;
            } finally {
                try {
                    fis.close ();
                } catch (IOException e2) {
                    fileStatusField.setText("IO Error reading file header.");
                    editStatusField.setText("");
                    break processFile;
                }
            }
            // Process the file header
            String fileFormatError = "Error: File format is incorrect, or file is corrupt.";
            if (! PgmUtils.bufferHasString( inBuf, fileSignature) ) {
                logger.out("File is missing File Signature string in header.");
                fileStatusField.setText( fileFormatError );
                editStatusField.setText("");
                break processFile;
            }
            int headerLength = 0;
            for (int i=0; i<inBuf.length; i++ ){
                if ( inBuf[i] == (byte) '\n' ) {
                    headerLength = i + 1;
                    break;
                }
            }
            int encDataBytes = inBuf.length - headerLength;
            String[] fileHeader = null;
            try {
                fileHeader = new String( inBuf, 0, (headerLength - 1), "UTF-8" ).split( ":" );
            } catch (Exception e) {
                logger.out("Error - Exception occurred decoding file header fields.");
            }
            if ( fileHeader.length != 3 ) {
                logger.out("Error - Did not get expected fields in file header.");
                fileStatusField.setText( fileFormatError );
                editStatusField.setText("");
                break processFile;
            }
            if (BuildConfig.DEBUG) {
                for (int i=0; i<fileHeader.length; i++ ){
                    System.err.println("Debug: Header Field" + i + " = " + fileHeader[i] );
                }
            }
            byte[] encryptedData = new byte[encDataBytes];
            System.arraycopy(inBuf, headerLength, encryptedData, 0, encDataBytes );
            inBuf = null;
            if ( !fileHeader[2].equals( new String(PgmUtils.MD5Hash(encryptedData)) ) ) {
                logger.out("Hash code of encrypted block doesn't match value in header.");
                fileStatusField.setText( fileFormatError );
                editStatusField.setText("");
                break processFile;
            }
            if (! PgmUtils.bufferHasString( encryptedData, encryptionSignature) ) {
                logger.out("Encryption data block doesn't have correct signature string.");
                fileStatusField.setText( fileFormatError );
                editStatusField.setText("");
                break processFile;
            }

            // Decrypt data and check MD5 hash to validate decryption.
            // Invalid decryption indicates incorrect password.
            char[] tmpPassword = null;
            byte[] decryptedData = null;
            String decryptedText = null;
            boolean decryptionVerified = false;
            PasswordDialog pwd = new PasswordDialog( this);
            pwd.setFrameTitle( file.getBasename() + file.getExt() );
            while ( !decryptionVerified ) {
                // Get password for this file
                pwd.setVisible( true );
                tmpPassword = pwd.getPasswd();
                if ( tmpPassword.length == 0 ) {
                    fileStatusField.setText(" - Cancelled -");
                    editStatusField.setText("");
                    break processFile;
                }
                try {
                    decryptedData = OpenSSL.decrypt("blowfish", tmpPassword, encryptedData);
                } catch (IOException ioe) {     // Incorrect Password
                    decryptedData = null;
                } catch (java.security.GeneralSecurityException gse) {
                    fileStatusField.setText("GeneralSecurityException, decryption.");
                    editStatusField.setText("");
                    System.err.println("GeneralSecurityException Error occurred during data decryption.");
                    gse.printStackTrace();
                    break processFile;
                }
                if ( decryptedData != null ) {
                    // get MD5 hash and verify the decryption (verify the password)
                    int textBegin = 0;
                    int lengthEmbeddedMD5 = 0;
                    int textBytes = 0;
                    for (int i=0; (i<decryptedData.length) && (i<33); i++) {
                        if (decryptedData[i] == (byte) '\n' ) {
                            textBegin = i+1;
                            lengthEmbeddedMD5 = i;
                            break;
                        }
                    }
                    if ( lengthEmbeddedMD5 > 0 ) {
                        textBytes = decryptedData.length - textBegin;
                        String expectedMD5 = null;
                        try {
                            expectedMD5 = new String(decryptedData, 0, lengthEmbeddedMD5, "UTF-8" );
                        } catch (Exception e) {
                            logger.out("Error - Exception occurred decoding MD5 Hash from file header.");
                        }
                        try {
                            decryptedText = new String(decryptedData, textBegin, textBytes, "UTF-8");
                        } catch (Exception e) {
                            logger.out("Error - Exception occurred decoding decryptedData.");
                        }
                        if ( expectedMD5.equals( PgmUtils.MD5Hash(decryptedText) ) ) {
                            decryptionVerified = true;
                        }
                    }
                }
                if ( !decryptionVerified ) {  // Bad password - warn and re-try
                    String warningMsg = "Password is not correct for file " + file.getBasename() + file.getExt();
                    JOptionPane warningPane = new JOptionPane();
                    warningPane.showMessageDialog(null, warningMsg, "Incorrect Password", JOptionPane.WARNING_MESSAGE);
                }
            } // End: Decrypt data and verify password
            pwd.dispose();
            file.setPassword( tmpPassword );
            Arrays.fill( tmpPassword, (char) ' ');   // blank out password
            Arrays.fill(decryptedData, (byte) ' ');  // blank editor data (still have a string ...)
            editorPane.setText( new String(decryptedText) );
            editorPane.setCaretPosition( 0 );      // top of text
            encryptedData = null;
            decryptedData = null;
            fileStatusField.setText("Encrypted file");
            editStatusField.setText("");
            menuFilePasswordChange.setEnabled(true);    // Allow password change
            fileLoaded = true;
        }   // End: processFile
        System.gc();
        if ( !fileLoaded ) {
            editorPane.setText( new String("") );
            file.setPassword( null );
            file.setFullpath("");
            file.setBasename( "Edit1" );
            file.setExt( ".ctxt" );
        }

        this.startingHashCode = this.editorPane.getText().hashCode();
        this.startingTextLength = this.editorPane.getText().length();
        resetUndoHistory();     // Clear the Undo/Redo memory
        showEditStatus( false, "" );
    }       // End: loadEncryptedFile()

    // -----   Menu Processing Methods   -----
    //
    private void fileOpen(ActionEvent e) {
        //
        if ( unsavedEditorChanges() ) {
            int response;
            String warningMessage = "Unsaved editor changes for file " + file.getBasename() + file.getExt() + "\n" +
                "Continue with File Open and lose changes?";
            JOptionPane optionPane = new JOptionPane();
            response = optionPane.showConfirmDialog(null, warningMessage, "Warning",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if ( response == JOptionPane.CANCEL_OPTION ) {
                return;
            }
        }

        FileDialog fileDialog = new FileDialog (this, "Open Encrypted File", FileDialog.LOAD);
        // fileDialog.show (); ... deprecated, now use setVisible( true )
        // fileDialog.setMode( FileDialog.LOAD ); ... in constructor
        // ------- create a FilenameFilter and override its accept-method ----
        FilenameFilter fileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                //if the file extension is .ctxt return true, else false
                //if (name.endsWith(".txt") || name.endsWith(".ctxt") )
                if (name.endsWith(".ctxt") )
                    return true;
                else
                    return false;
            }
        }; // -----------------------------------------------------------------
        fileDialog.setFilenameFilter( fileFilter );
        if ( activeDirectory != null )
            fileDialog.setDirectory( activeDirectory );
        fileDialog.setVisible( true );
        if (fileDialog.getFile() == null)
            return;     // Cancelled
        if ( !verifyDirectoryChoice(fileDialog.getDirectory()) )
            return;     // Unusable directory choice
        activeDirectory = fileDialog.getDirectory();
        File f = new File( fileDialog.getDirectory() + fileDialog.getFile() );
        loadEncryptedFile( f );
    }   // End: fileOpen()

    private void fileSave(ActionEvent e) {
        //
        if ("".equals( file.getFullpath() ) || !".ctxt".equals( file.getExt() ) ) {
            fileSaveAs( null );
            return;
        } else {
            if ( !file.passwordIsSet() ) {
                PasswordCreationDialog pwNew = new PasswordCreationDialog( this);
                pwNew.setFrameTitle( file.getBasename() + file.getExt() );
                pwNew.setVisible( true );
                char[] tmpPasswdArray = pwNew.getPasswd();
                if ( tmpPasswdArray.length != 0 ) {
                    file.setPassword( tmpPasswdArray );
                    Arrays.fill( tmpPasswdArray, (char) ' ');   // blank out password
                }
                else {
                    // Warn user they cancelled
                    String warningMsg = "Cancelled - File is not saved.";
                    JOptionPane warningPane = new JOptionPane();
                    warningPane.showMessageDialog(null, warningMsg, "Cancelled", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            logger.out("Saving encrypted file: " + file.getFullpath() );
            FileOutputStream fos = null;
            // Create file header
            byte[] editorTextBuf = null;
            char[] tmpPassword = file.getPassword();
            try {
                String editorTextStr = editorPane.getText();
                String editorTextMD5 = PgmUtils.MD5Hash( editorTextStr );
                editorTextBuf = new String(editorTextMD5 + "\n" + editorTextStr ).getBytes("UTF-8");
                byte[] encryptedData = OpenSSL.encrypt("blowfish", tmpPassword, editorTextBuf );
                String fileHeader = fileSignature + "0:" + PgmUtils.MD5Hash( encryptedData ) + ":\n";
                fos = new FileOutputStream ( file.getFullpath() );
                fos.write(fileHeader.getBytes("UTF-8") );   // Write the file header
                fos.write( encryptedData );
                editorTextStr = null;
                fileStatusField.setText("Encrypted file");
                showEditStatus(false, "(Saved)");
            } catch (IOException ioe) {
                fileStatusField.setText("IO Error, encryption.");
                editStatusField.setText("");
                System.err.println("IO Error occurred during data encryption.");
                ioe.printStackTrace();
            } catch (java.security.GeneralSecurityException gse) {
                fileStatusField.setText("GeneralSecurityException, encryption.");
                editStatusField.setText("");
                System.err.println("GeneralSecurityException Error occurred during data encryption.");
                gse.printStackTrace();
            } finally {
                try {
                    Arrays.fill(tmpPassword, (char) ' ');  // blank out password
                    Arrays.fill(editorTextBuf, (byte) ' ');
                    fos.close ();
                } catch (IOException e2) {
                    fileStatusField.setText("IO Error, file close.");
                }
            }
            menuFilePasswordChange.setEnabled(true);    // Allow password change
            startingHashCode = editorPane.getText().hashCode();
            startingTextLength = editorPane.getText().length();
            resetUndoHistory();     // Clear the Undo/Redo memory
        }
    }

    private void fileSaveAs(ActionEvent e) {
        //
        FileDialog fileDialog = new FileDialog (this, "Save Encrypted File", FileDialog.SAVE);
        // fileDialog.show ();  ... deprecated
        // fileDialog.setMode( FileDialog.SAVE );  ... in constructor
        // ------- create a FilenameFilter and override its accept-method ----
        FilenameFilter fileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                //if the file extension is .ctxt return true, else false
                //if (name.endsWith(".txt") || name.endsWith(".ctxt") )
                if (name.endsWith(".ctxt") )
                    return true;
                else
                    return false;
            }
        }; // -----------------------------------------------------------------
        fileDialog.setFilenameFilter( fileFilter );

        String oldFileExtension = file.getExt();
        file.setExt( ".ctxt" );
        fileDialog.setFile(file.getBasename() + file.getExt() );
        if ( activeDirectory != null )
            fileDialog.setDirectory( activeDirectory );
        fileDialog.setVisible( true );
        if (fileDialog.getFile () == null) {    // Cancelled
            file.setExt( oldFileExtension );
            return;
        }
        if ( !verifyDirectoryChoice(fileDialog.getDirectory()) )
            return;     // Unusable directory choice
        activeDirectory = fileDialog.getDirectory();
        String tmpFileName = fileDialog.getFile ();

        int dotIndex = tmpFileName.lastIndexOf(".");
        if (dotIndex >= 0 ) {
            file.setBasename( tmpFileName.substring(0, dotIndex) );
            //file.setExt( tmpFileName.substring(dotIndex) );
        } else {
            file.setBasename( tmpFileName );
        }
        file.setExt( ".ctxt" );
        //file.setFullpath( fileDialog.getDirectory() + File.separator + file.getBasename() + file.getExt() );
        file.setFullpath( fileDialog.getDirectory() + file.getBasename() + file.getExt() );
        file.setPassword( null );  //New file name, so assume a new password needed
        menuFilePasswordChange.setEnabled(false);   // Disallow password change (re-enabled in fileSave())
        fileSave( null );
    }

    private void quitApp(ActionEvent e) {
        //
        DeadboltEdit editor;
        for (int i = 0; i < openEditors.size(); i++) {
            editor = (DeadboltEdit) openEditors.get(i);
            if ( (i != editorID) && (editor != null) ) {
                if (BuildConfig.DEBUG)
                    System.err.println("Quitting ... openEditorCount=" + Integer.toString(openEditorCount) );
                editor.mainWindowClosing( null );
            }
        }
        if (BuildConfig.DEBUG)
            System.err.println("Quitting ... openEditorCount=" + Integer.toString(openEditorCount) );
        this.mainWindowClosing( null );
    }

    private void fileNewWin(ActionEvent e) {
    	// Pass current value of activeDirectory to new window.
        if (activeDirectory != null ) {
            settings.setProperty( "active.directory", activeDirectory );
        }
        PgmUtils.saveSettings( settings, runtimeSettings.getProperty("settings.dir"), runtimeSettings.getProperty("settings.file") );
    	new DeadboltEdit( null );
    }

    private void fileClose(ActionEvent e) {
    	//
        mainWindowClosing( null );
    }

    private void fileSavePlaintext(ActionEvent e) {
    	//
        FileDialog fileDialog = new FileDialog (this, "Save as Plain Text File", FileDialog.SAVE);
        String oldFileExtension = file.getExt();
        file.setExt( ".txt" );
        fileDialog.setFile(file.getBasename() + file.getExt() );
        if ( activeDirectory != null )
            fileDialog.setDirectory( activeDirectory );
        fileDialog.setVisible( true );
        if (fileDialog.getFile () == null) {    // Cancelled
            file.setExt( oldFileExtension );
            return;
        }
        if ( !verifyDirectoryChoice(fileDialog.getDirectory()) )
            return;     // Unusable directory choice
        activeDirectory = fileDialog.getDirectory();
        String tmpFileName = fileDialog.getFile ();

        int dotIndex = tmpFileName.lastIndexOf(".");
        if (dotIndex >= 0 ) {
            file.setBasename( tmpFileName.substring(0, dotIndex) );
            file.setExt( tmpFileName.substring(dotIndex) );
        } else {
            file.setBasename( tmpFileName );
            file.setExt( "" );
        }
        file.setFullpath( fileDialog.getDirectory() + file.getBasename() + file.getExt() );
        logger.out("Saving plain-text file: " + file.getFullpath() );
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream ( file.getFullpath() );
            fos.write( editorPane.getText().getBytes("UTF-8") );
            this.fileStatusField.setText("Plain-Text file");
            showEditStatus(false, "(Saved)");
        } catch (IOException ioe) {
            fileStatusField.setText("IO Error, file write.");
            System.err.println("IO Error occurred during file write.");
            ioe.printStackTrace();
        } finally {
            try {
                fos.close ();
            } catch (IOException e2) {
                fileStatusField.setText("IO Error, file close.");
                System.err.println("IO Error occurred during file close.");
                e2.printStackTrace();
            }
        }
        file.setPassword( null );
        menuFilePasswordChange.setEnabled(false);
        this.startingHashCode = this.editorPane.getText().hashCode();
        this.startingTextLength = this.editorPane.getText().length();
        resetUndoHistory();     // Clear the Undo/Redo memory
    }

    private void filePrint(ActionEvent e) {
        String valueStr = null;
        int tabSize;
        valueStr = settings.getProperty("tab.size");
        try {
            tabSize = Integer.parseInt( valueStr );
        }
        catch (NumberFormatException nfe) {       // Bad value from user settings file
            System.err.println("Error: filePrint() - Invalid tab.size, using FallbAck Default");
            tabSize = PgmUtils.FALLBACK_DEFAULT_TAB_SIZE;
        }
        Font printFont = editorPane.getFont();  // Use current editor font for printing
        PrintRenderer printRenderer = new PrintRenderer( printFont, tabSize);
        printRenderer.printText( editorPane.getText() );
    }

    private void fileOpenPlaintext(ActionEvent e) {
    	//
        if ( unsavedEditorChanges() ) {
            int response;
            JOptionPane optionPane = new JOptionPane();
            response = optionPane.showConfirmDialog(null, "File has changed - Continue with Open?", "Warning",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.NO_OPTION) {
                return;
            }
        }

        FileDialog fileDialog = new FileDialog (this, "Open Plain Text File", FileDialog.LOAD);
        // fileDialog.show (); ... deprecated, now use setVisible( true )
        // fileDialog.setMode( FileDialog.LOAD ); ... in constructor
        if ( activeDirectory != null )
            fileDialog.setDirectory( activeDirectory );
        fileDialog.setVisible( true );
        if (fileDialog.getFile () == null)  // Cancelled
            return;
        if ( !verifyDirectoryChoice(fileDialog.getDirectory()) )
            return;     // Unusable directory choice
        activeDirectory = fileDialog.getDirectory();
        String tmpFileName = fileDialog.getFile ();
        int dotIndex = tmpFileName.lastIndexOf(".");
        if ( dotIndex >= 0 ) {
            file.setBasename( tmpFileName.substring(0, dotIndex) );
            file.setExt( tmpFileName.substring(dotIndex) );
        } else {
            file.setBasename( tmpFileName );
            file.setExt( "" );
        }
        //file.setFullpath( fileDialog.getDirectory() + File.separator + file.getBasename() + file.getExt() );
        file.setFullpath( fileDialog.getDirectory() + file.getBasename() + file.getExt() );
        file.setPassword( null );
        menuFilePasswordChange.setEnabled(false);
        logger.out("Opening plain-text file: " + file.getFullpath() );
        // check file existance to avoid errors
        File tmpf = new File( file.getFullpath() );
        if ( !tmpf.exists() || !tmpf.canRead() ) {
            String warningMsg = "File \"" + tmpf.getPath() + "\" does not exist, or is not readable.\n\n" +
                "(File may have been renamed, moved, or deleted?)";
            JOptionPane warningPane = new JOptionPane();
            warningPane.showMessageDialog(null, warningMsg, "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tmpf = null;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream ( file.getFullpath() );
            int size = fis.available ();
            byte[] inBuf = new byte[size];
            fis.read (inBuf);
            editorPane.setText( new String(inBuf) );
            editorPane.setCaretPosition( 0 );      // top of text
            fileStatusField.setText("Plain-Text file.");
            inBuf = null;
            System.gc();
        } catch (IOException ioe) {
            fileStatusField.setText("IO Error, file read.");
            System.err.println("IO Error occurred during file read.");
            ioe.printStackTrace();
        } finally {
            try {
                fis.close ();
            } catch (IOException e2) {
                fileStatusField.setText("IO Error, file close.");
                System.err.println("IO Error occurred during file close.");
                e2.printStackTrace();
            }
        }

        this.startingHashCode = this.editorPane.getText().hashCode();
        this.startingTextLength = this.editorPane.getText().length();
        resetUndoHistory();     // Clear the Undo/Redo memory
        showEditStatus( false, "" );
    }

    private void filePasswordChange(ActionEvent e) {
    	//
        PasswordChangeDialog pwcd = new PasswordChangeDialog( this);
        pwcd.setExpectedMD5Hash( file.getMD5Hash() );    // to confirm old password
        pwcd.setFrameTitle( file.getBasename() + file.getExt() );
        pwcd.setVisible( true );
        char[] tmpPasswdArray = pwcd.getPasswd();

        boolean cancelPasswordChange = true;
        if ( tmpPasswdArray.length != 0 ) {
            // Confirm OK to save file, in order to complete password change
            int response;
            String confirmationMessage =
                "To complete the password change for file: " + file.getBasename() + file.getExt() + ",\n" +
                "the current contents of the editor will be encrypted with the\n" +
                "new password and saved.\n\n" +
                "OK to Save File using new password?";
            JOptionPane optionPane = new JOptionPane();
            response = optionPane.showConfirmDialog(null, confirmationMessage, "Confirm Action",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if ( response == JOptionPane.OK_OPTION ) {
                cancelPasswordChange = false;
            }
            else {
                cancelPasswordChange = true;
            }
        }
        else {
            cancelPasswordChange = true;
        }

        // Note: There were two opportunities to cancel password change, in the
        //       PasswordChangeDialog and the file-save confirmation dialog.
        if ( !cancelPasswordChange ) {
            // Save the file, using the new password
            logger.out("Changed password for file: " + file.getFullpath() );
            file.setPassword( tmpPasswdArray );
            Arrays.fill( tmpPasswdArray, (char) ' ');   // blank out password
            fileSave( null );
            //String infoMessage = "File: " + file.getBasename() + file.getExt() + ",\n" +
            //    "Encrypted with New Password and Saved.";
            //JOptionPane optionPane = new JOptionPane();
            //optionPane.showMessageDialog(null, infoMessage, "Password Changed", JOptionPane.PLAIN_MESSAGE);
            showEditStatus( false, "(Saved w/New PW)" );
        } else {
            String infoMessage = "Password change cancelled.\n";
            JOptionPane optionPane = new JOptionPane();
            optionPane.showMessageDialog(null, infoMessage, "Cancelled", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void editUndo(ActionEvent e) {
        try {
            undoManager.undo();
        } catch (CannotUndoException exp) {
            Toolkit.getDefaultToolkit().beep();
        }
        if ( undoManager.canUndo() ) {
            String undoMenuDesc = undoManager.getUndoPresentationName();
            menuEditUndo.setText(undoMenuDesc);
        }
        else {
            menuEditUndo.setText("Undo");
        }
        if ( undoManager.canRedo() ) {
            String redoMenuDesc = undoManager.getRedoPresentationName();
            menuEditRedo.setText(redoMenuDesc);
            redoInformationShown = true;
        }
        else {
            menuEditRedo.setText("Redo");
            redoInformationShown = false;
        }
    }

    private void editRedo(ActionEvent e) {
        try {
            undoManager.redo();
        } catch (CannotRedoException exp) {
            Toolkit.getDefaultToolkit().beep();
        }
        if ( undoManager.canRedo() ) {
            String redoMenuDesc = undoManager.getRedoPresentationName();
            menuEditRedo.setText(redoMenuDesc);
            redoInformationShown = true;
        }
        else {
            menuEditRedo.setText("Redo");
            redoInformationShown = false;
        }
        if ( undoManager.canUndo() ) {
            String undoMenuDesc = undoManager.getUndoPresentationName();
            menuEditUndo.setText(undoMenuDesc);
        }
        else {
            menuEditUndo.setText("Undo");
        }
    }

    private void editCut() {
        editorPane.cut();
    }

    private void editCopy() {
        editorPane.copy();
    }

    private void editPaste() {
        editorPane.paste();
    }

    private void editDelete() {
        editorPane.replaceSelection( null );
    }

    private void editSelectAll() {
        editorPane.selectAll();
    }

    private void editFind() {
        //
        if (textSearcher == null ) {
            textSearcher = new TextSearcher( editorPane );
        }
        if ( textSearcher.hasDialogLock() ) {
            return;
        }
        // grabFocus() is needed for case when activated by toolbar, JTextPane loses focus
        // and selected text isn't highlighted.  Not a problem when menu is used ???
        editorPane.grabFocus();    // In case activated by toolbar (JTextPane loses focus)
        FindDialog fd = new FindDialog( this, textSearcher );
        fd.setVisible( true );
        menuEditFindNext.setEnabled( true );
    }

    private void editFindNext() {
        //
        if ( textSearcher == null ) {
            return;
        }
        if ( textSearcher.canContinueSearch() ) {
            int result;
            if ( textSearcher.isMatchCaseSet() ) {
                result = textSearcher.findNextMatchCase();
            } else {
                result = textSearcher.findNextIgnoreCase();
            }
            if ( result < 0 ) {
                String infoMessage = "Find Next: Reached end-of-text.";
                JOptionPane optionPane = new JOptionPane();
                optionPane.showMessageDialog(null, infoMessage, "Find Next", JOptionPane.PLAIN_MESSAGE);
            }
        } else {
            if ( textSearcher.hasDialogLock() ) {
                String infoMessage = "Find Next: Reached end-of-text.";
                JOptionPane optionPane = new JOptionPane();
                optionPane.showMessageDialog(null, infoMessage, "Find Next", JOptionPane.PLAIN_MESSAGE);
            } else {
                // launch FindDialog
                this.editFind();
            }
        }
    }

    private void editFindReplace() {
        //
        if (textSearcher == null ) {
            textSearcher = new TextSearcher( editorPane );
        }
        if ( textSearcher.hasDialogLock() ) {
            return;
        }
        editorPane.grabFocus();    // In case activated by toolbar (JTextPane loses focus)
        FindReplaceDialog fr = new FindReplaceDialog( this, textSearcher );
        fr.setVisible( true );
        menuEditFindNext.setEnabled( true );
    }

    private void settingsTextColor() {
    	Color textColor = JColorChooser.showDialog( this, "Editor Text Color", editorPane.getForeground() );
    	if ( textColor != null ) {
    	    settings.setProperty("text.color", Integer.toString( textColor.getRGB()) );
    	    applySettingsAllWindows();
    	}
    }

    private void settingsBackgroundColor() {
    	Color bgColor = JColorChooser.showDialog( this, "Editor Background Color", editorPane.getBackground() );
    	if ( bgColor != null ) {
    	    settings.setProperty("background.color", Integer.toString( bgColor.getRGB()) );
    	    applySettingsAllWindows();
    	}
    }

    private void settingsTextFont() {
    	JFontChooser fontChooser = new JFontChooser();
    	fontChooser.setSelectedFont( editorPane.getFont() );
    	if ( fontChooser.showDialog( this ) == JFontChooser.OK_OPTION ) {
    	    Font font = fontChooser.getSelectedFont();
    	    String fName=fontChooser.getSelectedFontFamily();
    	    String fStyle = Integer.toString( fontChooser.getSelectedFontStyle() );
    	    String fSize = Integer.toString( fontChooser.getSelectedFontSize() );
    	    settings.setProperty("text.font.name", fName);
    	    settings.setProperty("text.font.style", fStyle);
    	    settings.setProperty("text.font.size", fSize);
    	    applySettingsAllWindows();
    	}
    }

    // Set Tab Size
    private void settingsTabSize() {
        TabSizeDialog tabSizeDialog = new TabSizeDialog( this, PgmUtils.MAX_TABSIZE );
        String valStr = settings.getProperty("tab.size");
        int currentTabSize;
        try {
            currentTabSize = Integer.parseInt( valStr );
        }
        catch (NumberFormatException nfe ) { //if bad value stored in settings
            System.err.println("settingsTabSize(): Bad tab.size settings value.");
            currentTabSize = 8;     // temporary fallback, allow user to continue
        }
        tabSizeDialog.setValue( currentTabSize );
        tabSizeDialog.setVisible( true );
        // Note: "Cancel" button is handled within dialog, reverts to currrent setting
        int newTabSize = tabSizeDialog.getValue();
        if ( newTabSize != currentTabSize ) {
            logger.out("Changing Tab Size setting to " + newTabSize );
            settings.setProperty("tab.size", Integer.toString( newTabSize ) );
            applySettingsAllWindows();
        }
    }

    // Line-Wrap Enable/Disable (Checkbox)
    private void settingsLineWrap() {
        if ( menuSettingsLineWrapChkbox.getState() == true ) { // Enable
            settings.setProperty("line.wrap.enable", "1");
        }
        else {                                                  // Disabled
            settings.setProperty("line.wrap.enable", "0");
        }
        applySettingsAllWindows();
    }

    // Select desired Window Style (Look and Feel)
    private void settingsWindowStyle() {
    	WindowStyleDialog winStyleDialog = new WindowStyleDialog( this );
    	String sysLabel = null;
    	String sysType = runtimeSettings.getProperty("system.type");
    	if ( sysType.equals( PgmUtils.SYS_WINDOWS ) ) {
    	    sysLabel = "MS Windows";
    	}
    	else if ( sysType.equals( PgmUtils.SYS_MACOSX ) ) {
    	    sysLabel = "Mac OS X (Aqua)";
    	}
    	else if ( sysType.equals( PgmUtils.SYS_LINUX ) ) {
    	    sysLabel = "Linux Default Style";
    	}
    	if (sysLabel != null ) {
    	    winStyleDialog.setSystemNameLabel(sysLabel);
    	}
    	winStyleDialog.setSelection( settings.getProperty("window.style") );
    	winStyleDialog.setVisible( true );
    	String windowStyle = winStyleDialog.getSelection();
    	// null result if Cancel button pressed ...
    	if (windowStyle != null ) {
    	    logger.out("Changing Window Style to: " + windowStyle );
    	    settings.setProperty("window.style", windowStyle);
    	    applySettingsAllWindows();
        }
    	winStyleDialog.dispose();
    }

    // Enable or Disable Debug Log
    // This is called when the CheckBox on the Settings menu is changed.
    private void settingsDebugLog() {
    	if ( MenuSettingsDebugLogChkbox.getState() == true ) { // Selected Enable
            int response;
            String confirmMessage =
                "Enable Debug Logfile?\n\n" +
                "Normally should be OFF, it is helpful when\ntroubleshooting problems.\n\n" +
                "Location:\n" + runtimeSettings.getProperty("log.file");
            JOptionPane optionPane = new JOptionPane();
            response = optionPane.showConfirmDialog(null, confirmMessage, "Enable Debug Logfile",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
            if ( response == JOptionPane.OK_OPTION ) {
                settings.setProperty("debug.log.enable", "1");
                applySettingsAllWindows();
            }
            else {
                // Canceled Action: reset menu checkbox to prior state
                MenuSettingsDebugLogChkbox.setState( false );
            }
    	}
    	else {     // Selected Disable
            int response;
            String confirmMessage =
                "Disable Debug Logfile?" +
                "\n\nLocation:\n" + runtimeSettings.getProperty("log.file");
            JOptionPane optionPane = new JOptionPane();
            response = optionPane.showConfirmDialog(null, confirmMessage, "Disable Debug Logfile",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
            if ( response == JOptionPane.OK_OPTION ) {
                settings.setProperty("debug.log.enable", "0");
                applySettingsAllWindows();
            }
            else {
                // Canceled Action: reset menu checkbox to prior state
                MenuSettingsDebugLogChkbox.setState( true );
            }

    	}
    }

    // Restore default settings (discard all customizations)
    private void settingsRestoreDefaults() {
        int response;
        String warningMessage = "This action will reset all program settings to default values.";
        JOptionPane optionPane = new JOptionPane();
        response = optionPane.showConfirmDialog(null, warningMessage, "Reset Program Settings",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE );
        if ( response == JOptionPane.OK_OPTION ) {
            logger.out("User Action: Program settings reset to default values.");
            settings = new Properties( defaultSettings );
            applySettingsAllWindows();
    	}
    }

    private void helpHelp() {
        java.net.URL helpURL = null;
        //
        // Look for HTML UserGuide file in file system, stored with app.
        String htmlHelpFile = new String(runtimeSettings.getProperty("app.root") + File.separator + "Help" + File.separator + "UserGuide.html");
        File hf = new File( htmlHelpFile );
        if  (hf.exists() && hf.canRead() ) {
            logger.out("HelpViewer: Displaying UserGuide from file: " + htmlHelpFile );
            try {
                helpURL = new java.net.URL( "file:" + htmlHelpFile );
            }
            catch (Exception e) {
                logger.out("HelpViewer: Exception occurred while creating URL from help file " + htmlHelpFile + ".");
                e.printStackTrace();
            }
        }
        // If HTML UserGuide file could not be found or read, use copy embedded in jar file.
        // This seems to be just as fast.
        if (helpURL == null ) {
            logger.out("HelpViewer: Displaying UserGuide from internal resource bundle.");
            helpURL = ClassLoader.getSystemClassLoader().getResource( "Resources/Help/UserGuide.html" );
        }

        if (! runtimeSettings.getProperty("system.type").equals(PgmUtils.SYS_MACOSX)) {
            // For non-MacOS, add an icon (HtmlViewer is a JFrame and can be inonized like a main window)
            java.net.URL iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/DeadboltEdit32x32.png");
            ImageIcon icon = new ImageIcon( iconURL);
            new HtmlViewer(helpURL, "DeadboltEdit Help", 750, 600, false, icon );
        } else {
            // For MacOS, use the default icon for JFrame (an active icon)
            new HtmlViewer(helpURL, "DeadboltEdit Help", 750, 600, false );
        }
    }

    private void helpViewLicense() {
        java.net.URL licenseURL = ClassLoader.getSystemClassLoader().getResource( "Resources/license.html" );
        if (! runtimeSettings.getProperty("system.type").equals(PgmUtils.SYS_MACOSX)) {
            // For non-MacOS, add an icon (HtmlViewer is a JFrame and can be inonized like a main window)
            java.net.URL iconURL = ClassLoader.getSystemClassLoader().getResource("Resources/images/DeadboltEdit32x32.png");
            ImageIcon icon = new ImageIcon( iconURL);
            new HtmlViewer(licenseURL, "Licensing and Legal Information", 750, 600, false, icon );
        } else {
            // For MacOS, use the default icon for JFrame (an active icon)
            new HtmlViewer(licenseURL, "Licensing and Legal Information", 750, 600, false );
        }
    }

    private void helpAbout() {
        String aboutMessage = "            DeadboltEdit\n\n" +
                              "   Version " + programVersion + " - " + runtimeSettings.getProperty("platform.desc") + "\n\n" +
                              "Copyright 2012-2018   Michael Wright" + "\n\n" +
                              "       //www.deadboltedit.org       ";
        java.net.URL iconURL =
            ClassLoader.getSystemClassLoader().getResource("Resources/images/DeadboltEdit48x48.png");
        ImageIcon icon = new ImageIcon( iconURL);
        JOptionPane optionPane = new JOptionPane();
        optionPane.showMessageDialog(null, aboutMessage, "About",
            JOptionPane.PLAIN_MESSAGE, icon);
    }

    // verifyDirectoryChoice() - Verify that directory returned from FileDialog exists.
    //    Called immediately after a FileDialog to verify the returned directory.
    //    Post error dialog if directory not usable.
    //    This is mostly needed for Linux - FileDialog will return a null directory when
    //    a choice is made from "Recent Files".
    private boolean verifyDirectoryChoice( String dir ) {
        if ( dir != null ) {
            File d = new File( dir );
            if ( d.exists() && d.isDirectory() ) {
                return( true );
            }
        }
        String warningMsg = "An invalid directory was returned by the File Chooser Dialog.\n" +
                            "Please choose a valid directory.\n\n";
        String sysType = runtimeSettings.getProperty("system.type");
        if ( sysType.equals(PgmUtils.SYS_LINUX) ) {
            warningMsg = warningMsg + "If selecting from \"Recently Used\", \"Recent Files\", or a\n" +
                                      "similar collection, navigate to the actual directory instead.\n" +
                                      "(Known issue on some versions of Linux)\n\n";
        }
        logger.out("Warning Dialog: Invalid Directory from File Chooser Dialog.");
        JOptionPane warningPane = new JOptionPane();
        warningPane.showMessageDialog(null, warningMsg, "Invalid Directory", JOptionPane.WARNING_MESSAGE);
        return( false );
    }

    // initActiveDirectory() - set activeDirectory for file dialogs
    private void initActiveDirectory() {
        // first verify user.home, it's the fallback if valid
        String userHome = null;
        if ( runtimeSettings.containsKey("user.home") ) {
            userHome = runtimeSettings.getProperty("user.home");
        }
        else {
            userHome = null;
        }
        if (userHome != null ) {
            File uh = new File( userHome );
            if (!uh.exists() || !uh.isDirectory() ) {
                userHome = null;  // it was set but not valid
            }
        }
        // If active.directory was saved by prior session, verify that it's still valid
        if (settings.containsKey("active.directory") ) {
            File ad = new File( settings.getProperty("active.directory") );
            if ( ad.exists() && ad.isDirectory() ) {
                activeDirectory = settings.getProperty("active.directory");
            }
            else {
                activeDirectory = userHome;
            }
        }
        else {
            activeDirectory = userHome;
        }
        return;
    }

    // Popup Menu in Editor Pane, invoked by right-click
    private void initEditorPopupMenu() {
        menuEditorPopup = new JPopupMenu();
        editorPopupMenuItemCut = new JMenuItem("Cut");
        editorPopupMenuItemCopy = new JMenuItem("Copy");
        editorPopupMenuItemPaste = new JMenuItem("Paste");
        editorPopupMenuItemDelete = new JMenuItem("Delete");
        // Add ActionListener to each menuItem
        editorPopupMenuItemCut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editCut();
            }
        });
        editorPopupMenuItemCopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editCopy();
            }
        });
        editorPopupMenuItemPaste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editPaste();
            }
        });
        editorPopupMenuItemDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editDelete();
            }
        });
        //
        menuEditorPopup.add(editorPopupMenuItemCut);
        menuEditorPopup.add(editorPopupMenuItemCopy);
        menuEditorPopup.add(editorPopupMenuItemPaste);
        menuEditorPopup.addSeparator();
        menuEditorPopup.add(editorPopupMenuItemDelete);

        editorPane.setComponentPopupMenu(menuEditorPopup);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar1 = new JMenuBar();
        menuFile = new JMenu();
        menuWindowNew = new JMenuItem();
        menuFileOpen = new JMenuItem();
        menuFileClose = new JMenuItem();
        menuFileSave = new JMenuItem();
        menuFileSaveAs = new JMenuItem();
        menuFilePasswordChange = new JMenuItem();
        separator1 = new JSeparator();
        menuFileImport = new JMenuItem();
        menuFileExport = new JMenuItem();
        menuFilePrint = new JMenuItem();
        menuFileQuit = new JMenuItem();
        menuEdit = new JMenu();
        menuEditUndo = new JMenuItem();
        menuEditRedo = new JMenuItem();
        menuEditCut = new JMenuItem();
        menuEditCopy = new JMenuItem();
        menuEditPaste = new JMenuItem();
        menuEditDelete = new JMenuItem();
        menuEditSelectAll = new JMenuItem();
        menuEditFind = new JMenuItem();
        menuEditFindNext = new JMenuItem();
        menuEditFindReplace = new JMenuItem();
        menuSettings = new JMenu();
        menuSettingsTextColor = new JMenuItem();
        menuSettingsBackgroundColor = new JMenuItem();
        menuSettingsTextFont = new JMenuItem();
        menuSettingsTabSize = new JMenuItem();
        menuSettingsLineWrapChkbox = new JCheckBoxMenuItem();
        menuSettingsWindowStyle = new JMenuItem();
        MenuSettingsDebugLogChkbox = new JCheckBoxMenuItem();
        menuSettingsRestoreDefaults = new JMenuItem();
        menuHelp = new JMenu();
        menuHelpHelp = new JMenuItem();
        menuHelpViewLicense = new JMenuItem();
        menuHelpAbout = new JMenuItem();
        toolBar1 = new JToolBar();
        toolbarNew = new JButton();
        toolbarOpen = new JButton();
        toolbarSave = new JButton();
        toolbarClose = new JButton();
        toolbarPrint = new JButton();
        toolbarCut = new JButton();
        toolbarCopy = new JButton();
        toolbarPaste = new JButton();
        toolbarFind = new JButton();
        toolbarHelp = new JButton();
        scrollPane1 = new JScrollPane();
        noWrapPanel = new JPanel();
        editorPane = new JTextPane();
        fileStatusField = new JLabel();
        editStatusField = new JLabel();

        //======== this ========
        setTitle("DeadboltEdit");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainWindowClosing(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {10, 347, 0, 5, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0, 0.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

        //======== menuBar1 ========
        {

            //======== menuFile ========
            {
                menuFile.setText("File");

                //---- menuWindowNew ----
                menuWindowNew.setText("New Window");
                menuWindowNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuWindowNew.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileNewWin(e);
                    }
                });
                menuFile.add(menuWindowNew);

                //---- menuFileOpen ----
                menuFileOpen.setText("Open ...");
                menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuFileOpen.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileOpen(e);
                    }
                });
                menuFile.add(menuFileOpen);

                //---- menuFileClose ----
                menuFileClose.setText("Close");
                menuFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuFileClose.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileClose(e);
                    }
                });
                menuFile.add(menuFileClose);

                //---- menuFileSave ----
                menuFileSave.setText("Save");
                menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuFileSave.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileSave(e);
                    }
                });
                menuFile.add(menuFileSave);

                //---- menuFileSaveAs ----
                menuFileSaveAs.setText("Save as ...");
                menuFileSaveAs.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileSaveAs(e);
                    }
                });
                menuFile.add(menuFileSaveAs);

                //---- menuFilePasswordChange ----
                menuFilePasswordChange.setText("Password Change ...");
                menuFilePasswordChange.setEnabled(false);
                menuFilePasswordChange.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        filePasswordChange(e);
                    }
                });
                menuFile.add(menuFilePasswordChange);
                menuFile.add(separator1);

                //---- menuFileImport ----
                menuFileImport.setText("Open Plain Text File ...");
                menuFileImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.ALT_MASK));
                menuFileImport.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileOpenPlaintext(e);
                    }
                });
                menuFile.add(menuFileImport);

                //---- menuFileExport ----
                menuFileExport.setText("Save Plain Text File ...");
                menuFileExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.ALT_MASK));
                menuFileExport.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileSavePlaintext(e);
                    }
                });
                menuFile.add(menuFileExport);
                menuFile.addSeparator();

                //---- menuFilePrint ----
                menuFilePrint.setText("Print");
                menuFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuFilePrint.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        filePrint(e);
                    }
                });
                menuFile.add(menuFilePrint);

                //---- menuFileQuit ----
                menuFileQuit.setText("Exit");
                menuFileQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
                menuFileQuit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        quitApp(e);
                    }
                });
                menuFile.add(menuFileQuit);
            }
            menuBar1.add(menuFile);

            //======== menuEdit ========
            {
                menuEdit.setText("Edit");

                //---- menuEditUndo ----
                menuEditUndo.setText("Undo");
                menuEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditUndo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editUndo(e);
                    }
                });
                menuEdit.add(menuEditUndo);

                //---- menuEditRedo ----
                menuEditRedo.setText("Redo");
                menuEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditRedo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editRedo(e);
                    }
                });
                menuEdit.add(menuEditRedo);
                menuEdit.addSeparator();

                //---- menuEditCut ----
                menuEditCut.setText("Cut");
                menuEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditCut.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editCut();
                    }
                });
                menuEdit.add(menuEditCut);

                //---- menuEditCopy ----
                menuEditCopy.setText("Copy");
                menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditCopy.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editCopy();
                    }
                });
                menuEdit.add(menuEditCopy);

                //---- menuEditPaste ----
                menuEditPaste.setText("Paste");
                menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditPaste.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editPaste();
                    }
                });
                menuEdit.add(menuEditPaste);

                //---- menuEditDelete ----
                menuEditDelete.setText("Delete");
                menuEditDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
                menuEditDelete.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editDelete();
                    }
                });
                menuEdit.add(menuEditDelete);
                menuEdit.addSeparator();

                //---- menuEditSelectAll ----
                menuEditSelectAll.setText("Select All");
                menuEditSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditSelectAll.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editSelectAll();
                    }
                });
                menuEdit.add(menuEditSelectAll);
                menuEdit.addSeparator();

                //---- menuEditFind ----
                menuEditFind.setText("Find ...");
                menuEditFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditFind.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editFind();
                    }
                });
                menuEdit.add(menuEditFind);

                //---- menuEditFindNext ----
                menuEditFindNext.setText("Find Next");
                menuEditFindNext.setEnabled(false);
                menuEditFindNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditFindNext.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editFindNext();
                    }
                });
                menuEdit.add(menuEditFindNext);

                //---- menuEditFindReplace ----
                menuEditFindReplace.setText("Find/Replace ...");
                menuEditFindReplace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                menuEditFindReplace.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editFindReplace();
                    }
                });
                menuEdit.add(menuEditFindReplace);
            }
            menuBar1.add(menuEdit);

            //======== menuSettings ========
            {
                menuSettings.setText("Settings");

                //---- menuSettingsTextColor ----
                menuSettingsTextColor.setText("Text Color ...");
                menuSettingsTextColor.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        settingsTextColor();
                    }
                });
                menuSettings.add(menuSettingsTextColor);

                //---- menuSettingsBackgroundColor ----
                menuSettingsBackgroundColor.setText("Background Color ...");
                menuSettingsBackgroundColor.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        settingsBackgroundColor();
                    }
                });
                menuSettings.add(menuSettingsBackgroundColor);

                //---- menuSettingsTextFont ----
                menuSettingsTextFont.setText("Font ...");
                menuSettingsTextFont.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        settingsTextFont();
                    }
                });
                menuSettings.add(menuSettingsTextFont);
                menuSettings.addSeparator();

                //---- menuSettingsTabSize ----
                menuSettingsTabSize.setText("Tab Size ...");
                menuSettingsTabSize.setToolTipText("Set tab spacing size");
                menuSettingsTabSize.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        settingsTabSize();
                    }
                });
                menuSettings.add(menuSettingsTabSize);
                menuSettings.addSeparator();

                //---- menuSettingsLineWrapChkbox ----
                menuSettingsLineWrapChkbox.setText("Line-Wrap");
                menuSettingsLineWrapChkbox.setToolTipText("Enable/Disable Line-Wrap (long lines wrap to next line)");
                menuSettingsLineWrapChkbox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        settingsLineWrap();
                    }
                });
                menuSettings.add(menuSettingsLineWrapChkbox);
                menuSettings.addSeparator();

                //---- menuSettingsWindowStyle ----
                menuSettingsWindowStyle.setText("Window Style ...");
                menuSettingsWindowStyle.setToolTipText("Select Window Style");
                menuSettingsWindowStyle.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        settingsWindowStyle();
                    }
                });
                menuSettings.add(menuSettingsWindowStyle);
                menuSettings.addSeparator();

                //---- MenuSettingsDebugLogChkbox ----
                MenuSettingsDebugLogChkbox.setText("Debug Log");
                MenuSettingsDebugLogChkbox.setSelected(true);
                MenuSettingsDebugLogChkbox.setToolTipText("Enable/Disable the debug logfile (session.log).\nSee Program Help for location of logfile.");
                MenuSettingsDebugLogChkbox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        settingsDebugLog();
                    }
                });
                menuSettings.add(MenuSettingsDebugLogChkbox);
                menuSettings.addSeparator();

                //---- menuSettingsRestoreDefaults ----
                menuSettingsRestoreDefaults.setText("Restore Default Settings");
                menuSettingsRestoreDefaults.setToolTipText("Restore all settings to default values.");
                menuSettingsRestoreDefaults.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        settingsRestoreDefaults();
                    }
                });
                menuSettings.add(menuSettingsRestoreDefaults);
            }
            menuBar1.add(menuSettings);

            //======== menuHelp ========
            {
                menuHelp.setText("Help");

                //---- menuHelpHelp ----
                menuHelpHelp.setText("User Guide");
                menuHelpHelp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        helpHelp();
                    }
                });
                menuHelp.add(menuHelpHelp);
                menuHelp.addSeparator();

                //---- menuHelpViewLicense ----
                menuHelpViewLicense.setText("License Information");
                menuHelpViewLicense.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        helpViewLicense();
                    }
                });
                menuHelp.add(menuHelpViewLicense);

                //---- menuHelpAbout ----
                menuHelpAbout.setText("About DeadboltEdit ...");
                menuHelpAbout.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        helpAbout();
                    }
                });
                menuHelp.add(menuHelpAbout);
            }
            menuBar1.add(menuHelp);
        }
        setJMenuBar(menuBar1);

        //======== toolBar1 ========
        {
            toolBar1.setFloatable(false);
            toolBar1.setBorderPainted(false);
            toolBar1.setBorder(null);
            toolBar1.setOpaque(false);

            //---- toolbarNew ----
            toolbarNew.setBorderPainted(false);
            toolbarNew.setToolTipText("New Window (C + n)");
            toolbarNew.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarNew.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fileNewWin(e);
                }
            });
            toolBar1.add(toolbarNew);

            //---- toolbarOpen ----
            toolbarOpen.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarOpen.setBorderPainted(false);
            toolbarOpen.setToolTipText("Open... (C + o)");
            toolbarOpen.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fileOpen(e);
                }
            });
            toolBar1.add(toolbarOpen);

            //---- toolbarSave ----
            toolbarSave.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarSave.setBorderPainted(false);
            toolbarSave.setToolTipText("Save File (C + s)");
            toolbarSave.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fileSave(e);
                }
            });
            toolBar1.add(toolbarSave);

            //---- toolbarClose ----
            toolbarClose.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarClose.setBorderPainted(false);
            toolbarClose.setToolTipText("Close (C + w)");
            toolbarClose.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fileClose(e);
                }
            });
            toolBar1.add(toolbarClose);

            //---- toolbarPrint ----
            toolbarPrint.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarPrint.setBorderPainted(false);
            toolbarPrint.setToolTipText("Print (C + p)");
            toolbarPrint.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    filePrint(e);
                }
            });
            toolBar1.add(toolbarPrint);
            toolBar1.addSeparator();

            //---- toolbarCut ----
            toolbarCut.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarCut.setBorderPainted(false);
            toolbarCut.setToolTipText("Cut (C + x)");
            toolbarCut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editCut();
                }
            });
            toolBar1.add(toolbarCut);

            //---- toolbarCopy ----
            toolbarCopy.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarCopy.setBorderPainted(false);
            toolbarCopy.setToolTipText("Copy (C + c)");
            toolbarCopy.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editCopy();
                }
            });
            toolBar1.add(toolbarCopy);

            //---- toolbarPaste ----
            toolbarPaste.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarPaste.setBorderPainted(false);
            toolbarPaste.setToolTipText("Paste (C + v)");
            toolbarPaste.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editPaste();
                }
            });
            toolBar1.add(toolbarPaste);
            toolBar1.addSeparator();

            //---- toolbarFind ----
            toolbarFind.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarFind.setToolTipText("Find (C + f)");
            toolbarFind.setBorderPainted(false);
            toolbarFind.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editFind();
                }
            });
            toolBar1.add(toolbarFind);
            toolBar1.addSeparator();

            //---- toolbarHelp ----
            toolbarHelp.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            toolbarHelp.setBorderPainted(false);
            toolbarHelp.setToolTipText("User Guide");
            toolbarHelp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    helpHelp();
                }
            });
            toolBar1.add(toolbarHelp);
        }
        contentPane.add(toolBar1, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //======== scrollPane1 ========
        {
            scrollPane1.setAutoscrolls(true);

            //======== noWrapPanel ========
            {
                noWrapPanel.setLayout(new BorderLayout());
                noWrapPanel.add(editorPane, BorderLayout.CENTER);
            }
            scrollPane1.setViewportView(noWrapPanel);
        }
        contentPane.add(scrollPane1, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- fileStatusField ----
        fileStatusField.setText("Status Text");
        fileStatusField.setForeground(Color.blue);
        fileStatusField.setToolTipText("File Status");
        contentPane.add(fileStatusField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- editStatusField ----
        editStatusField.setText("(Edited)");
        editStatusField.setForeground(Color.blue);
        editStatusField.setToolTipText("Edit status");
        contentPane.add(editStatusField, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));
        setSize(500, 400);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu menuFile;
    private JMenuItem menuWindowNew;
    private JMenuItem menuFileOpen;
    private JMenuItem menuFileClose;
    private JMenuItem menuFileSave;
    private JMenuItem menuFileSaveAs;
    private JMenuItem menuFilePasswordChange;
    private JSeparator separator1;
    private JMenuItem menuFileImport;
    private JMenuItem menuFileExport;
    private JMenuItem menuFilePrint;
    private JMenuItem menuFileQuit;
    private JMenu menuEdit;
    private JMenuItem menuEditUndo;
    private JMenuItem menuEditRedo;
    private JMenuItem menuEditCut;
    private JMenuItem menuEditCopy;
    private JMenuItem menuEditPaste;
    private JMenuItem menuEditDelete;
    private JMenuItem menuEditSelectAll;
    private JMenuItem menuEditFind;
    private JMenuItem menuEditFindNext;
    private JMenuItem menuEditFindReplace;
    private JMenu menuSettings;
    private JMenuItem menuSettingsTextColor;
    private JMenuItem menuSettingsBackgroundColor;
    private JMenuItem menuSettingsTextFont;
    private JMenuItem menuSettingsTabSize;
    private JCheckBoxMenuItem menuSettingsLineWrapChkbox;
    private JMenuItem menuSettingsWindowStyle;
    private JCheckBoxMenuItem MenuSettingsDebugLogChkbox;
    private JMenuItem menuSettingsRestoreDefaults;
    private JMenu menuHelp;
    private JMenuItem menuHelpHelp;
    private JMenuItem menuHelpViewLicense;
    private JMenuItem menuHelpAbout;
    private JToolBar toolBar1;
    private JButton toolbarNew;
    private JButton toolbarOpen;
    private JButton toolbarSave;
    private JButton toolbarClose;
    private JButton toolbarPrint;
    private JButton toolbarCut;
    private JButton toolbarCopy;
    private JButton toolbarPaste;
    private JButton toolbarFind;
    private JButton toolbarHelp;
    private JScrollPane scrollPane1;
    private JPanel noWrapPanel;
    private JTextPane editorPane;
    private JLabel fileStatusField;
    private JLabel editStatusField;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    // Definitions for Editor Popup Menu
    private JPopupMenu menuEditorPopup;   // Popup Menu for JTextPane
    private JMenuItem editorPopupMenuItemCut;
    private JMenuItem editorPopupMenuItemCopy;
    private JMenuItem editorPopupMenuItemPaste;
    private JMenuItem editorPopupMenuItemDelete;

    private FileInfo file = null;   // Class defines file variables
    
    private UndoManager undoManager = null;

    private int startingHashCode;       // Used to detect editor changes
    private int startingTextLength;     // Used to detect editor changes
    private String activeDirectory = null;     // For file dialogs.
    private int editorID;      // Unique ID of editor window, used as index into Vector of open editors
    private boolean documentChangeStatusShown = false;    // Indicates "Edited" status is showing
    private boolean redoInformationShown = false;  // Indicates Redo Information shown in Edit Menu

    //byte[] encryptionSignature = {'U', '2', 'F', 's', 'd', 'G', 'V', 'k', 'X', '1'};
    private String encryptionSignature = "U2FsdGVkX1";
    //byte[] fileSignature = "DeadboltEdit Encrypted Data - Do Not Edit :\n".getBytes();
    //String fileSignature = "DeadboltEdit Encrypted Data - Do Not Edit :";
    private String fileSignature = "DeadboltEdit Encrypted ~~ Do Not Modify :";

    private TextSearcher textSearcher = null;   // Search and Replace Class

    // -------- Static --------------------------------------------------
    static private int openEditorCount = 0; //Count of open editor windows
    static private int editorCount = 0;     // Count of all editor windows that have been opened
    // Note: Syntax <DeadboltEdit> below is "Generics", effectively a pre-use cast.  Introduced in JDK 5.0.
    //       It eliminates "Unchecked Exception" warnings, and the need to cast in assignments.
    static private Vector<DeadboltEdit> openEditors = new Vector<DeadboltEdit>();

    static private MacOSXAppAdapter macOSXadapter = null;
    static private MsgLogger logger = null;
    static private Properties runtimeSettings = null;
    static private Properties defaultSettings = null;
    static private Properties settings = null;

    static final private String programVersion = "3.30";    // Program version


    public static void main(String args[]) {
        runtimeSettings = PgmUtils.getRunTimeSettings();           // system dependemt settings
        defaultSettings = PgmUtils.loadDefaultSettings();          // load program default settings
        settings = PgmUtils.loadSavedSettings( defaultSettings, runtimeSettings.getProperty("settings.file") );
        // start debug log if enabled
        if ( "1".equals( settings.getProperty("debug.log.enable")) ) {
            logger = new MsgLogger( runtimeSettings.getProperty("log.file") );  // start logger
        }
        else {
            logger = new MsgLogger( null );     // logger will exist but ignore calls
        }
        //
        File startFile = null;
        if (args.length > 0 ) {
            startFile = new File( args[0] );
        }
        //
        // If we're on MacOS X, load and instantiate the MacOSXAppAdapterClass.
        // If missing, application will revert to generic Java app behavior.
        if (runtimeSettings.getProperty("system.type").equals(PgmUtils.SYS_MACOSX)) {
            try {
                Class c = Class.forName("org.mwsoftware.deadboltedit.MacOSXAppAdapterClass");
                Object o = c.newInstance();
                macOSXadapter = (MacOSXAppAdapter) o;
            }
            catch (Exception e) {
                macOSXadapter = null;   // No adapter, revert to generic app behaviour
            }
            // Build Option:
            if ((macOSXadapter == null) && !BuildConfig.MACOS_APP_ADAPTER_OPTIONAL) {
                System.out.println("Oops - MacOSXAppAdapterClass is missing.");
                logger.out("MacOSXAppAdapterClass is missing.  Exiting ...");
                logger.close();
                System.exit( 1 );
            }
        }
        if ( "1".equals( settings.getProperty("debug.log.enable")) ) {
            logger.out("------ System Properties ------");
            logger.out("os.name = " + System.getProperty("os.name") );
            logger.out("os.version = " + System.getProperty("os.version") );
            logger.out("java.runtime.name = " + System.getProperty("java.runtime.name") );
            logger.out("java.runtime.version = " + System.getProperty("java.runtime.version") );
            logger.out("java.vm.name = " + System.getProperty("java.vm.name") );
            logger.out("java.home = " + System.getProperty("java.home") );
            logger.out("java.ext.dirs = " + System.getProperty("java.ext.dirs") );
            logger.out("------ Run-time Settings ------");
            for (String key : runtimeSettings.stringPropertyNames() ) {
                String value = runtimeSettings.getProperty( key );
                logger.out( key + " = " + value );
            }
            logger.out("------------- Build --------------");
            logger.out( "DeadboltEdit Version: " + programVersion );
            if (runtimeSettings.getProperty("system.type").equals(PgmUtils.SYS_MACOSX)) {
                if (macOSXadapter != null) {
                    logger.out("MacOS X App Extensions: MacOSXAppAdapter included, loaded.");
                }
                else {
                    logger.out("MacOS X App Extensions: MacOSXAppAdapter not included.");
                }
            }
            logger.out("Debug Build: " + BuildConfig.DEBUG);
            logger.out("------ Application Settings ------");
            for (String key : settings.stringPropertyNames() ) {
                String value = settings.getProperty( key );
                logger.out( key + " = " + value );
            }
            logger.out("------");
        }
        if (startFile != null ) {
            logger.out( "Start file (Command-line arg): " + startFile );
        }
        DeadboltEdit DeadboltEdit = new DeadboltEdit( startFile );
    }

}

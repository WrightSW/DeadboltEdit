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
 * TextSearcher.java - Implements core functionality for Find and           *
 *                     Find/Replace.                                        *
 *                                                                          *
 * This class is used by the two dialogs Find and Find/Replace.             *
 * The class will probably be instantiated once per editor instance,        *
 * and passed tp the Find or Find/Replace dialogs.  This permits            *
 * the two dialogs to share current search and replace information.         *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;
 
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;   // needed for BadLocationException

public class TextSearcher {
    public TextSearcher( JTextPane ep ) 
    {
        this.editPane = ep; // Reference to JTextPane
        searchString = null;
        replaceString = null;
        searchIndex = -1;
        nextSearchOffset = 0;
        matchCount = 0;
        matchCase = true;
        dialogLock = false;
        
        // get system clipboard for copy/paste
        clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    
    // findMatchCase() 
    public int findMatchCase( String s ) {
        this.searchString = s;
        this.matchCase = true;
        this.matchCount = 0;
        int editorTextLength = editPane.getDocument().getLength();
        // Bug:  String text = editPane.getText();
        // editPane.getText() returns with platform-specific line endings, causing
        // inconsistent search offests on Windows (crlf).
        // Use editPane.getDocument().getText(0, len) instead, which always uses "/n".
        String text = null;
        try {
            text = editPane.getDocument().getText(0, editorTextLength);
        }
        catch (BadLocationException ble) {
            // Should never happen, but .....
            System.err.println("Warning: TextSearcher.findMatchCase(): BadLocationException reading edit text.");
            editorTextLength = 0;
        }

        if ( (searchString.length() > 0) && (editorTextLength > 0) ) {
            searchIndex = text.indexOf( searchString );
            if (searchIndex >=0 ) {
                matchCount++;
                editPane.setCaretPosition( searchIndex );
                editPane.setSelectionStart( searchIndex );
                editPane.setSelectionEnd( searchIndex + searchString.length() );
                nextSearchOffset = searchIndex + searchString.length();
            }
            return( searchIndex );
        } else {
            nextSearchOffset = 0;
            return( -1 );   // Could make this different (-2)
        }
    }
    
    // findIgnoreCase()
    public int findIgnoreCase( String s) {
        this.searchString = s;
        this.matchCase = false;
        this.matchCount = 0;
        int editorTextLength = editPane.getDocument().getLength();
        String text = null;
        try {
            text = editPane.getDocument().getText(0, editorTextLength).toLowerCase();
        }
        catch (BadLocationException ble) {
            // Should never happen, but .....
            System.err.println("Warning: TextSearcher.findMatchCase(): BadLocationException reading edit text.");
            editorTextLength = 0;
        }

        if ( (searchString.length() > 0) && (editorTextLength > 0) ) {
            searchIndex = text.indexOf( searchString.toLowerCase() );
            if (searchIndex >=0 ) {
                matchCount++;
                editPane.setCaretPosition( searchIndex );
                editPane.setSelectionStart( searchIndex );
                editPane.setSelectionEnd( searchIndex + searchString.length() );
                nextSearchOffset = searchIndex + searchString.length();
            }
            return( searchIndex );
        } else {
            nextSearchOffset = 0;
            return( -1 );   // Could make this different (-2)
        }
    }
    
    // findNextMatchCase()
    public int findNextMatchCase( ) {
        this.matchCase = true;
        int editorTextLength = editPane.getDocument().getLength();
        String text = null;
        try {
            text = editPane.getDocument().getText(0, editorTextLength);
        }
        catch (BadLocationException ble) {
            // Should never happen, but .....
            System.err.println("Warning: TextSearcher.findMatchCase(): BadLocationException reading edit text.");
            editorTextLength = 0;
        }

        if ( (searchString.length() > 0) && (editorTextLength > 0) ) {
            searchIndex = text.indexOf( searchString, nextSearchOffset );
            if (searchIndex >=0 ) {
                matchCount++;
                editPane.setCaretPosition( searchIndex );
                editPane.setSelectionStart( searchIndex );
                editPane.setSelectionEnd( searchIndex + searchString.length() );
                nextSearchOffset = searchIndex + searchString.length();
            }
            return( searchIndex );
        } else {
            nextSearchOffset = 0;
            return( -1 );   // Could make this different (-2)
        }
    }
    
    // findNextIgnoreCase()
    public int findNextIgnoreCase( ) {
        this.matchCase = false;
        int editorTextLength = editPane.getDocument().getLength();
        String text = null;
        try {
            text = editPane.getDocument().getText(0, editorTextLength).toLowerCase();
        }
        catch (BadLocationException ble) {
            // Should never happen, but .....
            System.err.println("Warning: TextSearcher.findMatchCase(): BadLocationException reading edit text.");
            editorTextLength = 0;
        }

        if ( (searchString.length() > 0) && (editorTextLength > 0) ) {
            searchIndex = text.indexOf( searchString.toLowerCase(), nextSearchOffset );
            if (searchIndex >=0 ) {
                matchCount++;
                editPane.setCaretPosition( searchIndex );
                editPane.setSelectionStart( searchIndex );
                editPane.setSelectionEnd( searchIndex + searchString.length() );
                nextSearchOffset = searchIndex + searchString.length();
            }
            return( searchIndex );
        } else {
            nextSearchOffset = 0;
            return( -1 );   // Could make this different (-2)
        }
    }
    
    public void replaceSelectedString( String replacementStr ) {
        this.replaceString = replacementStr;
        // copy replacement string to system clipboard
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(replaceString);
        clipboard.setContents(selection, selection);
        editPane.paste();   // replace selection in edit buffer
        // now highlight (select) the replacement text
        editPane.setCaretPosition( searchIndex );
        editPane.setSelectionStart( searchIndex );
        editPane.setSelectionEnd( searchIndex + replaceString.length() );
        nextSearchOffset = searchIndex + replaceString.length();
        // Following de-selects text and leaves cursor at end of replaced text
        //editPane.setCaretPosition( nextSearchOffset );
        //editPane.setSelectionStart( nextSearchOffset );
        //editPane.setSelectionEnd( nextSearchOffset );
    }
    
    public boolean canContinueSearch() {
        if ((searchString != null) && (searchIndex >= 0) && (nextSearchOffset > 0)) {
            return( true );
        } else {
            return( false );
        }
    }
    
    // A Find or Find/Replace can lock the TextSearcher instance
    // to indicate an active dialog.
    public boolean hasDialogLock() {
        return( this.dialogLock );
    }
    
    public void setDialogLock() {
        this.dialogLock = true;
    }
    
    public void resetDialogLock() {
        this.dialogLock = false;
    }
    
    public String getSearchString() {
        return( this.searchString );
    }
        
    public String getReplaceString() {
        return( this.replaceString );
    }
    
    public int getMatchCount() {
        return( this.matchCount );
    }
    
    public boolean isMatchCaseSet() {
        return( matchCase );
    }
    
//  ---------------------------------------------
    
    private JTextPane editPane;
    private String searchString;
    private String replaceString;
    private int searchIndex;    // Index in text buffer
    private int nextSearchOffset;   // length of either search string or replace string
    private int matchCount;     // No. matches of current search string
    private boolean matchCase;
    private boolean dialogLock;     // Lock by active Dialog
    
    java.awt.datatransfer.Clipboard clipboard;
}

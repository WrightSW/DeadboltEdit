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
 * MacOSXAppAdapterClass.java - This class is intended to insulate the      *
 *      application from MacOS-specific API's that cannot be resolved on    *
 *      non-MacOS systems.  This class will most likely be loaded           *
 *      dynamically using reflection.                                       *
 *                                                                          *
 * Used with: MacOSXAppAdapter (abstract class)                             *
 *            MacOSXOpenFileAdapterCallback (interface)                     *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

//import com.apple.eawt.AppEvent.OpenFilesEvent;
//import com.apple.eawt.Application;
//import com.apple.eawt.OpenFilesHandler;

public class MacOSXAppAdapterClass extends MacOSXAppAdapter implements com.apple.eawt.OpenFilesHandler{

    //private List<File> files = null;
    private static List<File> fileList = new ArrayList<File>();
    private java.util.List<MacOSXOpenFileAdapterCallback> listeners = new ArrayList<MacOSXOpenFileAdapterCallback>();

    public MacOSXAppAdapterClass() {
        com.apple.eawt.Application.getApplication().setOpenFileHandler(this);
    }
    
    // Methods to support the MacOS X com.apple.eawt.OpenFileHandler, and our
    // OpenFileAdapterCallback.
    //
    // The OpenFilesHandler service does the following:
    // 1. When the application is launched by double-clicking an associated file
    //    (defined in App Bundle), the filename is passed through this service.
    // 2. When an associated file (defined in App Bundle) is double-clicked while
    //    the app is running, the app is signalled to open the new file by way
    //    of a callback to the registered OpenFileHandler (this).
    //
    public void addCallback(MacOSXOpenFileAdapterCallback listener) {
        listeners.add( listener);
    }

    public List<File> getFiles() {
        return fileList;
    }
    
    public File getFile() {
        File tmpFile = null;
        if (fileList.size() > 0 ) {
            tmpFile = fileList.get(0);
            fileList.remove(0);
        }
        return tmpFile;
    }

    public void openFiles(com.apple.eawt.AppEvent.OpenFilesEvent event) {
        List<File> files = null;
        //System.err.println("Debug: MacOSXOpenFileAdapter - Got an OpenFilesEvent");
        files = event.getFiles();
        if ( files != null ) {
            List<File> tmpFileList = new ArrayList<File>( files );
            try {
                fileList.addAll( tmpFileList );
                //System.err.println("Debug: Path = " + fileList.get(0).getPath() );
            }
            catch (NullPointerException e) {
                System.err.println("MacOSXOpenFileAdapter Error: Null Pointer Exception");
            }
        }
        // for-each loop (introduced JDK 5.0) (see For_Each_Loop.rtf)
        for ( MacOSXOpenFileAdapterCallback callback : listeners ) {
            callback.MacOSXOpenFileAdapterListener();
        }
    }
    // End: com.apple.eawt.OpenFileHandler methods.
    
    // setQuitStrategy():
    // Set a QuitStrategy to catch the Quit event from either the Apple menu or Dock.
    // Default behavior is to terminate the Java JVM and app, without an opportunity
    // for the application to handle window-close events.
    //
    public void setQuitStrategy() {
        com.apple.eawt.Application thisApp = com.apple.eawt.Application.getApplication();
        thisApp.disableSuddenTermination();
        // From reading OpenJDK, default QuitStrategy (and only other choice) is SYSTEM_EXIT_0
        thisApp.setQuitStrategy(com.apple.eawt.QuitStrategy.CLOSE_ALL_WINDOWS);
    }
}



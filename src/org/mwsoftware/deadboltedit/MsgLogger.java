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
 * MsgLogger.java - A simple logger class                                   *
 *                                                                          *
 * The constructor redirects System.err to a specified logfile,             *
 * allowing all output to System.err to be logged.  The Close() method      *
 * closes the PrintStream and restores the original PrintStream that        *
 * was originally assignment to System.err.                                 *
 *                                                                          *
 * Notes:                                                                   *
 *  1. Constructor will attempt to create any needed directories for        *
 *       for the logfile pathname.                                          *
 *                                                                          *
 *  2. A primitive form of file locking is implemented which permits        *
 *     only a single logfile.  For multiple instances of the program,       *
 *     only the first instance will hcreate a logfile.                      *
 *                                                                          *
 * Usage:                                                                   *
 *   MsgLogger logger = new MsgLogger( LogfileFullPath );                   *
 *                      if LogfileFullPath is null, the logger object       *
 *                      is created but will quietly ignore calls.           *
 *                      Call MsgLogger.open( String LogfileFullPath )       *
 *                      open and start logfile at a later time.             *
 *                                                                          *
 *   // open logfile if not already opened.  Use this if constructor        *
 *   // called with null  LogfileFullPath, or if closed.                    *
 *   Msglogger logger = new MsgLogger( null );                              *
 *   logger.open( LogfileFullPath );                                        *
 *                                                                          *
 *   // output to System.err if logfile is opened successfully              *
 *   // Use this for discretionary message that should only go to the       *
 *   // logfile it it exists.                                               *
 *   logger.out("Log this message.");                                       *
 *                                                                          *
 *   // output to System.err unconditionally                                *
 *   // These are critical messages that should have an opportunity to      *
 *   // be sen, no matter where System.err is assigned.                     *
 *   // ex. Exceptions, IO errors                                           *
 *   System.err.println("Message will go to logfile if redirect OK");       *
 *                                                                          *
 *   logger.close();  // Close the logfile and restore System.err           *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MsgLogger {
    static boolean logfileOpen = false;
    static PrintStream origSystemErr = null;
    static File lockfile;
    static final String dateFormat = "E yyyy-MM-dd 'at' hh:mm:ss a";
    
    public MsgLogger( String logFilePath ) {
        // a null argument is legitimate, it disables logging but allows
        // a legitimate MsgLogger to be created.
        if ( logFilePath == null ) {
            logfileOpen = false;
            return;
        }
        else {
            this.open( logFilePath );
            return;
        }
    }
    
    public void open( String logFilePath ) {
        if ( logfileOpen ) {    // ignore
            return;
        }
        File logFile = new File( logFilePath );
        String logParentDirName = logFile.getParent();
        if (logParentDirName != null ) {
            File logDir = new File( logParentDirName );
            if ( ! logDir.isDirectory() ) {
                if ( !logDir.mkdirs() ) {
                    System.err.println("Error in Logger(): Unable to create log directory " + logDir );
                    return;
                }
            }
        }
        // Attempt to create a lock file
        lockfile = new File( new String(logFilePath + ".lock") );
        if ( lockfile.exists() ) {
            return;
        }
        else {
            try {
                lockfile.createNewFile();
                lockfile.deleteOnExit();  // in case of Ctrl-C or other abort 
            }
            catch (Exception e) {
                System.err.println("Unable to create lockfile for session log.");
                return;
            }
        }
        // Save the PrintStream currently assigned to System.err
        origSystemErr = System.err;
        // Re-direct System.err to our logfile
        System.err.flush();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream( logFile );
        } catch (java.io.FileNotFoundException e1) {
            System.err.println("Error in Logger() - Unable to open logfile for write.");
            logfileOpen = false;
            return;
        }
        PrintStream ps = new PrintStream( fos );
        try {
            System.setErr( ps );
        } catch (java.lang.SecurityException e2) {
            // Possible if Security Manager is installed - shouldn't happen ....
            logfileOpen = false;
            return;
        }
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat( dateFormat );
        System.err.println("- Begin Log: " + formatter.format( now ) + " -" );
        logfileOpen = true;
    }

    // send output if logfile is open
    public void out( String msg ) {
        if ( logfileOpen ) {   
            System.err.println( msg );
        }
    }
    
    // return open status
    public boolean isOpen() {
        return logfileOpen;
    }

    public void close() {
        if ( logfileOpen ) {
            logfileOpen = false;    // disable further calls to out()
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat( dateFormat );
            System.err.println("- End Log:   " + formatter.format( now ) + " -" );
            System.err.flush();
            System.err.close();
            if ( origSystemErr != null ) {
                System.setErr( origSystemErr );       // restore system.err
            }
            try {
                if (lockfile.exists() )
                    lockfile.delete();
            }
            catch (Exception e) {
            }
        }
    }
}

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
 * PgmUtils.java - Utility functions for DeadboltEdit.                      *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.io.*;
import java.security.*;     // MD5 hash
import java.math.*;         // MD5 hash

import java.util.*;         //Arrays.fill(), Properties
import java.lang.*;         // Double, Math(static)

import java.awt.Color;      // FALLBACK_DEFAULTS Def.s
import java.awt.Font;       // FALLBACK_DEFAULTS Def.s

public class PgmUtils {
    //
    //   --- Constants, package scope ---
    // System ID, used for "system.type" in rtSettings:
    //
    protected static final String SYS_MACOSX = "Mac";
    protected static final String SYS_WINDOWS = "Win";
    protected static final String SYS_LINUX = "Lnx";
    protected static final String SYS_UNIX = "UNX";
    //
    // Fallback default settings values.  These are used in the unlikely
    // event that the Settings properties become corrupt. The fallback values
    // are used to keep the program running and allow the program to correct the problem
    // by storing and recalling viable settings.
    protected static final Color FALLBACK_DEFAULT_FOREGROUND_COLOR = Color.BLACK;
    protected static final Color FALLBACK_DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    protected static final Font  FALLBACK_DEFAULT_TEXT_FONT = new Font("Monospaced", Font.PLAIN, 12);
    protected static final int   FALLBACK_DEFAULT_TAB_SIZE = 8;
    //
    protected static final int MAX_TABSIZE = 40;
    //
    //   --- End Constants ---

    // bufferHasString()
    // Check for a string at beginning of buffer
    public static boolean bufferHasString( byte[] buffer, String signature ) {
        if (buffer.length < signature.length() )
            return( false );
        for (int i=0; i< signature.length(); i++) {
            //System.err.println("buffer[" + i + "] = " + buffer[i] );
            if (buffer[i] != (byte) signature.charAt(i))
                return( false );
        }
        return( true );
    }

    // Return MD5 Hash value
    public static String MD5Hash(byte [] data) {
        try {
            MessageDigest md=MessageDigest.getInstance("MD5");
            md.update( data );
            return new BigInteger(1,md.digest()).toString(16);
        } catch (Exception e) {
            System.err.println("Exception occurred while computing MD5 Hash.");
            //e.printStackTrace();
            return "";
        }
    }

    // Return MD5 Hash value
    public static String MD5Hash(String str) {
        try {
            byte[] data = str.getBytes("UTF-8");
            MessageDigest md=MessageDigest.getInstance("MD5");
            md.update( data );
            return new BigInteger(1,md.digest()).toString(16);
        } catch (Exception e) {
            System.err.println("Exception occurred while computing MD5 Hash.");
            //e.printStackTrace();
            return "";
        }
    }

    // Return MD5 Hash value
    public static String MD5Hash(char [] data) {
        int bufflength = data.length;
        byte[] dataBytes = new byte[ bufflength ];
        for (int i=0; i<bufflength; i++) {
            dataBytes[i] = (byte) data[i];
        }
        try {
            MessageDigest md=MessageDigest.getInstance("MD5");
            md.update( dataBytes );
            return new BigInteger(1,md.digest()).toString(16);
        } catch (Exception e) {
            System.err.println("Exception occurred while computing MD5 Hash.");
            //e.printStackTrace();
            return "";
        } finally {     // This will happen before either of the 2 returns execute
            for (int i=0; i<bufflength; i++) {
                dataBytes[i] = 0;
            }
        }
    }

    // Strip non-ASCII characters from a String.  We're also removing DEL (X'7F'),
    // which is a legal ASCII char but should never be in our editor buffer.
    // Added for Rev. 1.09 bug fix.
    // (Optimized by using a single char buffer)
    public static String StripNonAsciiChar( String s ) {
        int length = s.length();
        char[] oldChars = new char[length];
        s.getChars(0, length, oldChars, 0);
        int newLen = 0;
        for (int j = 0; j < length; j++) {
            char ch = oldChars[j];
            if ( ch <= '~' ) {
                oldChars[newLen] = ch;
                newLen++;
            }
        }
        return new String(oldChars, 0, newLen);
    }

    // Determine application root dir from running jar file or class file
    // Note: This is called by getRunTimeSettings(), not expected to be called
    //       from anywhere else.
    public static String getAppRootDir() {
        // Note: we are calling class.getProtectionDomain() in a static context, using the
        // explicit class name DeadboltEdit because we are being invoked from main(), and
        // DeadboltEdit hasn't been instantiated yet.  In a non-static context, we could
        // call class.getProtectionDomain().
        String path = DeadboltEdit.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            return "";
        }
        // remove '/' that precedes Windows drive prefix, and jar or class file from end
        if ( path.lastIndexOf(':') >= 0 ) {
            // if Windows drive prefix
            path = path.substring( path.lastIndexOf(':')-1, path.lastIndexOf('/') );
        }
        else {
            path = path.substring( 0, path.lastIndexOf('/') );
        }
        // remove escape characters (Windows)
        path = path.replace('%', ' ');  // should be gone, just in case ...
        String filteredPath = "";
        for (int k=0; k<path.length(); k++) {
            filteredPath += path.charAt(k);
            //if (path.charAt(k) == ' ') k += 2;
        }
        filteredPath = filteredPath.replace('/', File.separatorChar);
        return filteredPath;
    }

    // Return system dependent run-time settings as a Property set
    public static Properties getRunTimeSettings() {
        Properties rtSettings = new Properties();
        // determine run-time settings that are system dependent:
		String osName = System.getProperty("os.name");
		String userHome = System.getProperty("user.home");
		String archDataModel = System.getProperty("sun.arch.data.model");   // 32 or 64 (bit)
		//
		String settingsDir = null;
		String settingsFile = null;
		String logFile = null;
		String systemType = null;
		String platformDesc = null;

		boolean ignoreCase = true;
		if( osName.regionMatches(ignoreCase, 0,"windows",0,7) ) {
			systemType = SYS_WINDOWS;
		    platformDesc = "Windows, " + archDataModel + "-bit";
			settingsDir = userHome + File.separator + "Application Data" + File.separator + "DeadboltEdit";
			settingsFile = settingsDir + File.separator + "settings.dat";
			logFile = settingsDir + File.separator + "debug.log";
		}
		else if ( osName.regionMatches(ignoreCase, 0,"mac os",0,6) ) {
			systemType = SYS_MACOSX;
		    platformDesc = "Mac OS X, " + archDataModel + "-bit";
			settingsDir = userHome + File.separator + "Library" + File.separator + "DeadboltEdit";
			settingsFile = settingsDir + File.separator + "settings.dat";
			logFile = settingsDir + File.separator + "debug.log";
		}
		else if ( osName.regionMatches(ignoreCase, 0,"linux",0,5) ) {
			systemType = SYS_LINUX;
		    platformDesc = "Linux, " + archDataModel + "-bit";
			settingsDir = userHome + File.separator + ".DeadboltEdit";
			settingsFile = settingsDir + File.separator + "settings.dat";
			logFile = settingsDir + File.separator + "debug.log";
		}
		else {
		    systemType = SYS_UNIX;
		    platformDesc = "UNIX, " + archDataModel + "-bit";
			settingsDir = userHome + File.separator + ".DeadboltEdit";
			settingsFile = settingsDir + File.separator + "settings.dat";
			logFile = settingsDir + File.separator + "debug.log";
		}
		rtSettings.setProperty( "user.home", userHome );
		rtSettings.setProperty( "system.type", systemType );
		rtSettings.setProperty( "settings.dir", settingsDir );
		rtSettings.setProperty( "settings.file", settingsFile );
		rtSettings.setProperty( "log.file", logFile );
		String appRoot = getAppRootDir();    // get app root directory
		rtSettings.setProperty( "app.root", appRoot );
		rtSettings.setProperty( "platform.desc", platformDesc );

        return rtSettings;
    }

    // Load Default Settings from program resource file (jar file).
    public static Properties loadDefaultSettings() {
        Properties defaults = new Properties();
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("Resources/defaultSettings.dat");
        if (in == null ) {
            System.err.println("Error in loadDefaultSettings() - Couldn't open resource file for default settings.");
            //return defaults;    // return empty defaults
        }
        else {
            try {
                defaults.load( in );
            } catch (IOException ioe1) {
                System.err.println("Error in loadDefaultSettings() - IO Error reading default dettings resource file.");
            } finally {
                try {
                    in.close();
                } catch (IOException ioe2 ) {
                    System.err.println("Error in loadDefaultSettings() - IO Error closing default dettings resource file.");
                }
            }
        }
        return defaults;
    }

    // Load Settings that were saved from previous run. Location is system dependent.
    public static Properties loadSavedSettings( Properties defaults, String settingsFile ) {
        Properties settings = new Properties( defaults );

        FileInputStream in = null;

        try {
            in = new FileInputStream( settingsFile );
        } catch (FileNotFoundException e ) {
            return settings;    // No settings file, return with defaults only
        }

        try {
            settings.load( in );
        } catch (IOException ioe1) {
            System.err.println("Error in loadSettings() - IO Error reading settings file.");
        } finally {
            try {
                in.close();
            } catch (IOException ioe2 ) {
                System.err.println("Error in loadSettings() - IO Error closing settings file.");
            }
        }
        return settings;
    }

    // Save Settings that differ from default.  This will only succeed if user has write
    // permissings to Settings Directory.
    public static void saveSettings( Properties settings, String settingsDir, String settingsFile ) {
        File sd = new File( settingsDir );
        File sf = new File( settingsFile );
        if ( !sd.isDirectory() ) {
            if ( !sd.mkdirs() ) {
                System.err.println("Warning in saveSettings() - Couldn't create settings directory.");
                return;         // Can't create Settings Dir
            }
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream( sf );
            settings.store( out, "-- DeadboltEdit Settings File --");
        } catch (FileNotFoundException e ) {
            System.err.println("Warning in saveSettings() - Couldn't open settings file for write.");
        } catch (IOException ioe1) {
            System.err.println("Warning in saveSettings() - IO error writing settings file.");
        } finally {
            try {
                out.close();
            } catch (IOException ioe2 ) {
                System.err.println("Warning in saveSettings() - IO Error closing settings file.");
            }
        }
    }
}

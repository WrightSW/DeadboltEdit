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
 * FileInfo.java - A class to define file variables.                        *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

import java.util.Arrays;    //Arrays.fill()
import java.io.*;                   // Encryption
import org.apache.commons.ssl.*;    // Encryption
import java.security.*;     // For MD5 Hash
import java.math.*;         // For MD5 Hash
import java.lang.*;    // Double, Math(static)

public class FileInfo {
    private String fullpath;
    private String basename;
    private String ext;     // Includes ".", example ".ctxt"
    private byte[] encryptedPassword;   // Password stored encrypted
    private String MD5Hash;             // MD5 Hash of clear text password
    
    // Used to internally encrypt file password
    private char[] internalPW =
        ("Encoding Error" +  new Double( Math.random() ).toString() ).toCharArray();
    
    //private boolean encrypted;
    public FileInfo() {
        this.fullpath = null;
        this.basename = null;
        this.ext = null;
        this.encryptedPassword = null;
        this.MD5Hash = null;
        //this.encrypted = false;   
    }
    /* 
     * copyIn() - Copy from existing FileInfo into this one
     *
     */
    public void copyIn( FileInfo f) {
        this.fullpath = f.getFullpath();
        this.basename = f.getBasename();
        this.ext = f.getExt();
        this.setPassword( f.getPassword() );  // NOT TESTED
        this.MD5Hash = f.getMD5Hash();
        //this.encrypted = f.getEncrypted();
        return;
    }        
    public String getFullpath () 
    {
        return this.fullpath;
    }
    public void setFullpath (String fullpath)
    {
        this.fullpath = fullpath;
    }
    public String getBasename () 
    {
        return this.basename;
    }
    public void setBasename (String basename)
    {
        this.basename = basename;
    }
    public String getExt () 
    {
        return this.ext;
    }
    public void setExt (String ext)
    {
        this.ext = ext;
    }
    public char[] getPassword () 
    {
        byte[] decryptedPassword = null;
        if ( encryptedPassword == null ) {
            return null;
        }
        else {
            try {
                decryptedPassword = OpenSSL.decrypt("des3", internalPW, encryptedPassword);
            } catch (IOException ioe) {
                System.err.println("IO Error occurred during password decryption.");
                ioe.printStackTrace();
            } catch (java.security.GeneralSecurityException gse) {
                System.err.println("GeneralSecurityException Error occurred during password decryption.");
                gse.printStackTrace();
            }
            char[] cleartextPassword = new char[decryptedPassword.length];
            for (int i=0; i<decryptedPassword.length; i++) {
                cleartextPassword[i] = (char) decryptedPassword[i];
            }
            Arrays.fill(decryptedPassword, (byte) ' ');
            //return this.password;
            return cleartextPassword;
        }
    }
    public void setPassword (char[] cleartextPassword)
    {
        // encrypt and store password
        if ( cleartextPassword == null ) {
            encryptedPassword = null;
        }
        else {
            byte[] tmpPassword = new byte[cleartextPassword.length];
            for (int i=0; i<cleartextPassword.length; i++) {
                tmpPassword[i] = (byte) cleartextPassword[i];
            }
            try {
                encryptedPassword = OpenSSL.encrypt("des3", internalPW, tmpPassword);
            } catch (IOException ioe) {
                System.err.println("IO Error occurred during password encryption.");
                ioe.printStackTrace();
            } catch (java.security.GeneralSecurityException gse) {
                System.err.println("GeneralSecurityException Error occurred during password encryption.");
                gse.printStackTrace();
            }
            // Compute MD5 Hash for this password
            MD5Hash = PgmUtils.MD5Hash( tmpPassword );
            Arrays.fill(tmpPassword, (byte) ' ');
        }
        return;
    }
    public boolean passwordIsSet() 
    {
        if (encryptedPassword == null )
            return false;
        else
            return true;
    }
    public String getMD5Hash()
    {
        return MD5Hash;
    }
    //public boolean getEncrypted () 
    //{
    //    return this.encrypted;
    //}
    //public void setEncrypted (boolean encrypted)
    //{
    //    this.encrypted = encrypted;
    //}
}
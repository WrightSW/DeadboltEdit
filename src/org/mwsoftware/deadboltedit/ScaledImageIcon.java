/*
 ****************************************************************************
 * Copyright (C) 2025        Michael Wright   All Rights Reserved           *
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
 * ScaledImageIcon.java - Class to scale an ImageIcon.                      *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;


import javax.swing.ImageIcon;
import java.awt.Image;

public class ScaledImageIcon extends ImageIcon {
    //
    //   --- Constants, package scope ---
    //

    // Create a scaled icon
    public ScaledImageIcon( java.net.URL iconURL, double scaleFactor ) {
        super();
        ImageIcon originalIcon = new ImageIcon(iconURL);
        int iconWidth = (int) (originalIcon.getIconWidth() * scaleFactor);
        int iconHeight = (int) (originalIcon.getIconHeight() * scaleFactor);
        Image scaledImage = originalIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
        //scaledIcon = new ImageIcon(scaledImage);
        this.setImage(scaledImage);
    }
}

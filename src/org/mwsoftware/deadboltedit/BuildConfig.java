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
 * BuildConfig.java - Define constants for conditional compile options.     *
 *                                                                          *
 ****************************************************************************
 */
package org.mwsoftware.deadboltedit;

public final class BuildConfig {
    //set to false to allow compiler to identify and eliminate debug code
    public static final boolean DEBUG = false;

    // Option to require MacOSXAppAdapterClass on Mac OS X systems.
    // (Only affects Mac OS X systems - MacOSXAppAdapterClass provides MacOS X app extensions)
    //
    // true  : On Mac systems, use MacOSXAppAdapterClass if available, otherwise revert to
    //         generic Java app behaviour if not included in the build.
    // false : On Mac systems, exit application with error code if MacOSXAppAdapterClass
    //         cannot be found and loaded.
    public static final boolean MACOS_APP_ADAPTER_OPTIONAL = false;
}


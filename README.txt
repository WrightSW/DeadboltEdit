                            ~~~ Readme ~~~

                   DeadboltEdit Source Distribution


DeadboltEdit may be examined and built with the source files that are
included in this source distribution (the package that includes this file).

Building DeadboltEdit requires some familiarity with building Java programs.

Requirements To Build DeadboltEdit
----------------------------------
You will need:
- A Java JDK for Java version 1.7 or greater (Java 7 JDK).

- The Not-Yet-Commons-SSL library
  http://juliusdavies.ca/commons-ssl/index.html

- GNU make utility

- UNIX/Linux/GNU command-line environment that is Bourne Shell
  compatible (eg. Bash or sh). Suitable environments include Mac OS X
  command-line, Linux, and MinGW on Windows.

On Mac OS X systems, you may need to install the GNU Command Line
Compiler Tools to get the make utility, which is a free download option
with Xcode from the Apple App Store.


Building DeadboltEdit
---------------------
1. Configuration:
   A. Edit file src/org/mwsoftware/deadboltedit/BuildConfig.java to set
      desired compile options.  See comments in BuildConfig.java for an
      explanation of the options.

   B. Edit Makefile to make adjustments for your system. You should only
      need to make changes in the top-most section of Makefile.
      1. Set INCLUDE_MACOSX_APPADAPTER to "true" or "false".  If set to
         "true", you must have the MacOSX-specific class libraries
         (com.apple.eawt.* and com.apple.laf.*) in your compiler classpath.

      (See note below about Java 8 on MacOSX)

      The MacOSXAppAdapter is optional for MacOSX systems, and not used at
      all on other systems.  If running the application on MacOSX and the
      MacOSXAppAdapter is uncluded, it provides improved application
      integration on MacOSX systems. (eg. Ctrl-Q Quit handler) If not included,
      the program will revert to generic Java cross-platform behavior on MacOSX.

      2. Set LIBS, JFLAGS_PRODUCTION, and JFLAGS_DEBUG to include the
         correct path to your installation of Not-Yet-Commons-SSL library.

      Note: When editing a Makefile, each indented line must begin with
      a "tab" character, not spaces.

2. Build:

   cd Project_base_directory (the directory that contains the "Makefile")

   make clean
   make        (for normal build)
       - or -
   make debug  (for debug build)

The make command will build DeadboltEdit.jar in the subdirectory
"DeadboltEdit".

The subdirectory "DeadboltEdit" contains the application files.

To run the program:
    DeadboltEdit/deadboltedit  (Mac OS X or Linux)
        - or -
    DeadboltEdit\deadboltedit.bat  (Windows)


Installing DeadboltEdit on Your System
--------------------------------------
Copy the "DeadboltEdit" directory and its complete contents to the
installation directory of your choice.  Use a copy method that will
preserve the executable permissions.  Copy-and-Paste using your system
File Manager will usually work well for this, if you have Admin rights
on the system.

The program can be launched by commad-line:
    /Installation_Directory/DeadboltEdit/deadboltedit  (Mac OS X or Linux)
        - or -
    \Installation_Directory\DeadboltEdit\deadboltedit.bat  (Windows)

On Windows, MacOSX, and many Linux systems, the program can be launched
from the GUI by double-clicking the file DeadboltEdit.jar. You can create
a desktop shortcut to Installation_Directory/DeadboltEdit/DeadboltEdit.jar.
Icons are included in Installation_Directory/DeadboltEdit/icons that can
be used to decorate your desktop shortcut.
    Windows: DeadboltEdit.ico
    MacOSX : DeadboltEdit.icns
    Linux: DeadboltEdit48x48.png


Not Included
------------
This source distribution does not include the files and framework for
building platform-specific release packages, which consists of the
following:

- Files for the ProGuard shrinker/optimizer/obfuscator (to shrink
  code size)
  http://proguard.sourceforge.net/

- Files for the Launch4j wrapper to create a Windows executable
  http://launch4j.sourceforge.net/

- Files for the Inno Setup installer for Windows
  http://www.innosetup.com/isinfo.php

- Files for the Java AppBundler for Mac OS X
  Project Page: https://java.net/projects/appbundler


Note About Java 8 on MacOSX
---------------------------
DeadboltEdit source files are Java 8 compatibale and will compile
without modification on a Java 8 JDK. As mentioned above, compiling the
MacOSXAppAdapterClass will require some MacOSX-specific classes to exist
in the compiler classpath. These classes exist in the default compiler
classpath when using a Java 7 JDK on MacOSX, but not for Java 8. If
compiling on MacOSX with Java 8, you may need to add a library to your
compiler classpath to resolve the MacOSX-specific classes
(com.apple.eawt.* and com.apple.laf.*). Java 8 includes these classes in
the JRE (rt.jar), so this is only an issue for compiling.

It's my understanding that one simple solution is to copy rt.jar from
the JRE to one of the compiler classpath directories, and name it
something other than rt.jar. My own solution is probably more
complicated than most people would want to do; I created my own library
jar file that contains the com.apple.eawt.* and com.apple.laf.* classes.
I did this originally on Java 7 so that I could compile the
MacOSXAppAdapterClass on Linux or Windows.




Copyright (C) 2015-2020      Michael Wright     All Rights Reserved

